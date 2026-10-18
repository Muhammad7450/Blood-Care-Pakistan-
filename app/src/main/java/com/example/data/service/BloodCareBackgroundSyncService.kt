package com.example.data.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.BloodRequest
import com.example.data.model.CityUtils
import com.example.data.model.NotificationRecord
import com.example.data.model.RequestStatus
import com.example.util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BloodCareBackgroundSyncService : JobService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Background sync job started by JobScheduler")
        serviceScope.launch {
            try {
                syncPendingAlerts(applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Error executing sync job", e)
            } finally {
                jobFinished(params, false)
            }
        }
        return true // asynchronous execution
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Background sync job stopped")
        serviceJob.cancel()
        return true // reschedule
    }

    companion object {
        private const val TAG = "BloodCareSync"
        private const val PERIODIC_JOB_ID = 9182
        private const val IMMEDIATE_JOB_ID = 9183

        /**
         * Schedules a persistent periodic job that survives device reboot and triggers when any network is available.
         */
        fun scheduleJob(context: Context) {
            try {
                val componentName = ComponentName(context, BloodCareBackgroundSyncService::class.java)
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return

                val jobInfo = JobInfo.Builder(PERIODIC_JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Triggers as soon as mobile data or Wi-Fi is active
                    .setPeriodic(15 * 60 * 1000) // Minimum 15-minute interval for persistent check
                    .setPersisted(true) // Survives phone restart
                    .build()

                jobScheduler.schedule(jobInfo)
                Log.d(TAG, "Periodic background sync job scheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule background job", e)
            }
        }

        /**
         * Triggers an immediate sync check when mobile data turns on or user opens app.
         */
        suspend fun triggerImmediateSync(context: Context) = withContext(Dispatchers.IO) {
            try {
                syncPendingAlerts(context)
            } catch (e: Exception) {
                Log.e(TAG, "Immediate sync failed: ${e.message}")
            }
        }

        /**
         * Core logic: Checks for unread notifications and matching open blood requests in donor's city.
         * Shows heads-up popup notifications for anything new!
         */
        private suspend fun syncPendingAlerts(context: Context) {
            val auth = Firebase.auth
            val currentUser = auth.currentUser ?: run {
                Log.d(TAG, "No authenticated user. Skipping background alert check.")
                return
            }

            val db = try {
                FirebaseFirestore.getInstance(
                    context.getString(R.string.firestore_database_id)
                )
            } catch (e: Exception) {
                FirebaseFirestore.getInstance()
            }

            val prefs = context.getSharedPreferences("bloodcare_alert_prefs", Context.MODE_PRIVATE)
            val shownAlertIds = prefs.getStringSet("shown_alerts", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

            // 1. Fetch unread notifications specifically addressed to this user
            try {
                val notifsQuery = db.collection("notifications")
                    .whereEqualTo("recipientUserId", currentUser.uid)
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()

                for (doc in notifsQuery.documents) {
                    val notif = try {
                        NotificationRecord.fromDocument(doc)
                    } catch (e: Exception) { null } ?: continue
                    val alertKey = "notif_${notif.id}"
                    if (!shownAlertIds.contains(alertKey)) {
                        val notifId = (notif.requestId.hashCode().takeIf { it != 0 } ?: notif.id.hashCode())

                        NotificationHelper.showBloodNotification(
                            context = context,
                            notificationId = notifId,
                            title = notif.title,
                            body = notif.body,
                            requestId = notif.requestId
                        )
                        shownAlertIds.add(alertKey)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking user notifications: ${e.message}")
            }

            // 2. Fetch user profile to identify donor city and blood group for real-time request matching
            try {
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                if (userDoc.exists()) {
                    val isDonor = userDoc.getBoolean("isDonor") ?: false
                    val isAvailable = userDoc.getBoolean("isAvailable") ?: true
                    val donorBloodGroup = userDoc.getString("bloodGroup") ?: ""
                    val normalizedCity = userDoc.getString("normalizedCity") ?: ""

                    if (isDonor && isAvailable && normalizedCity.isNotBlank()) {
                        val requestsQuery = db.collection("bloodRequests")
                            .whereEqualTo("normalizedCity", normalizedCity.lowercase())
                            .whereEqualTo("status", RequestStatus.OPEN.name)
                            .get()
                            .await()

                        for (doc in requestsQuery.documents) {
                            val request = try {
                                BloodRequest.fromDocument(doc)
                            } catch (e: Exception) { null } ?: continue
                            // Exclude user's own requests
                            if (request.patientId == currentUser.uid) continue

                            // Verify blood group compatibility
                            if (donorBloodGroup.isNotBlank() && !CityUtils.isBloodCompatible(donorBloodGroup, request.bloodGroup)) {
                                continue
                            }

                            val alertKey = "req_${request.id}"
                            if (!shownAlertIds.contains(alertKey)) {
                                val notifId = request.id.hashCode()
                                val urgencyTag = request.urgency.uppercase()
                                val title = "🚨 BLOODCARE PAKISTAN - $urgencyTag REQUEST"
                                val body = "Urgent: ${request.bloodGroup} needed at ${request.hospitalName} (${request.city}). ${request.unitsRequired} unit(s) requested."

                                NotificationHelper.showBloodNotification(
                                    context = context,
                                    notificationId = notifId,
                                    title = title,
                                    body = body,
                                    requestId = request.id
                                )
                                shownAlertIds.add(alertKey)
                                Log.d(TAG, "Popped up blood request alert for: ${request.id}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking city blood requests: ${e.message}")
            }

            prefs.edit().putStringSet("shown_alerts", shownAlertIds).apply()
        }
    }
}

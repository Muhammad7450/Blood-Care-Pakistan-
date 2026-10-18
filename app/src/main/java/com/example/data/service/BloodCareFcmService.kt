package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.data.repository.AuthRepository
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BloodCareFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed FCM token: $token")
        val authRepo = AuthRepository(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            authRepo.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Received FCM message: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "🚨 Emergency Blood Request - BloodCare"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "A matching blood request has been posted in your city."
        val requestId = remoteMessage.data["requestId"] ?: ""

        val notifId = (requestId.hashCode().takeIf { it != 0 } ?: System.currentTimeMillis().toInt())

        // Save to shown alerts to prevent duplicate sound/alert
        val prefs = applicationContext.getSharedPreferences("bloodcare_alert_prefs", Context.MODE_PRIVATE)
        val shownAlertIds = prefs.getStringSet("shown_alerts", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (requestId.isNotBlank()) {
            shownAlertIds.add("req_$requestId")
        }
        shownAlertIds.add("fcm_$notifId")
        prefs.edit().putStringSet("shown_alerts", shownAlertIds).apply()

        NotificationHelper.showBloodNotification(
            context = applicationContext,
            notificationId = notifId,
            title = title,
            body = body,
            requestId = requestId
        )
    }
}

package com.example

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.service.BloodCareBackgroundSyncService
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BloodCareApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        Log.d("BloodCareApp", "BloodCareApplication starting...")

        // 1. Create High-Importance Notification Channels immediately on app initialization
        NotificationHelper.createNotificationChannel(this)

        // 2. Schedule persistent background Job for periodic checks & network triggers
        BloodCareBackgroundSyncService.scheduleJob(this)

        // 3. Register NetworkCallback to trigger immediate check as soon as mobile data / Wi-Fi is on
        registerNetworkCallback()

        // 4. Run an initial check for any pending emergency requests/alerts
        applicationScope.launch {
            BloodCareBackgroundSyncService.triggerImmediateSync(applicationContext)
        }
    }

    private fun registerNetworkCallback() {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d("BloodCareApp", "Internet connection restored (Mobile Data / Wi-Fi). Triggering immediate sync.")
                    applicationScope.launch {
                        BloodCareBackgroundSyncService.triggerImmediateSync(applicationContext)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("BloodCareApp", "Failed to register network callback", e)
        }
    }
}

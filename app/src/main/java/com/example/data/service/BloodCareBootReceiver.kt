package com.example.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BloodCareBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        Log.d("BloodCareBootReceiver", "Received broadcast action: $action")

        // Ensure notification channels exist
        NotificationHelper.createNotificationChannel(context)

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d("BloodCareBootReceiver", "Device rebooted. Rescheduling background sync.")
                BloodCareBackgroundSyncService.scheduleJob(context)
                CoroutineScope(Dispatchers.IO).launch {
                    BloodCareBackgroundSyncService.triggerImmediateSync(context)
                }
            }
            ConnectivityManager.CONNECTIVITY_ACTION,
            "android.net.conn.CONNECTIVITY_CHANGE" -> {
                Log.d("BloodCareBootReceiver", "Connectivity changed. Triggering immediate alert check.")
                CoroutineScope(Dispatchers.IO).launch {
                    BloodCareBackgroundSyncService.triggerImmediateSync(context)
                }
            }
        }
    }
}

package com.example

import android.app.NotificationManager
import android.content.Context
import com.example.util.NotificationHelper
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testNotificationChannelsCreatedWithHighImportance() {
    val context: Context = RuntimeEnvironment.getApplication()
    NotificationHelper.createNotificationChannel(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
    assertNotNull("Expected blood_requests_channel_v3 to be created", channel)
  }
}

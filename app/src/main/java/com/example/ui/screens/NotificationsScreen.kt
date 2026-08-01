package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.util.LanguageState

data class NotificationItem(
    val title: String,
    val body: String,
    val time: String,
    val isSmsBackup: Boolean = false
)

@Composable
fun NotificationsScreen() {
    val alerts = listOf(
        NotificationItem("🚨 URGENT O- BLOOD REQUEST", "Critical request at Services Hospital Lahore for O- Negative. Universal donor needed!", "5 mins ago", isSmsBackup = true),
        NotificationItem("🩸 Donor Thank You Badge Earned", "You earned the 'Life Saver' badge for your latest donation certificate!", "1 hour ago"),
        NotificationItem("🛡️ Thalassemia Campaign Reminder", "New Thalassemia transfusion drive at Indus Hospital Karachi.", "1 day ago")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = LanguageState.getText("Push Alerts & SMS Broadcast Logs", "نوٹیفکیشن اور ایس ایم ایس ہسٹری"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(alerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (alert.isSmsBackup) Icons.Default.Sms else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = CrimsonRedPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(alert.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(alert.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(alert.time, style = MaterialTheme.typography.labelSmall, color = CrimsonRedPrimary)
                        }
                    }
                }
            }
        }
    }
}

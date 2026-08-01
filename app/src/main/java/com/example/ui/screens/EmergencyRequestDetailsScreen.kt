package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonorEntity
import com.example.ui.components.BloodGroupChip
import com.example.ui.components.DonorCard
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun EmergencyRequestDetailsScreen(
    requestId: Int,
    viewModel: BloodCareViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var request by remember { mutableStateOf<BloodRequestEntity?>(null) }
    val allDonors by viewModel.allDonors.collectAsState()

    LaunchedEffect(requestId) {
        request = viewModel.repository.getRequestById(requestId)
    }

    val currentRequest = request

    if (currentRequest == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading emergency request details...")
        }
        return
    }

    val compatibleDonors = allDonors.filter { donor ->
        donor.isAvailable && viewModel.repository.isCompatible(donor.bloodGroup, currentRequest.bloodGroup)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Emergency Priority Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CriticalRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY BLOOD ALERT",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = currentRequest.emergencyLevel,
                                color = CriticalRed,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentRequest.patientName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "${currentRequest.units} ${LanguageState.getText("Units Needed", "یونٹس درکار")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        BloodGroupChip(group = currentRequest.bloodGroup)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Details Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currentRequest.hospitalName}, ${currentRequest.city}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Contact Phone: ${currentRequest.contactPhone}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (currentRequest.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notes: ${currentRequest.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SMS Backup Broadcast Action
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${currentRequest.contactPhone}?body=BloodCare Pakistan Emergency Alert: Need ${currentRequest.units} unit(s) of ${currentRequest.bloodGroup} for ${currentRequest.patientName} at ${currentRequest.hospitalName}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("broadcast_sms_button")
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LanguageState.getText("SEND SMS BACKUP ALERT TO DONORS", "ایس ایم ایس بھیجیں"))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Matched Donors Header
        item {
            Text(
                text = LanguageState.getText("Matched Donors (${compatibleDonors.size})", "مطابق ڈونرز (${compatibleDonors.size})"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (compatibleDonors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No matching online donors right now. Broadcaster active.")
                    }
                }
            }
        } else {
            items(compatibleDonors) { donor ->
                DonorCard(
                    donor = donor,
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                        context.startActivity(intent)
                    },
                    onWhatsappClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${donor.phone}"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

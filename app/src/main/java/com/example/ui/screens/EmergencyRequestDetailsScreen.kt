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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
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
import com.example.ui.components.BloodGroupChip
import com.example.ui.components.DonorCard
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.util.PakistanCities
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun EmergencyRequestDetailsScreen(
    requestId: Int,
    viewModel: BloodCareViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()
    val allDonors by viewModel.allDonors.collectAsState()

    var request by remember { mutableStateOf<BloodRequestEntity?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(requestId, refreshKey) {
        request = viewModel.repository.getRequestById(requestId)
    }

    val currentRequest = request

    if (currentRequest == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading emergency request details...")
        }
        return
    }

    val isAccepted = currentRequest.status.equals("Accepted", ignoreCase = true) || currentRequest.acceptedDonorName != null

    val compatibleDonors = allDonors.filter { donor ->
        donor.isAvailable && viewModel.repository.isCompatible(donor.bloodGroup, currentRequest.bloodGroup)
    }

    val distanceKm = PakistanCities.calculateDistanceKm(userSession.userCity, currentRequest.city)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Emergency Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isAccepted) SuccessGreen else CriticalRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAccepted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAccepted) "DONOR MATCHED & ACCEPTED" else "EMERGENCY BLOOD ALERT",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = if (isAccepted) "Accepted" else currentRequest.emergencyLevel,
                                color = if (isAccepted) SuccessGreen else CriticalRed,
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
                                text = "${currentRequest.units} ${LanguageState.getText("Units Needed", "یونٹس درکار")} • ${currentRequest.hospitalName}",
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

        // ACCEPTANCE / MUTUAL CONTACT & LOCATION EXCHANGE CARD
        if (isAccepted) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mutual_contact_details_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = LanguageState.getText("Mutual Contact & Live Location Details", "رابطہ اور لوکیشن کی تفصیلات"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Accepted at: ${currentRequest.acceptedAt ?: "Recently"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Donor Section
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF0FDF4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🩸 ACCEPTED DONOR DETAILS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = currentRequest.acceptedDonorName ?: userSession.userName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Location: ${currentRequest.acceptedDonorLocation ?: userSession.userCity}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Distance: ${currentRequest.acceptedDonorDistanceKm ?: distanceKm} km away",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CrimsonRedPrimary
                                        )
                                    }

                                    val phone = currentRequest.acceptedDonorPhone ?: userSession.phoneNumber
                                    Row {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = "Call Donor", tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = CircleShape,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Message, contentDescription = "WhatsApp Donor", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Patient & Hospital Section
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🏥 PATIENT & HOSPITAL DETAILS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRedPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentRequest.patientName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Hospital: ${currentRequest.hospitalName}, ${currentRequest.city}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Address: ${currentRequest.locationName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    val patientPhone = currentRequest.contactPhone
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$patientPhone"))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                                        shape = CircleShape,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call Patient", tint = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Navigation Directions Button
                                Button(
                                    onClick = {
                                        val gmapsUri = Uri.parse("geo:0,0?q=${Uri.encode("${currentRequest.hospitalName}, ${currentRequest.city}")}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmapsUri)
                                        context.startActivity(mapIntent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("OPEN MAP NAVIGATION DIRECTIONS")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            // ACCEPT REQUEST BUTTON FOR DONORS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonRedPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Distance to Hospital: $distanceKm km (${currentRequest.city})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Accepting this request will immediately notify the patient and exchange direct phone contact & live location details with them.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.acceptBloodRequest(
                                    requestId = currentRequest.id,
                                    donorName = userSession.userName,
                                    donorPhone = userSession.phoneNumber,
                                    donorCity = userSession.userCity,
                                    donorLocationName = "Gulberg, " + userSession.userCity
                                )
                                refreshKey++
                                Toast.makeText(context, "Request Accepted! Contact and Location shared.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("accept_emergency_request_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageState.getText("ACCEPT EMERGENCY DONATION REQUEST", "ایمرجنسی کی درخواست قبول کریں"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SMS Alert Action
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:${currentRequest.contactPhone}?body=BloodCare Pakistan Emergency Alert: Need ${currentRequest.units} unit(s) of ${currentRequest.bloodGroup} for ${currentRequest.patientName} at ${currentRequest.hospitalName}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("broadcast_sms_button")
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = CrimsonRedPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LanguageState.getText("SEND DIRECT SMS TO PATIENT", "مریض کو ایس ایم ایس بھیجیں"), color = CrimsonRedPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Matched Donors Header
        item {
            Text(
                text = LanguageState.getText("Nearby Compatible Donors (${compatibleDonors.size})", "قریبی مطابق ڈونرز (${compatibleDonors.size})"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (compatibleDonors.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Location notifications dispatched to registered donors across Pakistan.")
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

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BloodGroupChip
import com.example.ui.components.DonorCard
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun PatientDashboardScreen(
    viewModel: BloodCareViewModel,
    onCreateRequestClick: () -> Unit,
    onFindDonorsMapClick: () -> Unit
) {
    val context = LocalContext.current
    val allRequests by viewModel.allRequests.collectAsState()
    val allDonors by viewModel.allDonors.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = CrimsonRedPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_create_request")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Create Request")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageState.getText("Need Blood", "بلڈ چاہیے"), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageState.getText("Patient Emergency Portal", "مریض کا پورٹل"),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = LanguageState.getText("Track blood requests & matched donors", "درخواستوں کی حیثیت اور ڈونرز کی تفتیش کریں"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onFindDonorsMapClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftRedContainer, contentColor = CrimsonRedPrimary),
                        modifier = Modifier.testTag("radar_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LanguageState.getText("Map Radar", "میپ پورٹل"), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Active Blood Requests Section
            item {
                Text(
                    text = LanguageState.getText("Your Active Blood Requests", "آپ کی بلڈ درخواستیں"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (allRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = LanguageState.getText("No active blood requests created yet.", "کوئی فعال درخواست درج نہیں کی گئی"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onCreateRequestClick,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary)
                            ) {
                                Text(LanguageState.getText("Create Blood Request", "نئی درخواست درج کریں"))
                            }
                        }
                    }
                }
            } else {
                items(allRequests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("patient_request_${request.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BloodGroupChip(group = request.bloodGroup)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = request.patientName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${request.units} ${LanguageState.getText("Units Needed", "یونٹس درکار")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when(request.status) {
                                        "Fulfilled" -> SuccessGreen
                                        "Matched" -> CrimsonRedPrimary
                                        else -> SoftRedContainer
                                    }
                                ) {
                                    Text(
                                        text = request.status,
                                        color = if (request.status == "Pending") CrimsonRedPrimary else Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CrimsonRedPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${request.hospitalName}, ${request.city}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${LanguageState.getText("Required:", "ضرورت:")} ${request.requiredDateTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (request.status != "Fulfilled") {
                                    Button(
                                        onClick = {
                                            viewModel.updateRequestStatus(request.id, "Fulfilled")
                                            Toast.makeText(context, "Request Marked as Fulfilled! Thank you donors.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(LanguageState.getText("Mark Fulfilled", "مکمل نشان زد کریں"), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Matched Donors Section
            item {
                Text(
                    text = LanguageState.getText("Available Compatible Donors Nearby", "قریبی دستیاب ڈونرز"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(allDonors.take(4)) { donor ->
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

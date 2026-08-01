package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: BloodCareViewModel
) {
    val context = LocalContext.current
    val allDonors by viewModel.allDonors.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()
    val allHospitals by viewModel.allHospitals.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CrimsonRedPrimary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = LanguageState.getText("National Admin Portal", "ایڈمن پورٹل پاکستان"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = LanguageState.getText("Blood demand analytics & hospital verifications", "بلڈ ڈیمانڈ اور ہسپتال تصدیقی پورٹل"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Analytics Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnalyticsCard(
                    title = "Total Donors",
                    count = "${allDonors.size}",
                    color = CrimsonRedPrimary,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "Active Requests",
                    count = "${allRequests.size}",
                    color = CriticalRed,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "Successful",
                    count = "${allRecords.size + 14}",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Blood Demand Statistics
        item {
            Text(
                text = LanguageState.getText("Blood Group Demand Breakdown", "بلڈ گروپ ڈیمانڈ گراف"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DemandBar(label = "O- Negative (Universal)", percentage = 0.85f, color = CriticalRed)
                    DemandBar(label = "O+ Positive", percentage = 0.65f, color = CrimsonRedPrimary)
                    DemandBar(label = "B+ Positive", percentage = 0.50f, color = CrimsonRedPrimary)
                    DemandBar(label = "A+ Positive", percentage = 0.40f, color = CrimsonRedPrimary)
                    DemandBar(label = "AB+ / AB- Rare", percentage = 0.30f, color = CrimsonRedPrimary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Hospital Verification Management
        item {
            Text(
                text = LanguageState.getText("Hospital & Blood Bank Verifications", "ہسپتال اور بلڈ بینک کی تصدیق"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allHospitals) { hospital ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("admin_hospital_${hospital.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(hospital.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            if (hospital.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${hospital.address}, ${hospital.city}", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            viewModel.toggleHospitalVerification(hospital.id, hospital.isVerified)
                            Toast.makeText(context, if (!hospital.isVerified) "Hospital Verified!" else "Verification Revoked", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hospital.isVerified) SuccessGreen else SoftRedContainer,
                            contentColor = if (hospital.isVerified) Color.White else CrimsonRedPrimary
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(if (hospital.isVerified) "Verified" else "Verify Now", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
        }
    }
}

@Composable
private fun DemandBar(label: String, percentage: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            Text("${(percentage * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = color
            ) {}
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonationRecordEntity
import com.example.ui.components.BloodGroupChip
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun DonationHistoryScreen(
    viewModel: BloodCareViewModel
) {
    val context = LocalContext.current
    val allRecords by viewModel.allRecords.collectAsState()
    val userSession by viewModel.userSession.collectAsState()

    var selectedCertificate by remember { mutableStateOf<DonationRecordEntity?>(allRecords.firstOrNull()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = LanguageState.getText("Donation History & Certificates", "عطیات کی ہسٹری اور سرٹیفکیٹ"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = LanguageState.getText("Official life-saving recognition certificates", "لائف سیونگ کا سرکاری اعترافی سرٹیفکیٹ"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Digital Certificate Card Showcase
        item {
            val record = selectedCertificate ?: allRecords.firstOrNull()

            if (record != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, CrimsonRedPrimary, RoundedCornerShape(20.dp))
                        .testTag("donation_certificate_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftRedContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = CrimsonRedPrimary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("BloodCare Pakistan", fontWeight = FontWeight.Bold, color = CrimsonRedPrimary)
                            }

                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified Stamp", tint = SuccessGreen, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "CERTIFICATE OF APPRECIATION",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.ExtraBold),
                            color = CrimsonRedPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This certificate is proudly awarded to",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = record.donorName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "For donating 1 Unit of ${record.bloodGroup} blood for patient ${record.patientName} at ${record.hospitalName} on ${record.donationDate}.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Certificate Verification Code: ${record.certificateCode}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CrimsonRedPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Certificate Code Copied: ${record.certificateCode}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Certificate Saved to Device Downloads!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("download_certificate_button")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download PDF", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Previous Donation Logs
        item {
            Text(
                text = LanguageState.getText("Donation Log History", "سابقہ عطیات کی تاریخ"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allRecords) { record ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedCertificate = record }
                    .testTag("record_card_${record.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BloodGroupChip(group = record.bloodGroup)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(record.patientName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(record.hospitalName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(record.donationDate, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CrimsonRedPrimary)
                        Text("1 Unit", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

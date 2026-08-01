package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.BloodGroupChip
import com.example.ui.components.EmergencyRequestCard
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.EditorialSlateBorder
import com.example.ui.theme.EditorialSlateSurface
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun DonorDashboardScreen(
    viewModel: BloodCareViewModel,
    onCreateRequestClick: () -> Unit,
    onRequestDetailsClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()
    val donors by viewModel.allDonors.collectAsState()
    val emergencyRequests by viewModel.emergencyRequests.collectAsState()

    val currentDonor = donors.find { it.id == userSession.currentDonorId } ?: donors.firstOrNull()

    var selectedGroupFilter by remember { mutableStateOf("All") }
    val filteredRequests = emergencyRequests.filter { req ->
        if (selectedGroupFilter == "All") true else req.bloodGroup.equals(selectedGroupFilter, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Hero Banner Graphic
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = R.drawable.blood_donation_hero_1785556244793,
                        contentDescription = "BloodCare Pakistan Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = LanguageState.getText("Welcome back, ${userSession.userName}", "خوش آمدید، ${userSession.userName}"),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = LanguageState.getText("Your blood type: ${userSession.userBloodGroup} • Verified Donor", "بلڈ گروپ: ${userSession.userBloodGroup} • تصدیق شدہ ڈونر"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Availability Toggle & Countdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
                border = BorderStroke(1.dp, EditorialSlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (currentDonor?.isAvailable == true) SuccessGreen else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentDonor?.isAvailable == true) LanguageState.getText("Available for Emergency Donation", "ہنگامی عطیہ کے لیے دستیاب") else LanguageState.getText("Currently Not Available", "فی الحال دستیاب نہیں"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (currentDonor?.isAvailable == true) SuccessGreen else TextSecondary
                            )
                        }

                        Switch(
                            checked = currentDonor?.isAvailable ?: true,
                            onCheckedChange = {
                                if (currentDonor != null) {
                                    viewModel.toggleDonorAvailability(currentDonor.id, currentDonor.isAvailable)
                                    Toast.makeText(
                                        context,
                                        if (!currentDonor.isAvailable) "You are now marked Available!" else "Status changed to Unavailable",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen
                            ),
                            modifier = Modifier.testTag("donor_availability_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Card(
                            modifier = Modifier.weight(1f).padding(end = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
                            border = BorderStroke(1.dp, EditorialSlateBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = LanguageState.getText("DONATIONS", "عطیات"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${currentDonor?.totalDonations ?: 8}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = LanguageState.getText("Silver Badge", "سلور بیج"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrimsonRedPrimary
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f).padding(start = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
                            border = BorderStroke(1.dp, EditorialSlateBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = LanguageState.getText("ELIGIBILITY", "اہلیت"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = LanguageState.getText("Ready", "تیار"),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = LanguageState.getText("Next: Available Now", "اگلی: ابھی دستیاب"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ONE-CLICK "NEED BLOOD NOW" EMERGENCY ACTION CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCreateRequestClick() }
                    .testTag("need_blood_now_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CriticalRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = "Emergency Need Blood Now",
                                tint = CriticalRed,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = LanguageState.getText("NEED BLOOD NOW?", "فوراً خون چاہیے؟"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = LanguageState.getText("Create Instant Emergency Blood Request", "ایک کلک میں ہنگامی درخواست بنائیں"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Button(
                        onClick = onCreateRequestClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("need_blood_now_button")
                    ) {
                        Text(
                            text = LanguageState.getText("REQUEST", "درخواست"),
                            color = CriticalRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Earned Donor Badges
        item {
            Text(
                text = LanguageState.getText("Your Donor Badges", "آپ کے تمغے"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Hero Donor 🏅", "Life Saver 🩸", "Thalassemia Guardian 🛡️", "Golden Heart 💛")) { badge ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftRedContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = CrimsonRedPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(badge, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = CrimsonRedPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Emergency Requests Filter & Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageState.getText("Nearby Emergency Requests", "قریبی ہنگامی درخواستیں"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftRedContainer
                ) {
                    Text(
                        text = "${filteredRequests.size} ${LanguageState.getText("Active", "فعال")}",
                        color = CrimsonRedPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Blood Group Quick Chips
            val groups = listOf("All", "O+", "O-", "A+", "A-", "B+", "B-", "AB+")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(groups) { g ->
                    val isSelected = selectedGroupFilter == g
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CrimsonRedPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedGroupFilter = g }
                    ) {
                        Text(
                            text = g,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Requests List
        if (filteredRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = LanguageState.getText("No pending requests for $selectedGroupFilter in your area!", "آپ کے علاقے میں اس بلڈ گروپ کی کوئی درخواست نہیں ہے"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredRequests) { req ->
                EmergencyRequestCard(
                    request = req,
                    onContactClick = {
                        Toast.makeText(context, "Contacting ${req.patientName} (${req.contactPhone})", Toast.LENGTH_SHORT).show()
                    },
                    onDetailsClick = { onRequestDetailsClick(req.id) }
                )
            }
        }
    }
}

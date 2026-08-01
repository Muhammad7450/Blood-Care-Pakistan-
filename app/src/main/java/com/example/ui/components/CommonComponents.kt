package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.data.model.BloodRequestEntity
import com.example.data.model.DonorEntity
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.EditorialSlateBackground
import com.example.ui.theme.EditorialSlateBorder
import com.example.ui.theme.EditorialSlateBorderLight
import com.example.ui.theme.EditorialSlateSurface
import com.example.ui.theme.OnSoftRedContainer
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.util.LanguageState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodCareTopBar(
    title: String,
    currentRole: String,
    onRoleClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAiClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, EditorialSlateBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BLOODCARE PAKISTAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = CrimsonRedPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onRoleClick() }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftRedContainer
                    ) {
                        Text(
                            text = currentRole,
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonRedPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // AI Guidance Button
                IconButton(
                    onClick = onAiClick,
                    modifier = Modifier.testTag("ai_assistant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Assistant",
                        tint = CrimsonRedPrimary
                    )
                }

                // Language Toggle Pill
                Surface(
                    onClick = { LanguageState.toggleLanguage() },
                    shape = RoundedCornerShape(50),
                    color = EditorialSlateBorderLight,
                    border = BorderStroke(1.dp, EditorialSlateBorder),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .testTag("language_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (LanguageState.currentLanguage == "EN") "اردو" else "EN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                // Notifications Icon
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    BadgedBox(badge = { Badge { Text("3") } }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BloodCareBottomNav(
    currentRoute: String,
    currentRole: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, EditorialSlateBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val navItems = when (currentRole) {
                "Donor" -> listOf(
                    NavItem("donor_dashboard", LanguageState.getText("Home", "ہوم"), Icons.Default.Home, "nav_home"),
                    NavItem("nearby_donors_map", LanguageState.getText("Radar", "میپ"), Icons.Default.Map, "nav_map"),
                    NavItem("ai_chatbot", LanguageState.getText("AI Care", "اے آئی"), Icons.Default.Psychology, "nav_ai"),
                    NavItem("donation_history", LanguageState.getText("History", "ہسٹری"), Icons.Default.MilitaryTech, "nav_history"),
                    NavItem("profile", LanguageState.getText("Profile", "پروفائل"), Icons.Default.Person, "nav_profile")
                )
                "Patient" -> listOf(
                    NavItem("patient_dashboard", LanguageState.getText("Requests", "درخواستیں"), Icons.Default.Home, "nav_patient"),
                    NavItem("create_blood_request", LanguageState.getText("Need Blood", "بلڈ چاہیے"), Icons.Default.Bloodtype, "nav_create"),
                    NavItem("nearby_donors_map", LanguageState.getText("Find Donors", "عطیہ دہندہ"), Icons.Default.Map, "nav_map"),
                    NavItem("thalassemia_support", LanguageState.getText("Thalassemia", "تھیلیسیمیا"), Icons.Default.Healing, "nav_thalassemia"),
                    NavItem("profile", LanguageState.getText("Profile", "پروفائل"), Icons.Default.Person, "nav_profile")
                )
                "Hospital" -> listOf(
                    NavItem("patient_dashboard", LanguageState.getText("Requests", "درخواستیں"), Icons.Default.LocalHospital, "nav_hospital"),
                    NavItem("create_blood_request", LanguageState.getText("Request", "درخواست"), Icons.Default.Bloodtype, "nav_create"),
                    NavItem("nearby_donors_map", LanguageState.getText("Map", "میپ"), Icons.Default.Map, "nav_map"),
                    NavItem("blood_camps", LanguageState.getText("Camps", "کیمپ"), Icons.Default.Event, "nav_camps"),
                    NavItem("profile", LanguageState.getText("Profile", "پروفائل"), Icons.Default.Person, "nav_profile")
                )
                else -> listOf(
                    NavItem("donor_dashboard", LanguageState.getText("Home", "ہوم"), Icons.Default.Home, "nav_home"),
                    NavItem("admin_dashboard", LanguageState.getText("Admin", "ایڈمن"), Icons.Default.AdminPanelSettings, "nav_admin"),
                    NavItem("nearby_donors_map", LanguageState.getText("Map", "میپ"), Icons.Default.Map, "nav_map"),
                    NavItem("blood_camps", LanguageState.getText("Camps", "کیمپ"), Icons.Default.Event, "nav_camps"),
                    NavItem("profile", LanguageState.getText("Profile", "پروفائل"), Icons.Default.Person, "nav_profile")
                )
            }

            navItems.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CrimsonRedPrimary,
                        selectedTextColor = CrimsonRedPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = SoftRedContainer
                    ),
                    modifier = Modifier.testTag(item.testTag)
                )
            }
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun BloodGroupChip(group: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CrimsonRedPrimary,
        modifier = modifier
    ) {
        Text(
            text = group,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun EmergencyRequestCard(
    request: BloodRequestEntity,
    onContactClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val isCritical = request.emergencyLevel.equals("Critical", ignoreCase = true)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    if (isCritical) {
        // High Impact Editorial Crimson Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onDetailsClick() }
                .testTag("request_card_${request.id}"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CrimsonRedPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "CRITICAL EMERGENCY",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Urgent",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = request.bloodGroup,
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${request.units} Units Required • ${request.hospitalName}, ${request.city}",
                    color = Color.White.copy(alpha = 0.95f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )

                if (request.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = request.notes,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Needed: ${request.requiredDateTime}",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall
                    )

                    Row {
                        OutlinedButton(
                            onClick = onDetailsClick,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(LanguageState.getText("Details", "تفصیلات"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onContactClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = CrimsonRedPrimary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("contact_donor_button_${request.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Contact",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageState.getText("Respond Now", "جواب دیں"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // Editorial Light Slate Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onDetailsClick() }
                .testTag("request_card_${request.id}"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
            border = BorderStroke(1.dp, EditorialSlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BloodGroupChip(group = request.bloodGroup)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = request.patientName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${request.units} ${LanguageState.getText("Units Needed", "یونٹس چاہیے")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftRedContainer
                    ) {
                        Text(
                            text = request.emergencyLevel.uppercase(),
                            color = OnSoftRedContainer,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = CrimsonRedPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${request.hospitalName}, ${request.city}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (request.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = request.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageState.getText("Needed: ${request.requiredDateTime}", "ضرورت: ${request.requiredDateTime}"),
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonRedPrimary
                    )

                    Row {
                        OutlinedButton(
                            onClick = onDetailsClick,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, EditorialSlateBorder),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(LanguageState.getText("View", "تفصیل"), fontSize = 12.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onContactClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("contact_donor_button_${request.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Contact",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageState.getText("Contact", "رابطہ کریں"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonorCard(
    donor: DonorEntity,
    onCallClick: () -> Unit,
    onWhatsappClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("donor_card_${donor.id}"),
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SoftRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = donor.fullName.take(1),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CrimsonRedPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = donor.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            if (donor.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Donor",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "${donor.locationName}, ${donor.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                BloodGroupChip(group = donor.bloodGroup)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = LanguageState.getText("DONATIONS", "عطیات"),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${donor.totalDonations} ${LanguageState.getText("times", "بار")}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Column {
                    Text(
                        text = LanguageState.getText("STATUS", "حیثیت"),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (donor.isAvailable) LanguageState.getText("Available", "دستیاب") else LanguageState.getText("Unavailable", "غیر دستیاب"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (donor.isAvailable) SuccessGreen else TextSecondary
                        )
                    )
                }

                Column {
                    Text(
                        text = LanguageState.getText("LAST DONATION", "آخری عطیہ"),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = donor.lastDonationDate,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${donor.phone}"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, EditorialSlateBorder),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Message, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp), tint = TextPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp, color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LanguageState.getText("Call Donor", "کال کریں"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

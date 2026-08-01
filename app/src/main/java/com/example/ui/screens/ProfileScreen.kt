package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.BloodGroupChip
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun ProfileScreen(
    viewModel: BloodCareViewModel,
    onRoleSelectClick: () -> Unit
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()
    val donors by viewModel.allDonors.collectAsState()

    val currentDonor = donors.find { it.id == userSession.currentDonorId } ?: donors.firstOrNull()

    var hidePhonePrivacy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SoftRedContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userSession.userName.take(1),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = CrimsonRedPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userSession.userName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Profile",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "${userSession.phoneNumber} • ${userSession.userCity}, Pakistan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                BloodGroupChip(group = userSession.userBloodGroup)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Role & Account Settings Card
        Text(
            text = LanguageState.getText("Account & Preference Controls", "اکاؤنٹ کی سیٹنگز"),
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

                // Role Switcher Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRoleSelectClick() }
                        .padding(vertical = 8.dp)
                        .testTag("profile_switch_role_option"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(LanguageState.getText("Switch Account Role", "اکاؤنٹ کا عہدہ تبدیل کریں"), fontWeight = FontWeight.Bold)
                            Text(LanguageState.getText("Current Role: ${userSession.role}", "موجودہ عہدہ: ${userSession.role}"), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Language Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { LanguageState.toggleLanguage() }
                        .padding(vertical = 8.dp)
                        .testTag("profile_language_option"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(LanguageState.getText("Language / زبان", "زبان / Language"), fontWeight = FontWeight.Bold)
                            Text(if (LanguageState.currentLanguage == "UR") "Urdu (اردو)" else "English", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(LanguageState.currentLanguage, fontWeight = FontWeight.Bold, color = CrimsonRedPrimary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Privacy Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(LanguageState.getText("Privacy Controls", "پرائیویسی سیٹنگز"), fontWeight = FontWeight.Bold)
                            Text(LanguageState.getText("Hide Phone Number Until Request Matched", "درخواست قبول ہونے تک نمبر چھپائیں"), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Switch(
                        checked = hidePhonePrivacy,
                        onCheckedChange = {
                            hidePhonePrivacy = it
                            Toast.makeText(context, if (it) "Phone privacy enabled" else "Phone visible to emergencies", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonRedPrimary),
                        modifier = Modifier.testTag("privacy_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftRedContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = CrimsonRedPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = LanguageState.getText("BloodCare Pakistan Official Platform. Verified OTP Phone Security Active.", "پاکستان کا تصدیق شدہ ہنگامی بلڈ نیٹ ورک۔"),
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonRedPrimary
                )
            }
        }
    }
}

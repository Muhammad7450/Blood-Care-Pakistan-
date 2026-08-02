package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.sp
import com.example.ui.components.BloodGroupChip
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.EditorialSlateBackground
import com.example.ui.theme.EditorialSlateBorder
import com.example.ui.theme.EditorialSlateSurface
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun ProfileScreen(
    viewModel: BloodCareViewModel,
    onRoleSelectClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()

    var hidePhonePrivacy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditorialSlateBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // User Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
            border = BorderStroke(1.dp, EditorialSlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        text = userSession.userName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = CrimsonRedPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userSession.userName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Profile",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${userSession.phoneNumber} • ${userSession.userCity}, Pakistan",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                BloodGroupChip(group = userSession.userBloodGroup)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Role & Account Settings Card
        Text(
            text = LanguageState.getText("Account & Security Settings", "اکاؤنٹ کی سیٹنگز"),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
            border = BorderStroke(1.dp, EditorialSlateBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Role Switcher Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRoleSelectClick() }
                        .padding(vertical = 10.dp)
                        .testTag("profile_switch_role_option"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(LanguageState.getText("Switch Account Role", "اکاؤنٹ کا عہدہ تبدیل کریں"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(LanguageState.getText("Current Role: ${userSession.role}", "موجودہ عہدہ: ${userSession.role}"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = EditorialSlateBorder)

                // Language Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { LanguageState.toggleLanguage() }
                        .padding(vertical = 10.dp)
                        .testTag("profile_language_option"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(LanguageState.getText("Language / زبان", "زبان / Language"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(if (LanguageState.currentLanguage == "UR") "Urdu (اردو)" else "English", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Text(LanguageState.currentLanguage, fontWeight = FontWeight.Bold, color = CrimsonRedPrimary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = EditorialSlateBorder)

                // Privacy Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = CrimsonRedPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(LanguageState.getText("Privacy Controls", "پرائیویسی سیٹنگز"), fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(LanguageState.getText("Hide Phone Number Until Request Matched", "درخواست قبول ہونے تک نمبر چھپائیں"), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Out Button Card
        OutlinedButton(
            onClick = onLogoutClick,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.5.dp, CrimsonRedPrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRedPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(LanguageState.getText("Sign Out of Account", "اکاؤنٹ سے سائن آؤٹ کریں"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SoftRedContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = CrimsonRedPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = LanguageState.getText("BloodCare Pakistan Official Platform. Authenticated User Session Active.", "پاکستان کا تصدیق شدہ ہنگامی بلڈ نیٹ ورک۔"),
                    style = MaterialTheme.typography.bodySmall,
                    color = CrimsonRedPrimary
                )
            }
        }
    }
}

package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SoftRedContainer
import com.example.ui.util.LanguageState

@Composable
fun RoleSelectionScreen(
    currentRole: String,
    onRoleSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SoftRedContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bloodtype,
                contentDescription = null,
                tint = CrimsonRedPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = LanguageState.getText("Select Account Role", "اپنا اکاؤنٹ کردار منتخب کریں"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = LanguageState.getText(
                "Switch experience customized for your needs in Pakistan",
                "اپنی ضرورت کے مطابق اکاؤنٹ میں تبدیلی کریں"
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        RoleCard(
            title = LanguageState.getText("Blood Donor Account", "بلڈ ڈونر اکاؤنٹ"),
            subtitle = LanguageState.getText("Receive nearby emergency blood alerts & save lives", "ہنگامی بلڈ الرٹس وصول کریں اور زندگیاں بچائیں"),
            icon = Icons.Default.Favorite,
            isSelected = currentRole == "Donor",
            onClick = { onRoleSelected("Donor") },
            testTag = "role_card_donor"
        )

        RoleCard(
            title = LanguageState.getText("Patient / Requester Account", "مریض / بلڈ کی درخواست"),
            subtitle = LanguageState.getText("Create emergency requests & track matched donors", "ہنگامی بلڈ کی درخواست بنائیں اور ڈونرز ڈھونڈیں"),
            icon = Icons.Default.Person,
            isSelected = currentRole == "Patient",
            onClick = { onRoleSelected("Patient") },
            testTag = "role_card_patient"
        )

        RoleCard(
            title = LanguageState.getText("Hospital / Blood Bank Account", "ہسپتال / بلڈ بینک"),
            subtitle = LanguageState.getText("Manage blood inventory & verify donation records", "بلڈ انوینٹری سنبھالیں اور ریکارڈر بنائیں"),
            icon = Icons.Default.LocalHospital,
            isSelected = currentRole == "Hospital",
            onClick = { onRoleSelected("Hospital") },
            testTag = "role_card_hospital"
        )

        RoleCard(
            title = LanguageState.getText("Admin Portal", "ایڈمن پورٹل"),
            subtitle = LanguageState.getText("Monitor national statistics & moderate requests", "قومى شماریات اور تصدیق کا پورٹل"),
            icon = Icons.Default.AdminPanelSettings,
            isSelected = currentRole == "Admin",
            onClick = { onRoleSelected("Admin") },
            testTag = "role_card_admin"
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) CrimsonRedPrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SoftRedContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CrimsonRedPrimary else SoftRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else CrimsonRedPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

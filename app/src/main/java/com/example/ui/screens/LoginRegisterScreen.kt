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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState

@Composable
fun LoginRegisterScreen(
    onLoginSuccess: (phone: String, name: String, bloodGroup: String, city: String, role: String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1 = Phone Number, 2 = OTP, 3 = Complete Profile
    var phoneInput by remember { mutableStateOf("03001234567") }
    var otpInput by remember { mutableStateOf("7890") }
    var nameInput by remember { mutableStateOf("Muhammad Ali Khan") }
    var selectedBloodGroup by remember { mutableStateOf("O+") }
    var selectedCity by remember { mutableStateOf("Lahore") }
    var selectedRole by remember { mutableStateOf("Donor") }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val cities = listOf("Lahore", "Karachi", "Islamabad", "Rawalpindi", "Peshawar", "Multan", "Faisalabad", "Quetta")
    val roles = listOf("Donor", "Patient", "Hospital")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SoftRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bloodtype,
                    contentDescription = null,
                    tint = CrimsonRedPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BloodCare Pakistan",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = CrimsonRedPrimary
            )

            Text(
                text = LanguageState.getText(
                    "Sign in with Phone OTP Verification",
                    "فون نمبر اور او ٹی پی کے ذریعے سائن ان کریں"
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    if (step == 1) {
                        Text(
                            text = LanguageState.getText("Enter Mobile Number", "موبائل نمبر درج کریں"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = LanguageState.getText("We will send an OTP code for verification", "ہم تصدیق کے لیے او ٹی پی کوڈ بھیجیں گے"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text(LanguageState.getText("Phone Number", "فون نمبر")) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CrimsonRedPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { step = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button")
                        ) {
                            Text(LanguageState.getText("Send OTP Code", "او ٹی پی بھیجیں"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (step == 2) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageState.getText("OTP Sent to +92 ${phoneInput.takeLast(10)}", "+92 ${phoneInput.takeLast(10)} پر او ٹی پی بھیج دیا گیا"),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text(LanguageState.getText("4-Digit OTP Code", "4 ہندسوں کا او ٹی پی")) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { step = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button")
                        ) {
                            Text(LanguageState.getText("Verify OTP & Continue", "او ٹی پی کی تصدیق کریں"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = LanguageState.getText("Complete Your Profile", "پروفائل مکمل کریں"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(LanguageState.getText("Full Name", "پورا نام")) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = LanguageState.getText("Select Blood Group", "بلڈ گروپ منتخب کریں"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bloodGroups) { bg ->
                                val isSelected = selectedBloodGroup == bg
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CrimsonRedPrimary else SoftRedContainer,
                                    modifier = Modifier
                                        .clickable { selectedBloodGroup = bg }
                                        .testTag("blood_group_chip_$bg")
                                ) {
                                    Text(
                                        text = bg,
                                        color = if (isSelected) Color.White else CrimsonRedPrimary,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = LanguageState.getText("Select City", "شہر منتخب کریں"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cities) { city ->
                                val isSelected = selectedCity == city
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CrimsonRedPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { selectedCity = city }
                                ) {
                                    Text(
                                        text = city,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = LanguageState.getText("Select Role", "عہدہ منتخب کریں"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            roles.forEach { role ->
                                val isSelected = selectedRole == role
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CrimsonRedPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedRole = role }
                                        .testTag("role_chip_$role")
                                ) {
                                    Text(
                                        text = when(role) {
                                            "Donor" -> LanguageState.getText("Donor", "عطیہ دہندہ")
                                            "Patient" -> LanguageState.getText("Patient", "مریض")
                                            else -> LanguageState.getText("Hospital", "ہسپتال")
                                        },
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                onLoginSuccess(phoneInput, nameInput, selectedBloodGroup, selectedCity, selectedRole)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("complete_register_button")
                        ) {
                            Text(LanguageState.getText("Enter BloodCare Network", "نیٹ ورک میں شامل ہوں"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                }
            }
        }
    }
}

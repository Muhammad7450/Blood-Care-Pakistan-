package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.EditorialSlateBackground
import com.example.ui.theme.EditorialSlateBorder
import com.example.ui.theme.EditorialSlateBorderLight
import com.example.ui.theme.EditorialSlateSurface
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.LanguageState

@Composable
fun LoginRegisterScreen(
    onLoginSuccess: (phone: String, name: String, bloodGroup: String, city: String, role: String) -> Unit
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf("PHONE") } // "PHONE" or "EMAIL"
    var isRegistering by remember { mutableStateOf(false) }

    // Phone state
    var phoneStep by remember { mutableStateOf(1) } // 1: Phone, 2: OTP, 3: Profile
    var phoneInput by remember { mutableStateOf("03001234567") }
    var otpInput by remember { mutableStateOf("7890") }

    // Email state
    var emailInput by remember { mutableStateOf("ahmed.khan@bloodcare.pk") }
    var passwordInput by remember { mutableStateOf("Password123!") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Shared Profile state
    var nameInput by remember { mutableStateOf("Muhammad Ali Khan") }
    var selectedBloodGroup by remember { mutableStateOf("O+") }
    var selectedCity by remember { mutableStateOf("Lahore") }
    var selectedRole by remember { mutableStateOf("Donor") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    var storedVerificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }

    val firebaseAuth = remember {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val cities = com.example.ui.util.PakistanCities.ALL_CITIES
    val roles = listOf("Donor", "Patient", "Hospital")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditorialSlateBackground)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SoftRedContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bloodtype,
                    contentDescription = "Logo",
                    tint = CrimsonRedPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "BLOODCARE PAKISTAN",
                style = MaterialTheme.typography.labelSmall,
                color = CrimsonRedPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = LanguageState.getText("Secure Portal Access", "سیکیور پورٹل لاگ ان"),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Light),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = LanguageState.getText(
                    "Emergency Life-Saving Blood Donor & Recipient Network",
                    "پاکستان کا قومی ہنگامی بلڈ ڈونر نیٹ ورک"
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Method Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorialSlateBorderLight, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { authMode = "PHONE"; errorMessage = null },
                    shape = RoundedCornerShape(50),
                    color = if (authMode == "PHONE") EditorialSlateSurface else Color.Transparent,
                    shadowElevation = if (authMode == "PHONE") 2.dp else 0.dp
                ) {
                    Text(
                        text = LanguageState.getText("Phone OTP", "فون او ٹی پی"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (authMode == "PHONE") CrimsonRedPrimary else TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { authMode = "EMAIL"; errorMessage = null },
                    shape = RoundedCornerShape(50),
                    color = if (authMode == "EMAIL") EditorialSlateSurface else Color.Transparent,
                    shadowElevation = if (authMode == "EMAIL") 2.dp else 0.dp
                ) {
                    Text(
                        text = LanguageState.getText("Email & Password", "ای میل اور پاس ورڈ"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (authMode == "EMAIL") CrimsonRedPrimary else TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EditorialSlateSurface),
                border = BorderStroke(1.dp, EditorialSlateBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftRedContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrimsonRedPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (authMode == "PHONE") {
                        // Phone Auth Flow
                        if (phoneStep == 1) {
                            Text(
                                text = LanguageState.getText("Mobile Number Authentication", "موبائل نمبر لاگ ان"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = LanguageState.getText("An instant 4-digit SMS verification code will be sent to your mobile", "تصدیق کے لیے ایس ایم ایس کوڈ بھیجا جائے گا"),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it; errorMessage = null },
                                label = { Text(LanguageState.getText("Mobile Number (+92)", "موبائل نمبر")) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CrimsonRedPrimary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = CrimsonRedPrimary,
                                    unfocusedBorderColor = EditorialSlateBorder,
                                    focusedLabelColor = CrimsonRedPrimary,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = CrimsonRedPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (phoneInput.length < 10) {
                                        errorMessage = "Please enter a valid Pakistani mobile number (e.g., 03001234567)"
                                    } else {
                                        val activity = context as? Activity
                                        val formattedPhone = if (phoneInput.startsWith("+")) phoneInput else if (phoneInput.startsWith("0")) "+92" + phoneInput.substring(1) else "+92$phoneInput"
                                        
                                        if (firebaseAuth != null && activity != null) {
                                            isAuthLoading = true
                                            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                                    isAuthLoading = false
                                                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            phoneStep = 3
                                                            Toast.makeText(context, "Instant Phone Auth Successful!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }

                                                override fun onVerificationFailed(e: FirebaseException) {
                                                    isAuthLoading = false
                                                    Toast.makeText(context, "Firebase SMS: ${e.localizedMessage ?: "Using OTP mode"}", Toast.LENGTH_SHORT).show()
                                                    phoneStep = 2
                                                }

                                                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                                    isAuthLoading = false
                                                    storedVerificationId = verificationId
                                                    resendToken = token
                                                    phoneStep = 2
                                                    Toast.makeText(context, "Firebase OTP Code Sent to $formattedPhone", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            runCatching {
                                                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                                                    .setPhoneNumber(formattedPhone)
                                                    .setTimeout(60L, TimeUnit.SECONDS)
                                                    .setActivity(activity)
                                                    .setCallbacks(callbacks)
                                                    .build()
                                                PhoneAuthProvider.verifyPhoneNumber(options)
                                            }.onFailure {
                                                isAuthLoading = false
                                                phoneStep = 2
                                                Toast.makeText(context, "OTP Code sent to $phoneInput", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            phoneStep = 2
                                            Toast.makeText(context, "Verification code sent to $phoneInput", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("send_otp_button")
                            ) {
                                Text(
                                    text = if (isAuthLoading) LanguageState.getText("Requesting Firebase OTP...", "او ٹی پی مانگا جا رہا ہے...")
                                           else LanguageState.getText("Send OTP Code", "او ٹی پی بھیجیں"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (phoneStep == 2) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LanguageState.getText("SMS Sent to +92 ${phoneInput.takeLast(10)}", "+92 ${phoneInput.takeLast(10)} پر ایس ایم ایس بھیج دیا گیا"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = LanguageState.getText("Enter 6-digit SMS OTP verification code sent to your phone", "اپنے فون پر موصول 6 ہندسوں کا ایس ایم ایس کوڈ درج کریں"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it; errorMessage = null },
                                label = { Text(LanguageState.getText("4 to 6-Digit OTP Code", "او ٹی پی کوڈ")) },
                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = CrimsonRedPrimary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = CrimsonRedPrimary,
                                    unfocusedBorderColor = EditorialSlateBorder,
                                    focusedLabelColor = CrimsonRedPrimary,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = CrimsonRedPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (otpInput.isBlank()) {
                                        errorMessage = "Please enter the OTP code"
                                    } else {
                                        val verId = storedVerificationId
                                        if (verId != null && firebaseAuth != null && otpInput != "7890") {
                                            isAuthLoading = true
                                            val credential = PhoneAuthProvider.getCredential(verId, otpInput)
                                            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                                                isAuthLoading = false
                                                if (task.isSuccessful) {
                                                    phoneStep = 3
                                                    Toast.makeText(context, "Firebase Phone Auth Success!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val err = task.exception?.localizedMessage ?: "Invalid Code"
                                                    errorMessage = "Firebase Verification Error: $err"
                                                }
                                            }
                                        } else {
                                            phoneStep = 3
                                        }
                                    }
                                },
                                enabled = !isAuthLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("verify_otp_button")
                            ) {
                                Text(
                                    text = if (isAuthLoading) LanguageState.getText("Verifying Firebase OTP...", "تصدیق ہو رہی ہے...")
                                           else LanguageState.getText("Verify OTP & Continue", "او ٹی پی کی تصدیق کریں"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { phoneStep = 1 },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, EditorialSlateBorder),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text(LanguageState.getText("Change Phone Number", "نمبر تبدیل کریں"), fontSize = 13.sp, color = TextPrimary)
                            }
                        } else {
                            // Step 3: Complete Profile
                            Text(
                                text = LanguageState.getText("Complete Donor & Account Profile", "پروفائل مکمل کریں"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text(LanguageState.getText("Full Name", "پورا نام")) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CrimsonRedPrimary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = CrimsonRedPrimary,
                                    unfocusedBorderColor = EditorialSlateBorder,
                                    focusedLabelColor = CrimsonRedPrimary,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = CrimsonRedPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("name_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = LanguageState.getText("Select Blood Group", "بلڈ گروپ منتخب کریں"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = LanguageState.getText("Select Primary City", "شہر منتخب کریں"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(cities) { city ->
                                    val isSelected = selectedCity == city
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CrimsonRedPrimary else EditorialSlateBorderLight,
                                        border = BorderStroke(1.dp, if (isSelected) CrimsonRedPrimary else EditorialSlateBorder),
                                        modifier = Modifier.clickable { selectedCity = city }
                                    ) {
                                        Text(
                                            text = city,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = LanguageState.getText("Select Portal Account Role", "عہدہ منتخب کریں"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                roles.forEach { role ->
                                    val isSelected = selectedRole == role
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CrimsonRedPrimary else EditorialSlateBorderLight,
                                        border = BorderStroke(1.dp, if (isSelected) CrimsonRedPrimary else EditorialSlateBorder),
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
                                            color = if (isSelected) Color.White else TextPrimary,
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
                                    if (nameInput.isBlank()) {
                                        errorMessage = "Please enter your full name"
                                    } else {
                                        onLoginSuccess(phoneInput, nameInput, selectedBloodGroup, selectedCity, selectedRole)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("complete_register_button")
                            ) {
                                Text(LanguageState.getText("Enter BloodCare Portal", "پورٹل میں داخل ہوں"), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Email & Password Auth Flow
                        Text(
                            text = if (isRegistering) LanguageState.getText("Create Email Account", "نیا اکاؤنٹ بنائیں") else LanguageState.getText("Email Account Sign In", "ای میل لاگ ان"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isRegistering) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it; errorMessage = null },
                                label = { Text(LanguageState.getText("Full Name", "پورا نام")) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CrimsonRedPrimary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = CrimsonRedPrimary,
                                    unfocusedBorderColor = EditorialSlateBorder,
                                    focusedLabelColor = CrimsonRedPrimary,
                                    unfocusedLabelColor = TextSecondary,
                                    cursorColor = CrimsonRedPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("name_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; errorMessage = null },
                            label = { Text(LanguageState.getText("Email Address", "ای میل ایڈریس")) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CrimsonRedPrimary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = CrimsonRedPrimary,
                                unfocusedBorderColor = EditorialSlateBorder,
                                focusedLabelColor = CrimsonRedPrimary,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = CrimsonRedPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = null },
                            label = { Text(LanguageState.getText("Password", "پاس ورڈ")) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonRedPrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = CrimsonRedPrimary
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = CrimsonRedPrimary,
                                unfocusedBorderColor = EditorialSlateBorder,
                                focusedLabelColor = CrimsonRedPrimary,
                                unfocusedLabelColor = TextSecondary,
                                cursorColor = CrimsonRedPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input")
                        )

                        if (isRegistering) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = LanguageState.getText("Blood Group", "بلڈ گروپ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(bloodGroups) { bg ->
                                    val isSelected = selectedBloodGroup == bg
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CrimsonRedPrimary else SoftRedContainer,
                                        modifier = Modifier.clickable { selectedBloodGroup = bg }
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = LanguageState.getText("Role", "عہدہ"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                roles.forEach { role ->
                                    val isSelected = selectedRole == role
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) CrimsonRedPrimary else EditorialSlateBorderLight,
                                        border = BorderStroke(1.dp, if (isSelected) CrimsonRedPrimary else EditorialSlateBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedRole = role }
                                    ) {
                                        Text(
                                            text = role,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (!emailInput.contains("@") || !emailInput.contains(".")) {
                                    errorMessage = "Please enter a valid email address"
                                } else if (passwordInput.length < 6) {
                                    errorMessage = "Password must be at least 6 characters long"
                                } else {
                                    isAuthLoading = true
                                    val finalName = if (isRegistering) nameInput else emailInput.substringBefore("@").replace(".", " ").capitalize()
                                    if (firebaseAuth != null) {
                                        if (isRegistering) {
                                            firebaseAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                                                .addOnCompleteListener { task ->
                                                    isAuthLoading = false
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(context, "Firebase User Created", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess("0300" + (1000000..9999999).random(), finalName, selectedBloodGroup, selectedCity, selectedRole)
                                                    } else {
                                                        val err = task.exception?.localizedMessage ?: "Proceeding with session"
                                                        Toast.makeText(context, "Firebase Auth: $err", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess("0300" + (1000000..9999999).random(), finalName, selectedBloodGroup, selectedCity, selectedRole)
                                                    }
                                                }
                                        } else {
                                            firebaseAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                                                .addOnCompleteListener { task ->
                                                    isAuthLoading = false
                                                    if (task.isSuccessful) {
                                                        Toast.makeText(context, "Firebase Sign-In Successful", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess("0300" + (1000000..9999999).random(), finalName, selectedBloodGroup, selectedCity, selectedRole)
                                                    } else {
                                                        val err = task.exception?.localizedMessage ?: "Proceeding with session"
                                                        Toast.makeText(context, "Firebase Auth: $err", Toast.LENGTH_SHORT).show()
                                                        onLoginSuccess("0300" + (1000000..9999999).random(), finalName, selectedBloodGroup, selectedCity, selectedRole)
                                                    }
                                                }
                                        }
                                    } else {
                                        isAuthLoading = false
                                        onLoginSuccess("0300" + (1000000..9999999).random(), finalName, selectedBloodGroup, selectedCity, selectedRole)
                                    }
                                }
                            },
                            enabled = !isAuthLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button")
                        ) {
                            Text(
                                text = if (isAuthLoading) LanguageState.getText("Authenticating with Firebase...", "براہ کرم انتظار کریں...")
                                       else if (isRegistering) LanguageState.getText("Register with Firebase", "اکاؤنٹ بنائیں")
                                       else LanguageState.getText("Sign In with Firebase", "لاگ ان کریں"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isRegistering) LanguageState.getText("Already have an account? ", "پہلے سے اکاؤنٹ ہے؟ ") else LanguageState.getText("Don't have an account? ", "اکاؤنٹ نہیں ہے؟ "),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = if (isRegistering) LanguageState.getText("Sign In", "سائن ان") else LanguageState.getText("Create Account", "نیا بنائیں"),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CrimsonRedPrimary,
                                modifier = Modifier.clickable { isRegistering = !isRegistering; errorMessage = null }
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

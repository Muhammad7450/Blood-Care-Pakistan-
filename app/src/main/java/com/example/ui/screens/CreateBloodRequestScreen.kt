package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.WarningOrange
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun CreateBloodRequestScreen(
    viewModel: BloodCareViewModel,
    onRequestCreated: () -> Unit
) {
    val context = LocalContext.current

    var patientName by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("O-") }
    var units by remember { mutableIntStateOf(2) }
    var hospitalName by remember { mutableStateOf("Services Hospital") }
    var selectedCity by remember { mutableStateOf("Lahore") }
    var emergencyLevel by remember { mutableStateOf("Critical") }
    var notes by remember { mutableStateOf("") }
    var isThalassemia by remember { mutableStateOf(false) }

    val bloodGroups = listOf("O-", "O+", "A+", "A-", "B+", "B-", "AB+", "AB-")
    val cities = listOf("Lahore", "Karachi", "Islamabad", "Rawalpindi", "Peshawar", "Multan", "Faisalabad", "Quetta")
    val emergencyLevels = listOf("Critical", "Urgent", "Normal")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = LanguageState.getText("Create Emergency Blood Request", "ہنگامی بلڈ کی درخواست دیں"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = CrimsonRedPrimary
        )

        Text(
            text = LanguageState.getText("Instantly notify nearby compatible donors in Pakistan", "پاکستان بھر کے ڈونرز کو برائے راست الرٹ بھیجیں"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Patient Name
                OutlinedTextField(
                    value = patientName,
                    onValueChange = { patientName = it },
                    label = { Text(LanguageState.getText("Patient Name", "مریض کا نام")) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CrimsonRedPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("request_patient_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Blood Group
                Text(
                    text = LanguageState.getText("Required Blood Group", "مطلوبہ بلڈ گروپ"),
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
                                .testTag("request_bg_chip_$bg")
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

                Spacer(modifier = Modifier.height(16.dp))

                // Units counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageState.getText("Number of Units Needed", "خون کی بوتلوں کی تعداد"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (units > 1) units-- },
                            modifier = Modifier
                                .background(SoftRedContainer, RoundedCornerShape(8.dp))
                                .testTag("minus_unit_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = CrimsonRedPrimary)
                        }

                        Text(
                            text = "$units",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        IconButton(
                            onClick = { if (units < 10) units++ },
                            modifier = Modifier
                                .background(SoftRedContainer, RoundedCornerShape(8.dp))
                                .testTag("plus_unit_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = CrimsonRedPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hospital Name
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text(LanguageState.getText("Hospital Name", "ہسپتال کا نام")) },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CrimsonRedPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hospital_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // City Selection
                Text(
                    text = LanguageState.getText("Hospital City", "ہسپتال کا شہر"),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Priority
                Text(
                    text = LanguageState.getText("Emergency Priority Level", "ہنگامی نوعیت"),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    emergencyLevels.forEach { level ->
                        val isSelected = emergencyLevel == level
                        val bgClr = when(level) {
                            "Critical" -> CriticalRed
                            "Urgent" -> WarningOrange
                            else -> CrimsonRedPrimary
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) bgClr else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { emergencyLevel = level }
                                .testTag("priority_$level")
                        ) {
                            Text(
                                text = level,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Thalassemia Patient Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isThalassemia = !isThalassemia }
                ) {
                    Checkbox(
                        checked = isThalassemia,
                        onCheckedChange = { isThalassemia = it },
                        colors = CheckboxDefaults.colors(checkedColor = CrimsonRedPrimary),
                        modifier = Modifier.testTag("thalassemia_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageState.getText("This request is for a Thalassemia Patient", "یہ درخواست تھیلیسیمیا کے مریض بچے کے لیے ہے"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Additional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(LanguageState.getText("Additional Notes / Ward / Doctor Name", "اضافی تفصیلات / وارڈ کا نام")) },
                    leadingIcon = { Icon(Icons.Default.Note, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("request_notes_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val name = if (patientName.isBlank()) "Emergency Patient" else patientName
                        viewModel.createBloodRequest(
                            patientName = name,
                            bloodGroup = selectedBloodGroup,
                            units = units,
                            hospitalName = hospitalName,
                            city = selectedCity,
                            locationName = "$hospitalName, $selectedCity",
                            emergencyLevel = emergencyLevel,
                            requiredDateTime = "Today, Urgent",
                            notes = notes,
                            isThalassemia = isThalassemia
                        )
                        Toast.makeText(context, "🚨 Emergency Request Broadcasted to $selectedCity Donors!", Toast.LENGTH_LONG).show()
                        onRequestCreated()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_request_button")
                ) {
                    Icon(Icons.Default.Bloodtype, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageState.getText("BROADCAST EMERGENCY ALERT NOW", "ہنگامی الرٹ ارسال کریں"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

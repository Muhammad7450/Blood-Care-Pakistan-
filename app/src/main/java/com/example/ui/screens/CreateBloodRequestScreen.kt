package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityUtils
import com.example.data.model.RequestUrgency
import com.example.data.model.UserProfile
import com.example.theme.CriticalBg
import com.example.theme.CriticalRed
import com.example.theme.CrimsonRed
import com.example.theme.DarkCrimson
import com.example.theme.NormalBg
import com.example.theme.NormalGreen
import com.example.theme.UrgentBg
import com.example.theme.UrgentOrange
import com.example.ui.components.BloodCareTopBar
import com.example.ui.viewmodel.BloodCareViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateBloodRequestScreen(
    userProfile: UserProfile,
    bloodCareViewModel: BloodCareViewModel,
    onBackClick: () -> Unit,
    onRequestCreated: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var patientName by remember { mutableStateOf(userProfile.fullName) }
    var patientPhone by remember { mutableStateOf(userProfile.phoneNumber) }
    var selectedBloodGroup by remember { mutableStateOf(userProfile.bloodGroup.ifBlank { "A+" }) }
    var selectedCity by remember { mutableStateOf(userProfile.city.ifBlank { "Nawabshah" }) }
    var latitude by remember { mutableStateOf(CityUtils.getCityCoordinates(userProfile.city.ifBlank { "Nawabshah" }).first) }
    var longitude by remember { mutableStateOf(CityUtils.getCityCoordinates(userProfile.city.ifBlank { "Nawabshah" }).second) }
    var cityExpanded by remember { mutableStateOf(false) }
    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var unitsRequired by remember { mutableLongStateOf(2L) }
    var selectedUrgency by remember { mutableStateOf(RequestUrgency.CRITICAL) }
    var requiredDate by remember { mutableStateOf("Immediate") }
    var additionalNotes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isActionLoading by bloodCareViewModel.isActionLoading.collectAsState()

    Scaffold(
        topBar = {
            BloodCareTopBar(
                title = "Emergency Blood Request",
                showBack = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Patient & Emergency Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCrimson
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (validationError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = validationError ?: "",
                                color = Color(0xFFC62828),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Patient Name
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("Patient / Requester Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_patient_name_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Patient Contact Phone
                    OutlinedTextField(
                        value = patientPhone,
                        onValueChange = { patientPhone = it },
                        label = { Text("Contact Phone (for Donors & Hospital)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_patient_phone_field"),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Required Blood Group
                    Text(
                        text = "Required Blood Group",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CityUtils.BLOOD_GROUPS.forEach { group ->
                            val isSelected = selectedBloodGroup == group
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CrimsonRed else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) CrimsonRed else Color(0xFFBDBDBD),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedBloodGroup = group }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = group,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DarkCrimson
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // City Dropdown
                    Text(
                        text = "City in Pakistan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = !cityExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = CrimsonRed
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("request_city_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false }
                        ) {
                            CityUtils.PAKISTAN_CITIES.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        selectedCity = city
                                        cityExpanded = false
                                        val coords = CityUtils.getCityCoordinates(city)
                                        latitude = coords.first
                                        longitude = coords.second
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hospital Name
                    OutlinedTextField(
                        value = hospitalName,
                        onValueChange = { hospitalName = it },
                        label = { Text("Hospital / Blood Bank Name") },
                        placeholder = { Text("e.g. Services Hospital, Peoples Medical") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_hospital_name_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hospital Address
                    OutlinedTextField(
                        value = hospitalAddress,
                        onValueChange = { hospitalAddress = it },
                        label = { Text("Hospital Address / Ward Info") },
                        placeholder = { Text("e.g. Ward 4, Emergency Wing") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_hospital_address_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Units Required Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Units (Bags) Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Standard 450ml donation bag",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (unitsRequired > 1) unitsRequired-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                text = "$unitsRequired",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = { if (unitsRequired < 10) unitsRequired++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEEEEE))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Urgency Level Selector
                    Text(
                        text = "Urgency Level",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UrgencyOption(
                            title = "Critical",
                            subtitle = "< 2 hours",
                            isSelected = selectedUrgency == RequestUrgency.CRITICAL,
                            activeColor = CriticalRed,
                            bgColor = CriticalBg,
                            onClick = { selectedUrgency = RequestUrgency.CRITICAL },
                            modifier = Modifier.weight(1f)
                        )
                        UrgencyOption(
                            title = "Urgent",
                            subtitle = "Today",
                            isSelected = selectedUrgency == RequestUrgency.URGENT,
                            activeColor = UrgentOrange,
                            bgColor = UrgentBg,
                            onClick = { selectedUrgency = RequestUrgency.URGENT },
                            modifier = Modifier.weight(1f)
                        )
                        UrgencyOption(
                            title = "Normal",
                            subtitle = "Scheduled",
                            isSelected = selectedUrgency == RequestUrgency.NORMAL,
                            activeColor = NormalGreen,
                            bgColor = NormalBg,
                            onClick = { selectedUrgency = RequestUrgency.NORMAL },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Required Date
                    OutlinedTextField(
                        value = requiredDate,
                        onValueChange = { requiredDate = it },
                        label = { Text("Required By (Time / Date)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notes
                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        label = { Text("Additional Notes (e.g. Patient condition)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (patientName.isBlank()) {
                                validationError = "Please enter patient name."
                                return@Button
                            }
                            if (patientPhone.isBlank()) {
                                validationError = "Please enter contact phone number."
                                return@Button
                            }
                            if (hospitalName.isBlank()) {
                                validationError = "Please enter hospital name."
                                return@Button
                            }
                            validationError = null
                            bloodCareViewModel.submitBloodRequest(
                                patientName = patientName,
                                patientPhone = patientPhone,
                                bloodGroup = selectedBloodGroup,
                                city = selectedCity,
                                hospitalName = hospitalName,
                                hospitalAddress = hospitalAddress,
                                unitsRequired = unitsRequired,
                                urgency = selectedUrgency,
                                requiredDate = requiredDate,
                                additionalNotes = additionalNotes,
                                latitude = latitude,
                                longitude = longitude,
                                onSuccess = onRequestCreated
                            )
                        },
                        enabled = !isActionLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_blood_request_button")
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Broadcast Emergency Request",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrgencyOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    activeColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) activeColor else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) bgColor else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) activeColor else Color(0xFF424242)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

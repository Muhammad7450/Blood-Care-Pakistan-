package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import com.example.data.model.BloodRequest
import com.example.data.model.CityUtils
import com.example.data.model.DonorResponse
import com.example.data.model.RequestStatus
import com.example.data.model.UserProfile
import com.example.theme.CrimsonRed
import com.example.theme.DarkCrimson
import com.example.theme.SoftPink
import com.example.ui.components.BloodCareTopBar
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun RequestDetailsScreen(
    requestId: String,
    currentUserProfile: UserProfile,
    bloodCareViewModel: BloodCareViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedRequest by bloodCareViewModel.selectedRequest.collectAsState()
    val responses by bloodCareViewModel.requestResponses.collectAsState()
    val isActionLoading by bloodCareViewModel.isActionLoading.collectAsState()
    val actionMessage by bloodCareViewModel.actionMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDonateDialog by remember { mutableStateOf(false) }

    // Intercept hardware/system back button safely to return to dashboard
    BackHandler {
        onBackClick()
    }

    LaunchedEffect(requestId) {
        if (requestId.isNotBlank()) {
            bloodCareViewModel.loadRequestById(requestId)
        }
    }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            bloodCareViewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            BloodCareTopBar(
                title = "Emergency Request Details",
                showBack = true,
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val request = selectedRequest
        if (request == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CrimsonRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading emergency details...", color = Color.Gray)
                }
            }
        } else {
            val isPatientOwner = request.patientId == currentUserProfile.uid
            val isOpen = request.status == RequestStatus.OPEN.name
            val hasResponded = responses.any { it.donorId == currentUserProfile.uid }

            val reqLat = when {
                request.latitude != 0.0 -> request.latitude
                else -> CityUtils.getCityCoordinates(request.city).first
            }
            val reqLng = when {
                request.longitude != 0.0 -> request.longitude
                else -> CityUtils.getCityCoordinates(request.city).second
            }
            val donorCoords = CityUtils.getCityCoordinates(currentUserProfile.city)
            val distanceKm = CityUtils.calculateHaversineDistanceKm(donorCoords.first, donorCoords.second, reqLat, reqLng)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8F9FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BloodGroupBadge(bloodGroup = request.bloodGroup.ifBlank { "O+" })

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${request.unitsRequired} Bag${if (request.unitsRequired > 1) "s" else ""} Needed",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkCrimson
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatusBadge(status = request.status.ifBlank { "OPEN" })
                                    UrgencyBadge(urgency = request.urgency.ifBlank { "URGENT" })
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Hospital info
                        DetailRow(
                            icon = Icons.Default.LocalHospital,
                            label = "Hospital / Medical Center",
                            value = request.hospitalName.ifBlank { "Hospital in ${request.city}" }
                        )

                        DetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "City & Hospital Ward Address",
                            value = "${request.city} - ${request.hospitalAddress.ifBlank { "Emergency Ward" }}"
                        )

                        if (request.requiredDate.isNotBlank()) {
                            DetailRow(
                                icon = Icons.Default.DateRange,
                                label = "Required Date & Time",
                                value = request.requiredDate
                            )
                        }

                        if (request.additionalNotes.isNotBlank()) {
                            DetailRow(
                                icon = Icons.Default.Info,
                                label = "Medical Condition / Notes",
                                value = request.additionalNotes
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient Live Contact Card with Direct Call Button
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Patient / Attendant Contact",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkCrimson
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Name: ${request.patientName.ifBlank { "Patient / Attendant" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        val phoneToCall = request.patientPhone.ifBlank { "" }
                        Text(
                            text = "Phone: ${phoneToCall.ifBlank { "Not provided" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF424242)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (phoneToCall.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall")).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (e: Exception) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("Patient Phone", phoneToCall)
                                        clipboard?.setPrimaryClip(clip)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Phone number copied: $phoneToCall")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("call_patient_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Patient Now",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call Patient Directly ($phoneToCall)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient Live Location & GPS Coordinates Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF00796B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Location Coordinates",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkCrimson
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Hospital GPS: ${String.format(java.util.Locale.US, "%.5f", reqLat)}° N, ${String.format(java.util.Locale.US, "%.5f", reqLng)}° E",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (distanceKm > 0.0) {
                            Text(
                                text = "Estimated Distance: ~${String.format(java.util.Locale.US, "%.1f", distanceKm)} km from ${currentUserProfile.city}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00796B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        // 1. Try Google Maps turn-by-turn navigation intent
                                        val gmmIntentUri = Uri.parse("google.navigation:q=$reqLat,$reqLng&mode=d")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                            `package` = "com.google.android.apps.maps"
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        // 2. Fallback to standard geo / maps URL (No API key needed!)
                                        try {
                                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$reqLat,$reqLng")
                                            val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(browserIntent)
                                        } catch (err: Exception) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Coordinates: $reqLat, $reqLng")
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_live_map_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Navigate with Google Maps",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Navigate on Maps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("GPS Coordinates", "$reqLat,$reqLng")
                                    clipboard?.setPrimaryClip(clip)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Coordinates copied: $reqLat, $reqLng")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Coordinates",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient or Donor Action Box
                if (!isPatientOwner) {
                    // Donor Action
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (hasResponded) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You volunteered for this request",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "The patient has received your response and contact details.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF616161)
                                )
                            } else if (isOpen) {
                                Text(
                                    text = "Can you donate blood for this emergency?",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = DarkCrimson
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Your blood group (${currentUserProfile.bloodGroup}) can save a life in ${request.city}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF616161)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showDonateDialog = true },
                                    enabled = !isActionLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("i_can_donate_button")
                                ) {
                                    if (isActionLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.VolunteerActivism,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "I Can Donate",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "This request is no longer open (${request.status}).",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                } else {
                    // Patient Management View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Volunteer Donors (${responses.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkCrimson
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (responses.isEmpty()) {
                                Text(
                                    text = "No donor offers yet. Compatible donors in ${request.city} have been alerted.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF757575)
                                )
                            } else {
                                responses.forEach { response ->
                                    DonorResponseItem(response = response) { phone ->
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            val clip = ClipData.newPlainText("Donor Phone", phone)
                                            clipboard?.setPrimaryClip(clip)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Donor phone copied: $phone")
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Request Management",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isOpen || request.status == RequestStatus.DONOR_RESPONDED.name) {
                                    Button(
                                        onClick = {
                                            bloodCareViewModel.updateStatus(request.id, RequestStatus.FULFILLED)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("details_mark_fulfilled_button")
                                    ) {
                                        Text("Mark Fulfilled", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            bloodCareViewModel.updateStatus(request.id, RequestStatus.CANCELLED)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("details_cancel_button")
                                    ) {
                                        Text("Cancel Request")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            bloodCareViewModel.deleteRequest(request.id) {
                                                onBackClick()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("details_archive_button")
                                    ) {
                                        Text("Archive Request", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Confirmation Dialog for "I Can Donate"
            if (showDonateDialog) {
                AlertDialog(
                    onDismissRequest = { showDonateDialog = false },
                    title = {
                        Text(
                            text = "Confirm Blood Donation",
                            fontWeight = FontWeight.Bold,
                            color = DarkCrimson
                        )
                    },
                    text = {
                        Text(
                            text = "By clicking Confirm, you commit to donate ${request.unitsRequired} unit(s) of ${request.bloodGroup} at ${request.hospitalName}. Your contact phone (${currentUserProfile.phoneNumber}) will be shared directly with the patient for hospital coordination."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDonateDialog = false
                                bloodCareViewModel.donateForRequest(
                                    request = request,
                                    donorName = currentUserProfile.fullName.ifBlank { "Anonymous Donor" },
                                    donorPhone = currentUserProfile.phoneNumber,
                                    donorBloodGroup = currentUserProfile.bloodGroup,
                                    onSuccess = {}
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.testTag("confirm_donation_button")
                        ) {
                            Text("Confirm Donation", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDonateDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CrimsonRed,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF757575)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
        }
    }
}

@Composable
fun DonorResponseItem(
    response: DonorResponse,
    onCallClick: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SoftPink),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CrimsonRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = response.donorBloodGroup.ifBlank { "O+" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = response.donorName.ifBlank { "Volunteer Donor" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkCrimson
                )
                Text(
                    text = "Phone: ${response.donorPhone.ifBlank { "Not provided" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF424242)
                )
            }

            if (response.donorPhone.isNotBlank()) {
                IconButton(
                    onClick = { onCallClick(response.donorPhone) },
                    modifier = Modifier.testTag("call_donor_button_${response.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Donor",
                        tint = CrimsonRed
                    )
                }
            }
        }
    }
}

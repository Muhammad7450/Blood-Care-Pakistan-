package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodRequest
import com.example.data.model.RequestStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.theme.CrimsonRed
import com.example.theme.DarkCrimson
import com.example.theme.SoftPink
import com.example.ui.components.BloodCareTopBar
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.viewmodel.BloodCareViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun PatientDashboardScreen(
    userProfile: UserProfile,
    bloodCareViewModel: BloodCareViewModel,
    onRequestClick: (BloodRequest) -> Unit,
    onCreateRequestClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val requestsState by bloodCareViewModel.patientRequestsState.collectAsState()
    val notifications by bloodCareViewModel.notificationsState.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }

    LaunchedEffect(userProfile.uid) {
        bloodCareViewModel.startObservingPatientRequests(userProfile.uid)
        bloodCareViewModel.startObservingNotifications(userProfile.uid)
    }

    Scaffold(
        topBar = {
            BloodCareTopBar(
                title = "My Blood Requests",
                actions = {
                    if (userProfile.role == UserRole.ADMIN.name) {
                        IconButton(
                            onClick = onAdminClick,
                            modifier = Modifier.testTag("admin_panel_button")
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = "Admin Panel", tint = Color.White)
                        }
                    }
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = Color.White, contentColor = CrimsonRed) {
                                        Text("$unreadNotifs", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                    }
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.testTag("profile_button")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = CrimsonRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("patient_create_request_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Blood Request", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Patient Header Card
            PatientHeaderCard(userProfile = userProfile)

            // Feed Content
            when (val state = requestsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CrimsonRed)
                    }
                }
                is UiState.Error -> {
                    EmptyStateView(
                        title = "Error loading your requests",
                        subtitle = state.message,
                        icon = Icons.Default.Bloodtype,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyStateView(
                            title = "No Blood Requests Posted Yet",
                            subtitle = "Tap 'New Blood Request' below to broadcast an emergency request to compatible donors in your city.",
                            icon = Icons.Default.VolunteerActivism,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.data, key = { it.id }) { request ->
                                PatientRequestCard(
                                    request = request,
                                    onClick = { onRequestClick(request) },
                                    onMarkFulfilled = {
                                        bloodCareViewModel.updateStatus(request.id, RequestStatus.FULFILLED)
                                    },
                                    onCancel = {
                                        bloodCareViewModel.updateStatus(request.id, RequestStatus.CANCELLED)
                                    },
                                    onArchive = {
                                        bloodCareViewModel.deleteRequest(request.id) {}
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientHeaderCard(userProfile: UserProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SoftPink),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userProfile.fullName.ifBlank { "Patient Account" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkCrimson
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${userProfile.city}, Pakistan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE3F2FD))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Role: Patient",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
fun PatientRequestCard(
    request: BloodRequest,
    onClick: () -> Unit,
    onMarkFulfilled: () -> Unit,
    onCancel: () -> Unit,
    onArchive: () -> Unit
) {
    val isDonorResponded = request.status == RequestStatus.DONOR_RESPONDED.name
    val isOpen = request.status == RequestStatus.OPEN.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("patient_request_card_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Highlight banner if donor has responded
            if (isDonorResponded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3E5F5))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = Color(0xFF7B1FA2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Donor Responded! Tap to view contact details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BloodGroupBadge(bloodGroup = request.bloodGroup)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.hospitalName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "${request.unitsRequired} Bag${if (request.unitsRequired > 1) "s" else ""} Required",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonRed
                    )
                }

                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                UrgencyBadge(urgency = request.urgency)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${request.city} - ${request.hospitalAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("patient_view_responses_${request.id}")
                ) {
                    Text(
                        text = if (isDonorResponded) "View Donors" else "View Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (isOpen || isDonorResponded) {
                    OutlinedButton(
                        onClick = onMarkFulfilled,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("mark_fulfilled_${request.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fulfilled",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cancel_request_${request.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onArchive,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("archive_request_${request.id}")
                    ) {
                        Text(
                            text = "Archive",
                            color = Color(0xFF757575),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

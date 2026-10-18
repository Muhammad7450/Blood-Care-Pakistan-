package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.CityUtils
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.theme.CrimsonRed
import com.example.theme.DarkCrimson
import com.example.theme.SoftPink
import com.example.ui.components.BloodCareTopBar
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.UrgencyBadge
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.BloodCareViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun DonorDashboardScreen(
    userProfile: UserProfile,
    authViewModel: AuthViewModel,
    bloodCareViewModel: BloodCareViewModel,
    onRequestClick: (BloodRequest) -> Unit,
    onCreateRequestClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val requestsState by bloodCareViewModel.matchingRequestsState.collectAsState()
    val bloodFilter by bloodCareViewModel.bloodGroupFilter.collectAsState()
    val urgencyFilter by bloodCareViewModel.urgencyFilter.collectAsState()
    val notifications by bloodCareViewModel.notificationsState.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(userProfile.city, userProfile.bloodGroup, userProfile.uid) {
        bloodCareViewModel.startObservingMatchingRequests(
            context = context,
            city = userProfile.city,
            donorBloodGroup = userProfile.bloodGroup
        )
        bloodCareViewModel.startObservingNotifications(
            context = context,
            userId = userProfile.uid
        )
    }

    Scaffold(
        topBar = {
            BloodCareTopBar(
                title = "Donor Feed",
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
            // Allows donors to also submit emergency requests if needed!
            FloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = CrimsonRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("donor_create_request_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Request Blood")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Need Blood?", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            // Donor Status & City Header Card
            DonorHeaderCard(
                userProfile = userProfile,
                onToggleAvailability = { authViewModel.toggleAvailability(it) }
            )

            // Filter Chips
            DonorFilterBar(
                selectedBloodFilter = bloodFilter,
                selectedUrgencyFilter = urgencyFilter,
                onSelectBloodFilter = { bloodCareViewModel.setBloodGroupFilter(it) },
                onSelectUrgencyFilter = { bloodCareViewModel.setUrgencyFilter(it) }
            )

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
                        title = "Unable to load requests",
                        subtitle = state.message,
                        icon = Icons.Default.Bloodtype,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is UiState.Success -> {
                    val filteredList = state.data.filter { req ->
                        val matchesBlood = if (bloodFilter == "ALL") {
                            CityUtils.isBloodCompatible(userProfile.bloodGroup, req.bloodGroup)
                        } else {
                            req.bloodGroup == bloodFilter
                        }
                        val matchesUrgency = if (urgencyFilter == "ALL") true else req.urgency == urgencyFilter
                        matchesBlood && matchesUrgency
                    }

                    if (filteredList.isEmpty()) {
                        EmptyStateView(
                            title = "No Urgent Requests in ${userProfile.city}",
                            subtitle = "You will receive real-time alerts as soon as an emergency request is published.",
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredList, key = { it.id }) { request ->
                                DonorRequestCard(
                                    request = request,
                                    onClick = { onRequestClick(request) }
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
fun DonorHeaderCard(
    userProfile: UserProfile,
    onToggleAvailability: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BloodGroupBadge(bloodGroup = userProfile.bloodGroup)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile.fullName.ifBlank { "Registered Donor" },
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

                // Availability Switch
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (userProfile.isAvailable) "Available" else "Busy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (userProfile.isAvailable) Color(0xFF2E7D32) else Color(0xFF757575)
                    )
                    Switch(
                        checked = userProfile.isAvailable,
                        onCheckedChange = onToggleAvailability,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CrimsonRed,
                            checkedTrackColor = SoftPink
                        ),
                        modifier = Modifier.testTag("availability_toggle_switch")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonorFilterBar(
    selectedBloodFilter: String,
    selectedUrgencyFilter: String,
    onSelectBloodFilter: (String) -> Unit,
    onSelectUrgencyFilter: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Live Requests Matching Your Area",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChipItem(
                label = "Compatible",
                isSelected = selectedBloodFilter == "ALL",
                onClick = { onSelectBloodFilter("ALL") }
            )
            FilterChipItem(
                label = "Critical",
                isSelected = selectedUrgencyFilter == "CRITICAL",
                onClick = {
                    onSelectUrgencyFilter(if (selectedUrgencyFilter == "CRITICAL") "ALL" else "CRITICAL")
                }
            )
            FilterChipItem(
                label = "Urgent",
                isSelected = selectedUrgencyFilter == "URGENT",
                onClick = {
                    onSelectUrgencyFilter(if (selectedUrgencyFilter == "URGENT") "ALL" else "URGENT")
                }
            )
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CrimsonRed else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@Composable
fun DonorRequestCard(
    request: BloodRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("donor_request_card_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                UrgencyBadge(urgency = request.urgency)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${request.city} - ${request.hospitalAddress.ifBlank { "Hospital location" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161)
                )
            }

            if (request.requiredDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Required by: ${request.requiredDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                }
            }

            if (request.additionalNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${request.additionalNotes}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_details_button_${request.id}")
            ) {
                Text(
                    text = "View Details & Respond",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

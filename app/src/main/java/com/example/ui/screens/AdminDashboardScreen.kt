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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BloodRequest
import com.example.data.model.CityUtils
import com.example.data.model.RequestStatus
import com.example.theme.CrimsonRed
import com.example.theme.DarkCrimson
import com.example.ui.components.BloodCareTopBar
import com.example.ui.components.BloodGroupBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.components.UrgencyBadge
import com.example.ui.viewmodel.BloodCareViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    bloodCareViewModel: BloodCareViewModel,
    onBackClick: () -> Unit,
    onRequestClick: (BloodRequest) -> Unit
) {
    val requestsState by bloodCareViewModel.adminRequestsState.collectAsState()

    var selectedCityFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedBloodFilter by remember { mutableStateOf("ALL") }
    var cityExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bloodCareViewModel.startObservingAdminRequests()
    }

    Scaffold(
        topBar = {
            BloodCareTopBar(
                title = "Admin Console",
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
        ) {
            // Filters card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System-Wide Requests & Moderation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkCrimson
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // City Filter
                    ExposedDropdownMenuBox(
                        expanded = cityExpanded,
                        onExpandedChange = { cityExpanded = !cityExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (selectedCityFilter == "ALL") "All Cities in Pakistan" else selectedCityFilter,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            label = { Text("Filter by City") }
                        )

                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Cities") },
                                onClick = {
                                    selectedCityFilter = "ALL"
                                    cityExpanded = false
                                }
                            )
                            CityUtils.PAKISTAN_CITIES.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        selectedCityFilter = city
                                        cityExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Filters
                    Text(
                        text = "Filter by Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "OPEN", "DONOR_RESPONDED", "FULFILLED", "CANCELLED").forEach { status ->
                            FilterChipItem(
                                label = status,
                                isSelected = selectedStatusFilter == status,
                                onClick = { selectedStatusFilter = status }
                            )
                        }
                    }
                }
            }

            // Requests list
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
                        title = "Error loading requests",
                        subtitle = state.message,
                        icon = Icons.Default.Bloodtype,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is UiState.Success -> {
                    val filtered = state.data.filter { req ->
                        val matchCity = if (selectedCityFilter == "ALL") true else req.city.equals(selectedCityFilter, ignoreCase = true)
                        val matchStatus = if (selectedStatusFilter == "ALL") true else req.status == selectedStatusFilter
                        val matchBlood = if (selectedBloodFilter == "ALL") true else req.bloodGroup == selectedBloodFilter
                        matchCity && matchStatus && matchBlood
                    }

                    if (filtered.isEmpty()) {
                        EmptyStateView(
                            title = "No Matching Requests",
                            subtitle = "Adjust your filters to view requests.",
                            icon = Icons.Default.Shield,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered, key = { it.id }) { req ->
                                AdminRequestCard(
                                    request = req,
                                    onClick = { onRequestClick(req) },
                                    onMarkFulfilled = {
                                        bloodCareViewModel.updateStatus(req.id, RequestStatus.FULFILLED)
                                    },
                                    onArchive = {
                                        bloodCareViewModel.deleteRequest(req.id) {}
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
fun AdminRequestCard(
    request: BloodRequest,
    onClick: () -> Unit,
    onMarkFulfilled: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("admin_request_card_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Patient: ${request.patientName} (${request.patientPhone})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                }
                StatusBadge(status = request.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                UrgencyBadge(urgency = request.urgency)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${request.city} | ${request.unitsRequired} Bag(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details", fontSize = 12.sp)
                }

                if (request.status == RequestStatus.OPEN.name || request.status == RequestStatus.DONOR_RESPONDED.name) {
                    OutlinedButton(
                        onClick = onMarkFulfilled,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fulfilled", color = Color(0xFF2E7D32), fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onArchive,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Archive", color = Color(0xFFC62828), fontSize = 12.sp)
                }
            }
        }
    }
}

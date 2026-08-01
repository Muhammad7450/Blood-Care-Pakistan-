package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonorEntity
import com.example.ui.components.BloodGroupChip
import com.example.ui.theme.CrimsonRedPrimary
import com.example.ui.theme.SoftRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.LanguageState
import com.example.ui.viewmodel.BloodCareViewModel

@Composable
fun NearbyDonorsMapScreen(
    viewModel: BloodCareViewModel
) {
    val context = LocalContext.current
    val allDonors by viewModel.allDonors.collectAsState()
    val allHospitals by viewModel.allHospitals.collectAsState()

    var selectedCity by remember { mutableStateOf("Lahore") }
    var selectedGroup by remember { mutableStateOf("All") }
    var selectedDonor by remember { mutableStateOf<DonorEntity?>(null) }

    val cities = listOf("Lahore", "Karachi", "Islamabad", "Peshawar", "Quetta", "Multan", "Faisalabad")
    val groups = listOf("All", "O+", "O-", "A+", "A-", "B+", "B-", "AB+")

    val filteredDonors = allDonors.filter { donor ->
        (selectedCity == "All" || donor.city.equals(selectedCity, ignoreCase = true)) &&
                (selectedGroup == "All" || donor.bloodGroup.equals(selectedGroup, ignoreCase = true))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarRadius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = CrimsonRedPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageState.getText("Live Donors Radar Map", "لائیو ڈونرز میپ"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftRedContainer
                ) {
                    Text(
                        text = "${filteredDonors.size} ${LanguageState.getText("Donors Nearby", "ڈونرز نزدیک")}",
                        color = CrimsonRedPrimary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cities
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cities) { city ->
                    val isSelected = selectedCity == city
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CrimsonRedPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedCity = city }
                    ) {
                        Text(
                            text = city,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Blood Group Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(groups) { g ->
                    val isSelected = selectedGroup == g
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CrimsonRedPrimary else SoftRedContainer,
                        modifier = Modifier.clickable { selectedGroup = g }
                    ) {
                        Text(
                            text = g,
                            color = if (isSelected) Color.White else CrimsonRedPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Radar Map Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1E242B)),
            contentAlignment = Alignment.Center
        ) {
            val gridColor = Color(0xFF2C353F)
            val primaryRadar = CrimsonRedPrimary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width.coerceAtMost(size.height) * 0.42f

                // Draw Radar concentric circles
                drawCircle(color = gridColor, radius = maxRadius * 0.3f, center = center, style = Stroke(1.5f))
                drawCircle(color = gridColor, radius = maxRadius * 0.6f, center = center, style = Stroke(1.5f))
                drawCircle(color = gridColor, radius = maxRadius * 0.9f, center = center, style = Stroke(1.5f))

                // Radar Crosslines
                drawLine(gridColor, Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius), 1.5f)
                drawLine(gridColor, Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y), 1.5f)

                // Animated Radar Wave Pulse
                drawCircle(
                    color = primaryRadar.copy(alpha = (1f - radarRadius) * 0.5f),
                    radius = maxRadius * radarRadius,
                    center = center,
                    style = Stroke(3f)
                )

                // User Location Center Dot
                drawCircle(color = Color.White, radius = 10f, center = center)
                drawCircle(color = primaryRadar, radius = 6f, center = center)
            }

            // Overlay Donors as Pins
            filteredDonors.forEachIndexed { index, donor ->
                // Calculate pseudo-random pin positions around center
                val angle = (index * 68) * (Math.PI / 180.0)
                val distanceRatio = 0.25f + ((index % 4) * 0.2f)
                val xOffset = (Math.cos(angle) * 130 * distanceRatio).dp
                val yOffset = (Math.sin(angle) * 130 * distanceRatio).dp

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = xOffset.coerceAtLeast(0.dp), top = yOffset.coerceAtLeast(0.dp))
                        .clickable { selectedDonor = donor }
                        .testTag("donor_pin_${donor.id}")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonRedPrimary,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = donor.bloodGroup,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Map Attribution Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Location: $selectedCity, Pakistan", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Donor Pin Detail Bottom Sheet Card
        selectedDonor?.let { donor ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("donor_map_details_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SoftRedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(donor.fullName.take(1), fontWeight = FontWeight.Bold, color = CrimsonRedPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(donor.fullName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("${donor.locationName}, ${donor.city}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        BloodGroupChip(group = donor.bloodGroup)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Donations: ${donor.totalDonations}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = if (donor.isAvailable) "Available Now" else "Not Available",
                            color = if (donor.isAvailable) SuccessGreen else Color.Gray,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { selectedDonor = null },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRedPrimary),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call ${donor.phone}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

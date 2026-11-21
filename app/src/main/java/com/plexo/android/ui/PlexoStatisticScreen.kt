package com.plexo.android.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plexo.android.ui.theme.*

@Composable
fun PlexoStatisticScreen(viewModel: DownloadViewModel) {
    val networkState by viewModel.networkState.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()

    var selectedFilter by remember { mutableStateOf("Speed") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VitalityBgTop,
                        VitalityBgMid,
                        VitalityBgBottom,
                        VitalityBgDeep
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top App Bar: Back Button, Title "Statistic", Menu Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button Circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VitalityGlassCard)
                        .border(1.dp, VitalityGlassBorder, CircleShape)
                        .clickable { viewModel.selectTab(PlexoTab.DOWNLOADS) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SageTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Statistic",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageTextPrimary
                )

                // Menu Circle Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VitalityGlassCard)
                        .border(1.dp, VitalityGlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = "Menu",
                        tint = SageTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Filter Tabs Pill Strip (Matching design: Steps, Steps, Regularity)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterPill(
                    title = "Speed",
                    icon = Icons.Default.Bolt,
                    isSelected = selectedFilter == "Speed",
                    onClick = { selectedFilter = "Speed" }
                )
                FilterPill(
                    title = "Data Flow",
                    icon = Icons.Default.FavoriteBorder,
                    isSelected = selectedFilter == "Data Flow",
                    onClick = { selectedFilter = "Data Flow" }
                )
                FilterPill(
                    title = "Bonding",
                    icon = Icons.Default.SignalCellularAlt,
                    isSelected = selectedFilter == "Bonding",
                    onClick = { selectedFilter = "Bonding" }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Hero Card: 7-Day Throughput Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(VitalityGlassCard)
                    .border(1.dp, VitalityGlassBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = currentTask?.formattedTotalSpeed ?: "1.8 MB/s",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SageTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "average throughput",
                                    fontSize = 12.sp,
                                    color = SageTextSecondary,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        // Timeframe Dropdown Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(VitalityGlassCardLight)
                                .border(1.dp, VitalityGlassBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "This Week",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SageTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SageTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 7-Day Vertical Pill Bars (Sat, Sun, Mon, Tue, Wed, Thu, Fri)
                    val days = listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
                    val heights = listOf(0.45f, 0.65f, 0.40f, 0.55f, 0.95f, 0.50f, 0.35f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEachIndexed { index, day ->
                            val isPeak = (day == "Wed") // Peak bonded throughput day
                            val barHeightFraction = heights[index]

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Vertical Rounded Bar
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .fillMaxHeight(barHeightFraction)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isPeak) {
                                                VitalityEmeraldDark
                                            } else {
                                                VitalityGlassCardLight
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isPeak) VitalityMint.copy(alpha = 0.5f) else VitalityGlassBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPeak) SageTextPrimary else SageTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header: Network Performance (Matching "Recent workout" in screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network telemetry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageTextPrimary
                )
                Text(
                    text = "Real-time",
                    fontSize = 12.sp,
                    color = VitalityMint
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Split Grid Cards: Left (Waveform area chart) + Right (Stacked metric cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Card: Real-time Waveform Area Chart (Heart rate style)
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(200.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(VitalityGlassCard)
                        .border(1.dp, VitalityGlassBorder, RoundedCornerShape(26.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Icon Chip (Heart icon)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(VitalityGlassCardLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = VitalityMint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Waveform Area Chart with Mint Gradient Fill
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val path = Path().apply {
                                moveTo(0f, h * 0.75f)
                                cubicTo(w * 0.2f, h * 0.70f, w * 0.35f, h * 0.20f, w * 0.5f, h * 0.15f)
                                cubicTo(w * 0.65f, h * 0.10f, w * 0.8f, h * 0.65f, w, h * 0.60f)
                            }

                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            // Gradient Area Fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        VitalityMint.copy(alpha = 0.45f),
                                        VitalityMint.copy(alpha = 0.05f)
                                    ),
                                    startY = 0f,
                                    endY = h
                                )
                            )

                            // Top Curve Stroke
                            drawPath(
                                path = path,
                                color = VitalityMint,
                                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                            )

                            // Peak Indicator Dot
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = Offset(w * 0.5f, h * 0.15f)
                            )
                            drawCircle(
                                color = VitalityMint,
                                radius = 8f,
                                center = Offset(w * 0.5f, h * 0.15f),
                                style = Stroke(width = 2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Wi-Fi Flow",
                            fontSize = 11.sp,
                            color = SageTextSecondary
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = currentTask?.formattedWifiSpeed ?: (if (networkState.wifi.isConnected) "1.2 MB/s" else "Offline"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )
                        }
                    }
                }

                // Right Column: Stacked Cards (Calories & Duration style)
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Card: Cellular Speed
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(94.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(VitalityGlassCard)
                            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(22.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(VitalityGlassCardLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = VitalityOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Cellular 5G",
                                fontSize = 11.sp,
                                color = SageTextSecondary
                            )
                            Text(
                                text = currentTask?.formattedCellularSpeed ?: (if (networkState.cellular.isConnected) "672 KB/s" else "Standby"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )
                        }
                    }

                    // Bottom Card: Active Streams
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(94.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(VitalityGlassCard)
                            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(22.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(VitalityGlassCardLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = VitalityCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Parallel Streams",
                                fontSize = 11.sp,
                                color = SageTextSecondary
                            )
                            Text(
                                text = "${currentTask?.chunks?.size ?: 16} chunks",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Multi-Link Telemetry Breakdown
            Text(
                text = "Multi-link interfaces",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SageTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InterfaceTelemetryBadge(
                    name = "Wi-Fi",
                    status = if (networkState.wifi.isConnected) "Connected" else "Standby",
                    speed = currentTask?.formattedWifiSpeed ?: "0 B/s",
                    tint = VitalityMint,
                    icon = Icons.Default.Wifi
                )
                InterfaceTelemetryBadge(
                    name = "5G Cellular",
                    status = if (networkState.cellular.isConnected) "Connected" else "Standby",
                    speed = currentTask?.formattedCellularSpeed ?: "0 B/s",
                    tint = VitalityOrange,
                    icon = Icons.Default.CellTower
                )
                InterfaceTelemetryBadge(
                    name = "USB Ethernet",
                    status = if (networkState.ethernet.isConnected) "Connected" else "Standby",
                    speed = currentTask?.formattedEthernetSpeed ?: "0 B/s",
                    tint = VitalityCyan,
                    icon = Icons.Default.Lan
                )
                InterfaceTelemetryBadge(
                    name = "Bluetooth PAN",
                    status = if (networkState.bluetooth.isConnected) "Connected" else "Standby",
                    speed = currentTask?.formattedBluetoothSpeed ?: "0 B/s",
                    tint = VitalityPurple,
                    icon = Icons.Default.Bluetooth
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun InterfaceTelemetryBadge(
    name: String,
    status: String,
    speed: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(VitalityGlassCard)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(VitalityGlassCardLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SageTextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(status, fontSize = 10.sp, color = SageTextSecondary)
            Text(speed, fontSize = 13.sp, fontWeight = FontWeight.Black, color = SageTextPrimary)
        }
    }
}

@Composable
fun FilterPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(VitalityPillActive)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = VitalityPillText,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VitalityPillText
            )
        }
    } else {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(VitalityGlassCard)
                .border(1.dp, VitalityGlassBorder, RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = SageTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = SageTextSecondary
            )
        }
    }
}

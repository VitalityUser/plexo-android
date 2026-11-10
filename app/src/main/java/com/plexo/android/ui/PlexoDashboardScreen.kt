package com.plexo.android.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plexo.android.engine.ChunkStatus
import com.plexo.android.engine.DownloadStatus
import com.plexo.android.engine.DownloadTask
import com.plexo.android.network.InterfaceInfo
import com.plexo.android.network.NetworkBondingState
import com.plexo.android.network.NetworkInterfaceType
import com.plexo.android.ui.theme.*

@Composable
fun PlexoDashboardScreen(viewModel: DownloadViewModel) {
    val networkState by viewModel.networkState.collectAsState()
    val currentTask by viewModel.currentTask.collectAsState()

    var urlInput by remember { mutableStateOf("") }
    var selectedSegments by remember { mutableIntStateOf(4) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VitalityBgTop,
                        VitalityBgMid,
                        VitalityBgBottom
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
            // Top App Bar / Profile Header with @vitality_user Watermark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Avatar / Logo Cluster
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(VitalityGlassCardLight)
                            .border(1.dp, VitalityGlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Plexo",
                            tint = VitalityMint,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "PLEXO",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SageTextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Multi-Link Aggregator",
                            fontSize = 12.sp,
                            color = SageTextSecondary
                        )
                    }
                }

                // Right: Watermark badge of @vitality_user & Bonding Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Watermark Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(VitalityGlassCard)
                            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ @vitality_user",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VitalityOrangeGlow
                            )
                        }
                    }

                    // Bonding Active Pill
                    VitalityBondingBadge(isBonding = networkState.isBondingActive)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Greeting Banner (matching screenshot: "Welcome, Alex! here's your...")
            Text(
                text = "Welcome! here's your\nnetwork speed snapshot for today!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SageTextPrimary,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (networkState.isBondingActive) "Dual-channel cellular & Wi-Fi bonding is operational" else "Single channel connection active",
                fontSize = 12.sp,
                color = SageTextSecondary
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Section: Today's Stats (Matching screenshot with circular progress meter cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's stats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageTextPrimary
                )
                Text(
                    text = "View more",
                    fontSize = 12.sp,
                    color = SageTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Scroll of Frosted Stats Cards (Step to walk, Cal burnt style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Wi-Fi Stats Card
                VitalityMetricCard(
                    title = "Wi-Fi link",
                    value = currentTask?.formattedWifiSpeed ?: (if (networkState.wifi.isConnected) "Ready" else "Offline"),
                    badgeText = if (networkState.wifi.isConnected) "Active" else "Offline",
                    ringColor = VitalityMint,
                    glowColor = VitalityMintGlow,
                    icon = Icons.Default.Wifi,
                    progress = if (networkState.wifi.isConnected) 0.85f else 0f
                )

                // Card 2: Cellular Stats Card
                VitalityMetricCard(
                    title = "Cellular link",
                    value = currentTask?.formattedCellularSpeed ?: (if (networkState.cellular.isConnected) "Bonded" else "Standby"),
                    badgeText = if (networkState.cellular.isConnected) "5G Bonded" else "Standby",
                    ringColor = VitalityOrange,
                    glowColor = VitalityOrangeGlow,
                    icon = Icons.Default.CellTower,
                    progress = if (networkState.cellular.isConnected) 0.70f else 0f
                )

                // Card 3: Combined Aggregated Flow Card
                VitalityMetricCard(
                    title = "Aggregated flow",
                    value = currentTask?.formattedTotalSpeed ?: "0 MB/s",
                    badgeText = if (networkState.isBondingActive) "2x Speed" else "Normal",
                    ringColor = VitalityCyan,
                    glowColor = Cyan400,
                    icon = Icons.Default.Bolt,
                    progress = if (networkState.isBondingActive) 1.0f else 0.5f
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Active Download Card (Matching screenshot "Recent workout" & "Statistic")
            currentTask?.let { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent download",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageTextPrimary
                    )
                    Text(
                        text = task.status.name,
                        fontSize = 12.sp,
                        color = VitalityMint
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                VitalityActiveDownloadCard(
                    task = task,
                    onPause = { viewModel.pauseDownload() },
                    onCancel = { viewModel.cancelDownload() }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: New Download & In-App Browser Action
            Text(
                text = "Start new download",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SageTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Frosted Glass New Download Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(VitalityGlassCard)
                    .border(1.dp, VitalityGlassBorder, RoundedCornerShape(26.dp))
                    .padding(18.dp)
            ) {
                Column {
                    // Search / URL Input Capsule
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = {
                            Text(
                                "Paste file link or URL...",
                                color = SageTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = VitalityMint,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VitalityMint,
                            unfocusedBorderColor = VitalityGlassBorder,
                            focusedTextColor = SageTextPrimary,
                            unfocusedTextColor = SageTextPrimary,
                            focusedContainerColor = VitalityGlassCardDark,
                            unfocusedContainerColor = VitalityGlassCardDark
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chunks Segments Filter (4x, 8x, 16x)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parallel Streams: $selectedSegments chunks",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SageTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(4, 8, 16).forEach { seg ->
                                val isSelected = selectedSegments == seg
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) VitalityPillActive else VitalityGlassCardLight)
                                        .border(
                                            1.dp,
                                            if (isSelected) VitalityMint else VitalityGlassBorder,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    TextButton(
                                        onClick = { selectedSegments = seg },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(20.dp)
                                    ) {
                                        Text(
                                            text = "${seg}x",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) VitalityPillText else SageTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Action: Download with Multi-Link Aggregation
                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                viewModel.startDownload(urlInput, selectedSegments)
                                urlInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VitalityPillActive,
                            contentColor = VitalityPillText
                        )
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = VitalityOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download with Dual-Link Aggregation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Action: Built-In Browser Button
                    OutlinedButton(
                        onClick = { viewModel.selectTab(PlexoTab.BROWSER) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SageTextPrimary
                        ),
                        border = BorderStroke(1.dp, VitalityGlassBorder)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            tint = VitalityMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search & Download via Plexo Browser",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-start switch from Chrome & Clipboard
                    val autoStart by viewModel.autoStartDownloads.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Start From Chrome & Links",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SageTextPrimary
                            )
                            Text(
                                text = "Instantly bond & download when links are shared or clicked",
                                fontSize = 11.sp,
                                color = SageTextSecondary
                            )
                        }
                        Switch(
                            checked = autoStart,
                            onCheckedChange = { viewModel.autoStartDownloads.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VitalityDarkNav,
                                checkedTrackColor = VitalityMint,
                                uncheckedThumbColor = SageTextMuted,
                                uncheckedTrackColor = VitalityGlassCardLight
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Setup Guide Card in Footer
            VitalitySetupGuideCard()

            Spacer(modifier = Modifier.height(30.dp))

            // Footer Watermark & Designer Attribution
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UI Inspiration & Art Direction by ",
                    fontSize = 12.sp,
                    color = SageTextMuted
                )
                Text(
                    text = "@vitality_user",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalityOrangeGlow
                )
            }
        }
    }
}

@Composable
fun VitalityBondingBadge(isBonding: Boolean) {
    val bgColor = if (isBonding) VitalityMint.copy(alpha = 0.2f) else VitalityGlassCardLight
    val textColor = if (isBonding) VitalityMint else SageTextMuted
    val text = if (isBonding) "⚡ BONDED" else "SINGLE LINK"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, if (isBonding) VitalityMint.copy(alpha = 0.5f) else VitalityGlassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

// Stats Card with Circular Dial (Matching the Step to walk / Cal burnt cards in screenshot)
@Composable
fun VitalityMetricCard(
    title: String,
    value: String,
    badgeText: String,
    ringColor: Color,
    glowColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    progress: Float
) {
    Box(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(VitalityGlassCard)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(26.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SageTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = SageTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dark pill tag inside the card (e.g. "Good", "Average")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(VitalityGlassCardDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageTextPrimary
                    )
                }

                // Glowing Circular Progress Meter
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(46.dp)) {
                        val strokeWidth = 4.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2f
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                        // Background track
                        drawCircle(
                            color = Color(0x22FFFFFF),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        // Progress arc
                        drawArc(
                            brush = Brush.sweepGradient(listOf(ringColor, glowColor)),
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inner Icon with subtle background
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ringColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = ringColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// Active Download Card (Matching the "Statistic" / "Recent workout" card with wave curve)
@Composable
fun VitalityActiveDownloadCard(
    task: DownloadTask,
    onPause: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(VitalityGlassCard)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Column {
            // Header with Icon & File Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VitalityGlassCardLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = VitalityMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = task.fileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SageTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = task.formattedSize,
                            fontSize = 12.sp,
                            color = SageTextSecondary
                        )
                    }
                }

                // Progress Percentage Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VitalityGlassCardDark)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = VitalityMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waveform Telemetry Chart (Inspired by the Heart Rate wave in screenshot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(VitalityGlassCardDark)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val path = Path().apply {
                        moveTo(0f, h * 0.7f)
                        cubicTo(w * 0.2f, h * 0.8f, w * 0.35f, h * 0.2f, w * 0.5f, h * 0.3f)
                        cubicTo(w * 0.65f, h * 0.4f, w * 0.8f, h * 0.1f, w, h * 0.5f)
                    }

                    // Stroke Wave
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            listOf(VitalityMint, VitalityOrangeGlow)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Bonded Throughput",
                        fontSize = 10.sp,
                        color = SageTextMuted
                    )
                    Text(
                        text = task.formattedTotalSpeed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Frosted Stat Tiles (Calories & Duration style from screenshot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Wi-Fi Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VitalityGlassCardLight)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = VitalityMint, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wi-Fi Flow", fontSize = 11.sp, color = SageTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(task.formattedWifiSpeed, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SageTextPrimary)
                    }
                }

                // Cellular Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VitalityGlassCardLight)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CellTower, contentDescription = null, tint = VitalityOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cellular Flow", fontSize = 11.sp, color = SageTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(task.formattedCellularSpeed, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SageTextPrimary)
                    }
                }
            }

            // Chunks Parallel Grid
            if (task.chunks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    task.chunks.forEach { chunk ->
                        val chunkColor = when (chunk.assignedInterface) {
                            NetworkInterfaceType.WIFI -> VitalityMint
                            NetworkInterfaceType.CELLULAR -> VitalityOrange
                            NetworkInterfaceType.DEFAULT -> VitalityCyan
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (chunk.status == ChunkStatus.COMPLETED) chunkColor
                                    else if (chunk.status == ChunkStatus.DOWNLOADING) chunkColor.copy(alpha = 0.5f)
                                    else Color(0x33FFFFFF)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                    border = BorderStroke(1.dp, Rose500.copy(alpha = 0.5f))
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onPause,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalityPillActive,
                        contentColor = VitalityPillText
                    )
                ) {
                    Text(
                        if (task.status == DownloadStatus.PAUSED) "Resume" else "Pause",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun VitalitySetupGuideCard() {
    var isExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(VitalityGlassCard)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(VitalityOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = VitalityOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HOW TO SETUP & BOND",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SageTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "4-step quick guide for maximum speed",
                            fontSize = 11.sp,
                            color = SageTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = SageTextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    SetupStepItem(
                        step = "1",
                        title = "Enable Mobile Data Always Active",
                        description = "Go to Android Settings > Developer Options > Enable 'Mobile data always active'. This keeps cellular alive even when connected to Wi-Fi.",
                        icon = Icons.Default.Tune,
                        accentColor = VitalityMint
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SetupStepItem(
                        step = "2",
                        title = "Turn ON Both Connections",
                        description = "Enable both Wi-Fi and Mobile Data on your device. The top badge will turn green with '⚡ BONDED' when both adapters are ready.",
                        icon = Icons.Default.CompareArrows,
                        accentColor = VitalityOrange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SetupStepItem(
                        step = "3",
                        title = "Download from Chrome or In-App Browser",
                        description = "In Chrome or any app, tap Share > Plexo, copy any link, or browse websites in the built-in 'Browser' tab to intercept downloads.",
                        icon = Icons.Default.Language,
                        accentColor = VitalityCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SetupStepItem(
                        step = "4",
                        title = "Unrestricted Battery",
                        description = "Set Plexo's App Battery usage to 'Unrestricted' so background bonded streams are never paused when your screen is locked.",
                        icon = Icons.Default.BatteryChargingFull,
                        accentColor = VitalityMintGlow
                    )
                }
            }
        }
    }
}

@Composable
fun SetupStepItem(
    step: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(VitalityGlassCardDark)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SageTextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = SageTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

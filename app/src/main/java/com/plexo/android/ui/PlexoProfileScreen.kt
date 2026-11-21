package com.plexo.android.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plexo.android.ui.theme.*

@Composable
fun PlexoProfileScreen(viewModel: DownloadViewModel) {
    val networkState by viewModel.networkState.collectAsState()
    val autoStart by viewModel.autoStartDownloads.collectAsState()

    var soundAlerts by remember { mutableStateOf(true) }
    var wakeLockActive by remember { mutableStateOf(true) }

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
            // Top App Bar: Back, Title "Profile", Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
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
                    text = "Profile & Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SageTextPrimary
                )

                // Edit / Options Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VitalityGlassCard)
                        .border(1.dp, VitalityGlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = SageTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Profile Card (Jacob Donald card style from screenshot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(VitalityGlassCard)
                    .border(1.dp, VitalityGlassBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Avatar with glowing frosted border
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(VitalityGlassCardLight)
                                .border(2.dp, VitalityMint.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = VitalityMint,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Shreyansh 🔥",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SageTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@vitality_user • Plexo Android",
                                fontSize = 12.sp,
                                color = SageTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Stat Pill Boxes (Age 27, Height 170cm, Weight 52kg style)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatPill(
                            modifier = Modifier.weight(1f),
                            label = "Dual Link",
                            value = "2x",
                            unit = "speed"
                        )
                        ProfileStatPill(
                            modifier = Modifier.weight(1f),
                            label = "Status",
                            value = if (networkState.isBondingActive) "Bonded" else "Ready",
                            unit = if (networkState.isBondingActive) "active" else "standby"
                        )
                        ProfileStatPill(
                            modifier = Modifier.weight(1f),
                            label = "Streams",
                            value = "16",
                            unit = "chunks"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Account settings (Matching screenshot stacked list)
            Text(
                text = "Bonding engine settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SageTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Item 1: Auto-Sniffer
                ProfileSettingRow(
                    icon = Icons.Default.Language,
                    title = "Chrome & Browser Auto-Sniff",
                    trailing = {
                        Switch(
                            checked = autoStart,
                            onCheckedChange = { viewModel.autoStartDownloads.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VitalityPillText,
                                checkedTrackColor = VitalityMint,
                                uncheckedThumbColor = SageTextSecondary,
                                uncheckedTrackColor = VitalityGlassCardDark
                            )
                        )
                    }
                )

                // Item 2: WakeLock & Background Sync
                ProfileSettingRow(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Background WakeLock Sync",
                    trailing = {
                        Switch(
                            checked = wakeLockActive,
                            onCheckedChange = { wakeLockActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VitalityPillText,
                                checkedTrackColor = VitalityMint,
                                uncheckedThumbColor = SageTextSecondary,
                                uncheckedTrackColor = VitalityGlassCardDark
                            )
                        )
                    }
                )

                // Item 3: Sound Alerts
                ProfileSettingRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound & Vibration Alerts",
                    trailing = {
                        Switch(
                            checked = soundAlerts,
                            onCheckedChange = { soundAlerts = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VitalityPillText,
                                checkedTrackColor = VitalityMint,
                                uncheckedThumbColor = SageTextSecondary,
                                uncheckedTrackColor = VitalityGlassCardDark
                            )
                        )
                    }
                )

                // Item 4: USB-C Ethernet Status
                ProfileSettingRow(
                    icon = Icons.Default.Lan,
                    title = "USB-C Ethernet Aggregation",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (networkState.ethernet.isConnected) VitalityMint.copy(alpha = 0.2f) else VitalityGlassCardDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (networkState.ethernet.isConnected) "ACTIVE" else "READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (networkState.ethernet.isConnected) VitalityMint else SageTextMuted
                            )
                        }
                    }
                )

                // Item 5: Bluetooth Tethering Status
                ProfileSettingRow(
                    icon = Icons.Default.Bluetooth,
                    title = "Bluetooth Tethering Bonding",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (networkState.bluetooth.isConnected) VitalityPurple.copy(alpha = 0.2f) else VitalityGlassCardDark)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (networkState.bluetooth.isConnected) "ACTIVE" else "READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (networkState.bluetooth.isConnected) VitalityPurple else SageTextMuted
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Preferences & Credits
            Text(
                text = "Preferences & Info",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SageTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Item 1: Guide
                ProfileLinkRow(
                    icon = Icons.Default.HelpOutline,
                    title = "How to Bond Networks (Guide)",
                    subtitle = "Multi-link setup (Wi-Fi, 5G, USB Ethernet & Bluetooth)"
                )

                // Item 2: GitHub
                ProfileLinkRow(
                    icon = Icons.Default.Code,
                    title = "GitHub Repository",
                    subtitle = "VitalityUser/plexo-android (Public)"
                )

                // Item 3: Art Direction Watermark
                ProfileLinkRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "UI Art Direction & Design",
                    subtitle = "Created with inspiration from @vitality_user"
                )

                // Item 4: App Version
                ProfileLinkRow(
                    icon = Icons.Default.Info,
                    title = "Plexo Android Version",
                    subtitle = "v1.0.0 • Frosted Sage Release"
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileStatPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(VitalityGlassCardLight)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = SageTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = SageTextPrimary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = SageTextMuted,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    trailing: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(VitalityGlassCard)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VitalityGlassCardLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = SageTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = SageTextPrimary
                )
            }
            trailing()
        }
    }
}

@Composable
fun ProfileLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(VitalityGlassCard)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(VitalityGlassCardLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = SageTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SageTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = SageTextSecondary
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SageTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

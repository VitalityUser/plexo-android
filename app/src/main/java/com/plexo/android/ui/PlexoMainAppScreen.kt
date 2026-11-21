package com.plexo.android.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plexo.android.ui.theme.*

@Composable
fun PlexoMainAppScreen(viewModel: DownloadViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val detectedDownload by viewModel.detectedDownload.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitalityBgBottom)
    ) {
        // Main Screen Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentTab) {
                PlexoTab.DOWNLOADS -> {
                    PlexoDashboardScreen(viewModel = viewModel)
                }
                PlexoTab.BROWSER -> {
                    PlexoBrowserScreen(viewModel = viewModel)
                }
                PlexoTab.STATISTICS -> {
                    PlexoStatisticScreen(viewModel = viewModel)
                }
                PlexoTab.PROFILE -> {
                    PlexoProfileScreen(viewModel = viewModel)
                }
            }
        }

        // Floating Signature Pill Navigation Bar (Matching @vitality_user inspiration)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
        ) {
            VitalityFloatingNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }

        // Floating Detected Download Banner (from Chrome / Browser / Clipboard)
        AnimatedVisibility(
            visible = detectedDownload != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(14.dp)
        ) {
            detectedDownload?.let { detection ->
                VitalityDetectedDownloadCard(
                    detection = detection,
                    onAccept = { viewModel.acceptDetectedDownload() },
                    onDismiss = { viewModel.dismissDetectedDownload() }
                )
            }
        }
    }
}

// Signature Floating Pill Navigation Bar (Inspired by @vitality_user)
@Composable
fun VitalityFloatingNavBar(
    currentTab: PlexoTab,
    onTabSelected: (PlexoTab) -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(36.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(36.dp))
            .background(VitalityDarkNav)
            .border(1.dp, VitalityGlassBorder, RoundedCornerShape(36.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VitalityNavBarItem(
                tab = PlexoTab.DOWNLOADS,
                icon = Icons.Default.Bolt,
                label = "Home",
                isSelected = currentTab == PlexoTab.DOWNLOADS,
                onSelect = onTabSelected
            )
            VitalityNavBarItem(
                tab = PlexoTab.BROWSER,
                icon = Icons.Default.Language,
                label = "Browser",
                isSelected = currentTab == PlexoTab.BROWSER,
                onSelect = onTabSelected
            )
            VitalityNavBarItem(
                tab = PlexoTab.STATISTICS,
                icon = Icons.Default.BarChart,
                label = "Statistic",
                isSelected = currentTab == PlexoTab.STATISTICS,
                onSelect = onTabSelected
            )
            VitalityNavBarItem(
                tab = PlexoTab.PROFILE,
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentTab == PlexoTab.PROFILE,
                onSelect = onTabSelected
            )
        }
    }
}

@Composable
fun VitalityNavBarItem(
    tab: PlexoTab,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onSelect: (PlexoTab) -> Unit
) {
    if (isSelected) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(VitalityPillActive)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = VitalityPillText,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VitalityPillText
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(VitalityGlassCardDark)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onSelect(tab) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = SageTextSecondary,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
fun VitalityDetectedDownloadCard(
    detection: DetectedDownload,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(VitalityDarkNav)
            .border(1.dp, VitalityMint, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(VitalityMint.copy(alpha = 0.2f))
                            .padding(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = VitalityMint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DOWNLOAD DETECTED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = VitalityMint,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "From ${detection.source}",
                            fontSize = 12.sp,
                            color = SageTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = SageTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = detection.fileName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SageTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = detection.url,
                fontSize = 11.sp,
                color = SageTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SageTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VitalityGlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dismiss", fontSize = 12.sp)
                }

                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitalityPillActive,
                        contentColor = VitalityPillText
                    ),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = VitalityOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Download (Bonded)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

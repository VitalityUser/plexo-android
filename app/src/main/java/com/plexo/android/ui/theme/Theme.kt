package com.plexo.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VitalityColorScheme = darkColorScheme(
    primary = VitalityMint,
    secondary = VitalityOrange,
    tertiary = VitalityCyan,
    background = VitalityBgBottom,
    surface = VitalityGlassCard,
    onPrimary = VitalityDarkNav,
    onSecondary = VitalityDarkNav,
    onTertiary = VitalityDarkNav,
    onBackground = SageTextPrimary,
    onSurface = SageTextPrimary
)

@Composable
fun PlexoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VitalityColorScheme,
        content = content
    )
}

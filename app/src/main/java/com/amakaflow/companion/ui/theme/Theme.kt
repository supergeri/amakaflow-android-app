package com.amakaflow.companion.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * AmakaFlow Theme Colors - matching iOS design
 */
object AmakaColors {
    // Primary backgrounds
    val background = Color(0xFF0D0D0F)
    val surface = Color(0xFF1A1A1E)
    val surfaceElevated = Color(0xFF25252A)

    // Text colors
    val textPrimary = Color.White
    val textSecondary = Color(0xFF9CA3AF)
    val textTertiary = Color(0xFF6B7280)

    // Accent colors
    val accentBlue = Color(0xFF3A8BFF)
    val accentGreen = Color(0xFF4EDF9B)
    val accentRed = Color(0xFFEF4444)
    val accentOrange = Color(0xFFF97316)
    val accentPurple = Color(0xFF8B5CF6)
    val accentYellow = Color(0xFFFACC15)

    // Device brand colors
    val garminBlue = Color(0xFF007ACC)
    val amazfitOrange = Color(0xFFFF6B00)

    // Borders
    val borderLight = Color(0xFF2D2D32)
    val borderMedium = Color(0xFF3F3F46)

    // Sport colors
    val sportRunning = accentGreen
    val sportCycling = accentBlue
    val sportStrength = accentPurple
    val sportMobility = accentOrange
    val sportSwimming = Color(0xFF06B6D4)
    val sportCardio = accentRed

    // Sync status colors (matching iOS)
    val syncSynced = Color(0xFF34C759)      // Green
    val syncPending = Color(0xFFFF9500)     // Orange
    val syncFailed = Color(0xFFFF3B30)      // Red
    val syncNotAssigned = Color(0xFF8E8E93) // Gray
}

/**
 * Spacing constants
 */
object AmakaSpacing {
    val xs = 4
    val sm = 8
    val md = 16
    val lg = 24
    val xl = 32
}

/**
 * Corner radius constants
 */
object AmakaCornerRadius {
    val sm = 8
    val md = 12
    val lg = 16
    val xl = 20
}

private val DarkColorScheme = darkColorScheme(
    primary = AmakaColors.accentBlue,
    onPrimary = Color.White,
    primaryContainer = AmakaColors.accentBlue.copy(alpha = 0.2f),
    onPrimaryContainer = AmakaColors.accentBlue,
    secondary = AmakaColors.accentGreen,
    onSecondary = Color.Black,
    secondaryContainer = AmakaColors.accentGreen.copy(alpha = 0.2f),
    onSecondaryContainer = AmakaColors.accentGreen,
    tertiary = AmakaColors.accentOrange,
    onTertiary = Color.Black,
    tertiaryContainer = AmakaColors.accentOrange.copy(alpha = 0.2f),
    onTertiaryContainer = AmakaColors.accentOrange,
    error = AmakaColors.accentRed,
    onError = Color.White,
    errorContainer = AmakaColors.accentRed.copy(alpha = 0.2f),
    onErrorContainer = AmakaColors.accentRed,
    background = AmakaColors.background,
    onBackground = AmakaColors.textPrimary,
    surface = AmakaColors.surface,
    onSurface = AmakaColors.textPrimary,
    surfaceVariant = AmakaColors.surfaceElevated,
    onSurfaceVariant = AmakaColors.textSecondary,
    outline = AmakaColors.borderMedium,
    outlineVariant = AmakaColors.borderLight,
    inverseSurface = Color.White,
    inverseOnSurface = AmakaColors.background,
    inversePrimary = AmakaColors.accentBlue,
    surfaceTint = AmakaColors.accentBlue,
)

@Composable
fun AmakaFlowTheme(
    darkTheme: Boolean = true, // Always dark theme like iOS
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AmakaColors.background.toArgb()
            window.navigationBarColor = AmakaColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

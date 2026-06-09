package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * BassPlayer Premium Theme System
 * Glassmorphism + High-End Audio Aesthetics
 */

// ========== COLOR SCHEMES ==========

private val BassPlayerDarkColorScheme = darkColorScheme(
    primary = BassPlayerColors.Primary.gradient_start,
    onPrimary = BassPlayerColors.Backgrounds.neutral_dark,
    primaryContainer = BassPlayerColors.Glass.dark_elevated,
    onPrimaryContainer = BassPlayerColors.Text.primary,
    
    secondary = BassPlayerColors.Secondary.gradient_start,
    onSecondary = BassPlayerColors.Backgrounds.neutral_dark,
    secondaryContainer = BassPlayerColors.Glass.dark_elevated.copy(alpha = 0.5f),
    onSecondaryContainer = BassPlayerColors.Text.primary,
    
    tertiary = BassPlayerColors.Accent.purple,
    onTertiary = BassPlayerColors.Backgrounds.neutral_dark,
    tertiaryContainer = BassPlayerColors.Glass.dark_elevated,
    onTertiaryContainer = BassPlayerColors.Text.primary,
    
    error = BassPlayerColors.Status.error,
    onError = BassPlayerColors.Backgrounds.neutral_dark,
    errorContainer = BassPlayerColors.Status.error_light,
    onErrorContainer = BassPlayerColors.Status.error,
    
    background = BassPlayerColors.Backgrounds.neutral_dark,
    onBackground = BassPlayerColors.Text.primary,
    surface = BassPlayerColors.Backgrounds.surface_1,
    onSurface = BassPlayerColors.Text.primary,
    surfaceVariant = BassPlayerColors.Glass.dark_surface,
    onSurfaceVariant = BassPlayerColors.Text.secondary,
    
    outline = BassPlayerColors.Glass.dark_border,
    outlineVariant = BassPlayerColors.Glass.dark_border.copy(alpha = 0.08f),
    scrim = BassPlayerColors.Overlay.scrim_dark
)

private val BassPlayerLightColorScheme = lightColorScheme(
    primary = BassPlayerColors.Primary.gradient_end,
    onPrimary = BassPlayerColors.Backgrounds.neutral_dark,
    primaryContainer = BassPlayerColors.Primary.surface,
    onPrimaryContainer = BassPlayerColors.Primary.dark,
    
    secondary = BassPlayerColors.Secondary.gradient_end,
    onSecondary = BassPlayerColors.Text.primary,
    secondaryContainer = BassPlayerColors.Secondary.surface,
    onSecondaryContainer = BassPlayerColors.Secondary.dark,
    
    tertiary = BassPlayerColors.Accent.purple,
    onTertiary = BassPlayerColors.Text.primary,
    tertiaryContainer = BassPlayerColors.Glass.light_surface,
    onTertiaryContainer = BassPlayerColors.Accent.purple,
    
    error = BassPlayerColors.Status.error,
    errorContainer = BassPlayerColors.Status.error_light,
    onError = BassPlayerColors.Text.primary,
    onErrorContainer = BassPlayerColors.Status.error,
    
    background = BassPlayerColors.Backgrounds.neutral_dark,
    onBackground = BassPlayerColors.Text.primary,
    surface = BassPlayerColors.Backgrounds.surface_1,
    onSurface = BassPlayerColors.Text.primary,
    surfaceVariant = BassPlayerColors.Glass.light_surface,
    onSurfaceVariant = BassPlayerColors.Text.secondary,
    
    outline = BassPlayerColors.Glass.light_border,
    outlineVariant = BassPlayerColors.Glass.light_border.copy(alpha = 0.08f),
    scrim = BassPlayerColors.Overlay.scrim_light
)

// ========== THEME COMPOSITION LOCALS ==========

/**
 * Local composition para themes adicionales (Glass properties, elevation, etc.)
 */
data class BassPlayerThemeTokens(
    val glassBlurRadius: Float = 12f,
    val glassBorderWidth: Float = 1.5f,
    val glassElevation: Float = 16f,
    val cornerRadiusSmall: Float = 12f,
    val cornerRadiusMedium: Float = 16f,
    val cornerRadiusLarge: Float = 24f,
    val cornerRadiusXLarge: Float = 32f
)

val LocalBassPlayerThemeTokens = compositionLocalOf {
    BassPlayerThemeTokens()
}

// ========== MAIN THEME COMPOSABLE ==========

@Composable
fun BassPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context).copy(
                    primary = BassPlayerColors.Primary.gradient_start,
                    secondary = BassPlayerColors.Secondary.gradient_start,
                    background = BassPlayerColors.Backgrounds.neutral_dark
                )
            } else {
                lightColorScheme(
                    primary = BassPlayerColors.Primary.gradient_end,
                    secondary = BassPlayerColors.Secondary.gradient_end,
                    background = BassPlayerColors.Backgrounds.surface_1
                )
            }
        }
        darkTheme -> BassPlayerDarkColorScheme
        else -> BassPlayerLightColorScheme
    }
    
    val themeTokens = BassPlayerThemeTokens()
    
    CompositionLocalProvider(
        LocalBassPlayerThemeTokens provides themeTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BassPlayerTypography,
            content = content
        )
    }
}

// Legacy compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    BassPlayerTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

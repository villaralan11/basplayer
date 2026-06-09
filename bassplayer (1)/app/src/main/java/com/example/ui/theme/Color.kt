package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium Color Palette for BassPlayer
 * Inspired by: Telegram, iOS, High-End Audio Aesthetics
 * Target: Epicenter Bass & Hi-Res/Hi-Fi/Hi-End Community
 */

object BassPlayerColors {
    
    // ========== PRIMARY GRADIENT SYSTEM ==========
    object Primary {
        val gradient_start = Color(0xFF00D9FF)      // Cyan vibrant
        val gradient_end = Color(0xFF0099CC)        // Deep cyan
        val light = Color(0xFF4DD9FF)               // Light cyan
        val dark = Color(0xFF007399)                // Dark cyan
        val surface = Color(0xFF00D9FF).copy(alpha = 0.12f)
    }
    
    object Secondary {
        val gradient_start = Color(0xFFFF006E)      // Magenta punch
        val gradient_end = Color(0xFFCC0055)        // Deep magenta
        val light = Color(0xFFFF4D99)               // Light magenta
        val dark = Color(0xFFBB0044)                // Dark magenta
        val surface = Color(0xFFFF006E).copy(alpha = 0.12f)
    }
    
    object Accent {
        val gold = Color(0xFFFFD700)                // Premium gold
        val lime = Color(0xFF00FF41)                // Acid lime (bass)
        val purple = Color(0xFF9D4EDD)             // Deep purple
        val orange = Color(0xFFFF7D00)             // Warm orange
    }
    
    // ========== GLASS MORPHISM SYSTEM ==========
    object Glass {
        // Light theme
        val light_bg = Color(0xFFFFFFFF).copy(alpha = 0.08f)
        val light_border = Color(0xFFFFFFFF).copy(alpha = 0.2f)
        val light_surface = Color(0xFFFFFFFF).copy(alpha = 0.05f)
        val light_elevated = Color(0xFFFFFFFF).copy(alpha = 0.12f)
        
        // Dark theme (default)
        val dark_bg = Color(0xFF1A1A2E).copy(alpha = 0.5f)
        val dark_border = Color(0xFFFFFFFF).copy(alpha = 0.15f)
        val dark_surface = Color(0xFF16213E).copy(alpha = 0.4f)
        val dark_elevated = Color(0xFF0F3460).copy(alpha = 0.6f)
        
        // Ultra premium (overlay mode)
        val premium_bg = Color(0xFF0D1B2A).copy(alpha = 0.7f)
        val premium_border = Color(0xFF00D9FF).copy(alpha = 0.3f)
        val premium_glow = Color(0xFF00D9FF).copy(alpha = 0.2f)
    }
    
    // ========== BACKGROUND GRADIENTS ==========
    object Backgrounds {
        // Main app background (dark neutral)
        val neutral_dark = Color(0xFF0F0F1E)
        val neutral_darker = Color(0xFF0A0A14)
        
        // Premium dark (almost black with blue tint)
        val premium_dark = Color(0xFF0D1B2A)
        val premium_darker = Color(0xFF0A1428)
        
        // Elevated surfaces (slightly lighter)
        val surface_1 = Color(0xFF1A1A2E)
        val surface_2 = Color(0xFF16213E)
        val surface_3 = Color(0xFF0F3460)
    }
    
    // ========== TEXT HIERARCHY ==========
    object Text {
        val primary = Color(0xFFFFFFFF)             // Primary text
        val secondary = Color(0xFFB0B0B0)           // Secondary text
        val tertiary = Color(0xFF808080)            // Tertiary text
        val disabled = Color(0xFF505050)            // Disabled text
        
        // Colored text
        val cyan = Color(0xFF00D9FF)                // Accent cyan
        val magenta = Color(0xFFFF006E)             // Accent magenta
        val gold = Color(0xFFFFD700)                // Premium gold
    }
    
    // ========== STATUS & FEEDBACK ==========
    object Status {
        val success = Color(0xFF00FF41)             // Success (bright lime)
        val warning = Color(0xFFFFB700)             // Warning (orange)
        val error = Color(0xFFFF3B30)               // Error (red)
        val info = Color(0xFF00D9FF)                // Info (cyan)
        
        val success_light = Color(0xFF00FF41).copy(alpha = 0.15f)
        val warning_light = Color(0xFFFFB700).copy(alpha = 0.15f)
        val error_light = Color(0xFFFF3B30).copy(alpha = 0.15f)
        val info_light = Color(0xFF00D9FF).copy(alpha = 0.15f)
    }
    
    // ========== DYNAMIC TRACK PALETTES ==========
    // Para visualizador reactivo
    object TrackPalettes {
        // Bass-focused palette (basshead favorite)
        val bass_gradient_start = Color(0xFF00FF41)
        val bass_gradient_end = Color(0xFF00D9FF)
        
        // Hi-Res palette (audiophile favorite)
        val hires_gradient_start = Color(0xFFFFD700)
        val hires_gradient_end = Color(0xFF9D4EDD)
        
        // Electronic palette
        val electronic_gradient_start = Color(0xFFFF006E)
        val electronic_gradient_end = Color(0xFF00D9FF)
        
        // Organic palette
        val organic_gradient_start = Color(0xFF9D4EDD)
        val organic_gradient_end = Color(0xFFFFD700)
    }
    
    // ========== OVERLAY & SEMANTIC ==========
    object Overlay {
        val scrim_dark = Color(0xFF000000).copy(alpha = 0.32f)
        val scrim_light = Color(0xFF000000).copy(alpha = 0.16f)
        val hover = Color(0xFFFFFFFF).copy(alpha = 0.08f)
        val pressed = Color(0xFFFFFFFF).copy(alpha = 0.12f)
        val focus = Color(0xFF00D9FF).copy(alpha = 0.1f)
    }
}

// Legacy Material3 compat (keep for transitional use)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

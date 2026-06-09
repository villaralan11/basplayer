package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Track
import kotlin.math.cos
import kotlin.math.sin

// Reusable Glassmorphic Card container
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shapeValue: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0x13FFFFFF),
                shape = shapeValue
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x35FFFFFF),
                        Color(0x05FFFFFF)
                    )
                ),
                shape = shapeValue
            )
            .padding(16.dp)
    ) {
        content()
    }
}

// Reusable Glassmorphic Panel button/row
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shapeValue: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0x0CFFFFFF),
                shape = shapeValue
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x22FFFFFF),
                        Color(0x02FFFFFF)
                    )
                ),
                shape = shapeValue
            )
    ) {
        content()
    }
}

// Organic Liquid-Glass background drawing with continuous morphing ovals
@Composable
fun LiquidGlassBackground(
    currentTrack: Track?,
    modifier: Modifier = Modifier
) {
    // Dynamically retrieve color palette according to track ID
    val colors = remember(currentTrack) {
        getPaletteForTrack(currentTrack)
    }

    val primaryColor = colors.first
    val secondaryColor = colors.second
    val tertiaryColor = colors.third

    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_loop")

    // Morphing animations for Circle 1
    val animProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "c1"
    )

    // Morphing animations for Circle 2
    val animProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "c2"
    )

    // Morphing animations for Circle 3
    val animProgress3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(19000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "c3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070709)) // Deep physical carbon noise baseline
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            // Calculate organic slow swirling motion pathways
            val c1x = w * 0.45f + cos(animProgress1) * w * 0.25f
            val c1y = h * 0.40f + sin(animProgress1 * 0.8f) * h * 0.15f
            val c1r = w * 0.45f + sin(animProgress1) * w * 0.05f

            val c2x = w * 0.60f + sin(animProgress2) * w * 0.20f
            val c2y = h * 0.65f + cos(animProgress2 * 1.2f) * h * 0.20f
            val c2r = w * 0.50f + cos(animProgress2) * w * 0.08f

            val c3x = w * 0.25f + cos(animProgress3 * 0.7f) * w * 0.18f
            val c3y = h * 0.75f + sin(animProgress3) * h * 0.18f
            val c3r = w * 0.38f + sin(animProgress3 * 1.5f) * w * 0.06f

            // Draw fluid gradients with enlarged radii for natural blur effect
            val radiusScale = 1.5f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, Color.Transparent),
                    center = Offset(c1x, c1y),
                    radius = c1r * radiusScale
                ),
                center = Offset(c1x, c1y),
                radius = c1r * radiusScale
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor, Color.Transparent),
                    center = Offset(c2x, c2y),
                    radius = c2r * radiusScale
                ),
                center = Offset(c2x, c2y),
                radius = c2r * radiusScale
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(tertiaryColor, Color.Transparent),
                    center = Offset(c3x, c3y),
                    radius = c3r * radiusScale
                ),
                center = Offset(c3x, c3y),
                radius = c3r * radiusScale
            )
        }
    }
}

// Maps specific master track specs to appropriate visual color vibrations
fun getPaletteForTrack(track: Track?): Triple<Color, Color, Color> {
    if (track == null) {
        return Triple(Color(0xFF311B92), Color(0xFF006064), Color(0xFF004D40))
    }
    return when {
        track.id.contains("track_0") -> Triple(
            Color(0xFFB71C1C), // Deep Red
            Color(0xFFE65100), // Pyro Orange
            Color(0xFFF57F17)  // Sulfur Yellow
        )
        track.id.contains("track_1") -> Triple(
            Color(0xFF4A148C), // Cyber Purple
            Color(0xFF00E676), // Fluor Acid Green
            Color(0xFF0D47A1)  // Electric Blue
        )
        track.id.contains("track_2") -> Triple(
            Color(0xFF00B0FF), // Ice Cobalt
            Color(0xFFF50057), // Hot Pink highlights
            Color(0xFFFFD600)  // Gold Sparkle
        )
        track.id.contains("track_3") -> Triple(
            Color(0xFFD50000), // Critical Red
            Color(0xFF2979FF), // Sky Blue
            Color(0xFFFFEA00)  // Golden Lime
        )
        track.id.contains("track_4") -> Triple(
            Color(0xFF00E5FF), // Pure Liquid Cyan
            Color(0xFF6200EA), // Intense Violet
            Color(0xFFC51162)  // Deep Magenta
        )
        // Scanned tracks color
        else -> Triple(
            Color(0xFF311B92), // Deep Indigo
            Color(0xFF00E5FF), // Cyan
            Color(0xFF4A148C)  // Dark Purple
        )
    }
}

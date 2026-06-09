package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BassPlayerColors
import com.example.ui.theme.ExtendedTypography
import com.example.ui.theme.LocalBassPlayerThemeTokens

/**
 * PREMIUM GLASS MORPHIC COMPONENTS
 * Designed for: Epicenter Bass & Hi-Res/Hi-Fi Community
 * Aesthetics: Telegram-style, iOS-native, liquid glass
 */

// ========== GLASS BUTTON VARIANTS ==========

/**
 * Premium Glass Button with multiple variants
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    icon: androidx.compose.material.icons.materialIcon.MaterialIcon? = null,
    enabled: Boolean = true,
    variant: GlassButtonVariant = GlassButtonVariant.FILLED,
    size: GlassButtonSize = GlassButtonSize.MEDIUM,
    isLoading: Boolean = false
) {
    val tokens = LocalBassPlayerThemeTokens.current
    val theme = MaterialTheme
    
    val backgroundColor = when (variant) {
        GlassButtonVariant.FILLED -> BassPlayerColors.Glass.dark_elevated
        GlassButtonVariant.OUTLINED -> Color.Transparent
        GlassButtonVariant.ACCENT -> BassPlayerColors.Primary.gradient_start.copy(alpha = 0.2f)
    }
    
    val borderColor = when (variant) {
        GlassButtonVariant.OUTLINED -> BassPlayerColors.Glass.dark_border
        GlassButtonVariant.ACCENT -> BassPlayerColors.Primary.gradient_start
        else -> Color.Transparent
    }
    
    val (width, height, fontSize) = when (size) {
        GlassButtonSize.SMALL -> Triple(48.dp, 36.dp, 12.sp)
        GlassButtonSize.MEDIUM -> Triple(120.dp, 44.dp, 14.sp)
        GlassButtonSize.LARGE -> Triple(160.dp, 56.dp, 16.sp)
    }
    
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(tokens.cornerRadiusMedium.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.08f),
                        backgroundColor
                    )
                ),
                shape = RoundedCornerShape(tokens.cornerRadiusMedium.dp)
            )
            .border(
                width = if (variant == GlassButtonVariant.OUTLINED) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(tokens.cornerRadiusMedium.dp)
            )
            .blur(radius = 2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = BassPlayerColors.Text.primary
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = BassPlayerColors.Text.primary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                if (label.isNotEmpty()) {
                    Text(label, fontSize = fontSize)
                }
            }
        }
    }
}

enum class GlassButtonVariant {
    FILLED, OUTLINED, ACCENT
}

enum class GlassButtonSize {
    SMALL, MEDIUM, LARGE
}

// ========== GLASS CARD ==========

/**
 * Premium Glass Card with blur, gradient border, and glow effect
 */
@Composable
fun GlassPremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glassEffect: GlassEffect = GlassEffect.NORMAL,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalBassPlayerThemeTokens.current
    
    val isClickable = onClick != null
    val clickableModifier = if (isClickable) {
        modifier
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = ripple(
                    color = BassPlayerColors.Overlay.hover,
                    bounded = true
                )
            ) { onClick?.invoke() }
    } else {
        modifier
    }
    
    Box(
        modifier = clickableModifier
            .clip(RoundedCornerShape(tokens.cornerRadiusLarge.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = when (glassEffect) {
                        GlassEffect.NORMAL -> listOf(
                            BassPlayerColors.Glass.dark_bg.copy(alpha = 0.1f),
                            BassPlayerColors.Glass.dark_surface
                        )
                        GlassEffect.PREMIUM -> listOf(
                            BassPlayerColors.Glass.premium_bg.copy(alpha = 0.2f),
                            BassPlayerColors.Glass.premium_glow.copy(alpha = 0.08f)
                        )
                        GlassEffect.GLOWING -> listOf(
                            BassPlayerColors.Primary.surface.copy(alpha = 0.15f),
                            BassPlayerColors.Glass.dark_surface.copy(alpha = 0.8f)
                        )
                    }
                ),
                shape = RoundedCornerShape(tokens.cornerRadiusLarge.dp)
            )
            .border(
                width = tokens.glassBorderWidth.dp,
                color = when (glassEffect) {
                    GlassEffect.NORMAL -> BassPlayerColors.Glass.dark_border
                    GlassEffect.PREMIUM -> BassPlayerColors.Glass.premium_border
                    GlassEffect.GLOWING -> BassPlayerColors.Primary.gradient_start.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(tokens.cornerRadiusLarge.dp)
            )
            .then(
                if (glassEffect == GlassEffect.GLOWING) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(tokens.cornerRadiusLarge.dp),
                        spotColor = BassPlayerColors.Primary.gradient_start.copy(alpha = 0.3f)
                    )
                } else {
                    Modifier
                }
            )
            .blur(radius = 2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        content = content
    )
}

enum class GlassEffect {
    NORMAL, PREMIUM, GLOWING
}

// ========== VERTICAL EQ SLIDER ==========

/**
 * Vertical slider for EQ bands with frequency label and dB display
 */
@Composable
fun VerticalEQSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    frequencyLabel: String = "1kHz",
    range: ClosedFloatingPointRange<Float> = -15f..15f,
    onValueChangeFinished: () -> Unit = {}
) {
    val tokens = LocalBassPlayerThemeTokens.current
    
    Column(
        modifier = modifier
            .width(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // dB Display
        Text(
            text = "${value.toInt()}dB",
            style = ExtendedTypography.dbValue,
            color = when {
                value > 5 -> BassPlayerColors.Text.cyan
                value < -5 -> BassPlayerColors.Text.magenta
                else -> BassPlayerColors.Text.secondary
            },
            modifier = Modifier.height(20.dp),
            maxLines = 1
        )
        
        // Vertical Slider
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range,
            modifier = Modifier
                .height(150.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            track = { sliderState ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    BassPlayerColors.Secondary.gradient_start.copy(alpha = 0.4f),
                                    BassPlayerColors.Glass.dark_surface,
                                    BassPlayerColors.Primary.gradient_start.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .border(
                            1.dp,
                            BassPlayerColors.Glass.dark_border,
                            RoundedCornerShape(2.dp)
                        )
                )
            },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            color = when {
                                value > 5 -> BassPlayerColors.Primary.gradient_start
                                value < -5 -> BassPlayerColors.Secondary.gradient_start
                                else -> BassPlayerColors.Accent.gold
                            },
                            shape = RoundedCornerShape(10.dp)
                        )\n                        .shadow(4.dp, RoundedCornerShape(10.dp))\n                )
            }
        )
        
        // Frequency Label
        Text(
            text = frequencyLabel,
            style = ExtendedTypography.frequencyLabel,
            color = BassPlayerColors.Text.tertiary,
            maxLines = 1
        )
    }
}

// ========== PRESET PILL BUTTON ==========

/**
 * Horizontal preset selector pills (Classic, Basshead, Clarity, etc.)
 */
@Composable
fun PresetPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalBassPlayerThemeTokens.current
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            BassPlayerColors.Primary.gradient_start.copy(alpha = 0.3f),
                            BassPlayerColors.Secondary.gradient_start.copy(alpha = 0.2f)
                        )
                    )
                } else {
                    Brush.solid(BassPlayerColors.Glass.dark_elevated)
                },
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) {
                    BassPlayerColors.Primary.gradient_start
                } else {
                    BassPlayerColors.Glass.dark_border
                },
                shape = RoundedCornerShape(18.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isSelected) {
                BassPlayerColors.Primary.gradient_start
            } else {
                BassPlayerColors.Text.secondary
            }
        ),
        elevation = ButtonDefaults.elevation(0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.sp
        )
    }
}

// ========== FLOATING ACTION BUTTON GLASS ==========

/**
 * Glass-morphic FAB with premium design
 */
@Composable
fun GlassFAB(
    onClick: () -> Unit,
    icon: androidx.compose.material.icons.materialIcon.MaterialIcon,
    modifier: Modifier = Modifier,
    glowEffect: Boolean = true
) {
    val tokens = LocalBassPlayerThemeTokens.current
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .then(
                if (glowEffect) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(tokens.cornerRadiusMedium.dp),
                        spotColor = BassPlayerColors.Primary.gradient_start.copy(alpha = 0.5f)
                    )
                } else {
                    Modifier
                }
            ),
        containerColor = BassPlayerColors.Glass.dark_elevated,
        contentColor = BassPlayerColors.Primary.gradient_start,
        shape = RoundedCornerShape(tokens.cornerRadiusMedium.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
    }
}
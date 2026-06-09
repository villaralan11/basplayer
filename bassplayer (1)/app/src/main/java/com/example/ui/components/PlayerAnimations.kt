package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.theme.GlassmorphicContainers

/**
 * Professional Audio Player UI Components with:
 * - Smooth animated transitions
 * - Glassmorphism design
 * - Reactive real-time visualizer
 * - Drag-to-expand Mini Player
 */

// ========== MINI PLAYER (Collapsed State) ==========

@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    onExpand: () -> Unit,
    onPlayToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val rotation = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "album_art_rotation"
    )

    GlassmorphicContainer(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) {  // Drag up to expand
                        onExpand()
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Album art with rotation animation
            AsyncImage(
                model = track.artworkUri,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isPlaying) {
                            Modifier.rotate(rotation)
                        } else {
                            Modifier
                        }
                    ),
                contentScale = ContentScale.Crop
            )

            // Track info with marquee effect
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            // Playback controls (animated)
            IconButton(
                onClick = onPlayToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next track",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ========== FULL PLAYER SCREEN (Expanded State) ==========

@Composable
fun FullPlayerScreen(
    track: Track?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    frequencyBands: FloatArray = FloatArray(10),
    bassPunchIntensity: Float = 0f,
    onPlayToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val rotation = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "full_player_rotation"
    )

    val bassPunchScale = animateFloatAsState(
        targetValue = 1f + (bassPunchIntensity * 0.15f),
        animationSpec = tween(150),
        label = "bass_punch_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = "Collapse player",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Now Playing",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // Visualizer (reactive to audio)
            AudioVisualizerDisplay(
                frequencyBands = frequencyBands,
                bassPunchIntensity = bassPunchIntensity,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // Album art with bass punch animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .fillMaxSize()
                        .scaleIn(
                            animationSpec = tween(500),
                            initialScale = 0.85f
                        )
                        .then(
                            if (isPlaying) {
                                Modifier
                                    .rotate(rotation)
                                    .scale(bassPunchScale.value)
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop
                )

                // Pulsing glow effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f * bassPunchIntensity)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Track info
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // Progress bar
            ProgressSlider(
                currentPosition = currentPosition,
                duration = duration
            )

            Spacer(modifier = Modifier.weight(0.15f))

            // Playback controls (large)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                FloatingActionButton(
                    onClick = onPlayToggle,
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

// ========== AUDIO VISUALIZER DISPLAY ==========

@Composable
fun AudioVisualizerDisplay(
    frequencyBands: FloatArray = FloatArray(10),
    bassPunchIntensity: Float = 0f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.background(Color(0xFF1a1a2e).copy(alpha = 0.5f))) {
        val barWidth = size.width / frequencyBands.size
        val baseColor = Color(0xFF00D9FF)
        val bassColor = Color(0xFFFF006E)

        for (i in frequencyBands.indices) {
            val height = frequencyBands[i] * size.height
            val x = i * barWidth
            val y = size.height - height

            // Bass frequencies (0-3 bands) glow on punch
            val color = if (i < 3) {
                bassColor.copy(
                    alpha = (0.5f + bassPunchIntensity * 0.5f)
                        .coerceIn(0f, 1f)
                )
            } else {
                baseColor.copy(alpha = frequencyBands[i])
            }

            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, height)
            )
        }
    }
}

// ========== PROGRESS SLIDER ==========

@Composable
fun ProgressSlider(
    currentPosition: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    onPositionChange: (Long) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { newValue ->
                onPositionChange((newValue * duration).toLong())
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp
            )
            Text(
                text = formatTime(duration),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp
            )
        }
    }
}

// ========== GLASSMORPHIC CONTAINER ==========

@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .blur(radius = 2.dp)
    ) {
        content()
    }
}

// ========== UTILITY FUNCTIONS ==========

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 60000) % 60
    val hours = ms / 3600000
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

@Composable
fun rotate(angle: Float): Modifier = Modifier.rotate(angle)

// Canvas extension for graphics
@Composable
fun Canvas(
    modifier: Modifier = Modifier,
    onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}

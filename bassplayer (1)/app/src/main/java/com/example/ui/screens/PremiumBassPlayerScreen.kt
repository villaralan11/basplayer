package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * PREMIUM BASS PLAYER SCREEN - LIQUID GLASS EDITION
 * Design: Telegram-style iOS × Android × Premium Audio
 * Target: Epicenter Bass & Hi-Res/Hi-Fi Community
 */

@Composable
fun PremiumBassPlayerScreen(viewModel: MainViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    
    // ========== STATE COLLECTION ==========
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    val frequencyBands by viewModel.frequencyBands.collectAsState()
    val bassPunchIntensity by viewModel.bassPunchIntensity.collectAsState()
    
    val eqGains by viewModel.eqGainsList.collectAsState()
    val subBass by viewModel.subBassSlider.collectAsState()
    val clarity by viewModel.claritySlider.collectAsState()
    
    // UI State
    var isPlayerExpanded by remember { mutableStateOf(false) }
    var showDSPPanel by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) } // 0: Now Playing, 1: EQ, 2: Library
    
    // ========== ANIMATIONS ==========
    val playerScale by animateFloatAsState(
        targetValue = if (isPlayerExpanded) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.8f)
    )
    
    val dspPanelOffset by animateDpAsState(
        targetValue = if (showDSPPanel) 0.dp else 200.dp,
        animationSpec = spring(dampingRatio = 0.8f)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BassPlayerColors.Backgrounds.premium_dark)
    ) {
        // ========== LIQUID BACKGROUND WITH MORPHING SHAPES ==========
        LiquidGlassBackground(
            modifier = Modifier.fillMaxSize(),
            bassIntensity = bassPunchIntensity
        )
        
        // ========== MAIN CONTENT PAGER ==========
        when (currentPage) {
            0 -> NowPlayingPagePremium(
                track = currentTrack,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                frequencyBands = frequencyBands,
                bassPunchIntensity = bassPunchIntensity,
                isExpanded = isPlayerExpanded,
                onPlayToggle = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipPrevious() },
                onNext = { viewModel.skipNext() },
                onExpand = { isPlayerExpanded = !isPlayerExpanded },
                modifier = Modifier.fillMaxSize()
            )
            1 -> EqualizerPagePremium(
                eqGains = eqGains,
                subBass = subBass,
                clarity = clarity,
                onEqChange = { band, gain -> viewModel.setEqBandGain(band, gain) },
                onSubBassChange = { viewModel.setSubBassBoost(it) },
                onClarityChange = { viewModel.setClarityBoost(it) },
                modifier = Modifier.fillMaxSize()
            )
            2 -> LibraryPagePremium(
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // ========== BOTTOM TAB NAVIGATOR ==========
        BottomTabNavigator(
            currentPage = currentPage,
            onPageChange = { currentPage = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
        
        // ========== DSP PANEL BOTTOM SHEET ==========
        if (showDSPPanel) {
            PremiumDSPBottomSheet(
                isVisible = showDSPPanel,
                onDismiss = { showDSPPanel = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = dspPanelOffset)
            )
        }
    }
}

// ========== PAGE 1: NOW PLAYING ==========

@Composable
fun NowPlayingPagePremium(
    track: Track?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    frequencyBands: FloatArray,
    bassPunchIntensity: Float,
    isExpanded: Boolean,
    onPlayToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return
    
    val rotation = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val bassPulse = animateFloatAsState(
        targetValue = 1f + (bassPunchIntensity * 0.1f),
        animationSpec = tween(150)
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ========== HEADER ==========
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Menu",
                    tint = BassPlayerColors.Text.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "Now Playing",
                style = MaterialTheme.typography.headlineSmall,
                color = BassPlayerColors.Text.primary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                    contentDescription = "Expand",
                    tint = BassPlayerColors.Text.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(0.15f))
        
        // ========== VISUALIZER ==========
        PremiumVisualizerCard(
            frequencyBands = frequencyBands,
            bassPunchIntensity = bassPunchIntensity,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        
        Spacer(modifier = Modifier.weight(0.1f))
        
        // ========== ALBUM ART WITH GLASS EFFECT ==========
        GlassPremiumCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(32.dp)),
            glassEffect = GlassEffect.GLOWING
        ) {
            AsyncImage(
                model = track.artworkUri,
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation)
                    .scaleIn(
                        animationSpec = tween(500),
                        initialScale = 0.85f
                    )
                    .then(
                        if (isPlaying) {
                            Modifier.scale(bassPulse.value)
                        } else {
                            Modifier
                        }
                    ),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.weight(0.1f))
        
        // ========== TRACK INFO ==========
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall,
                color = BassPlayerColors.Text.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = BassPlayerColors.Text.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(0.12f))
        
        // ========== PROGRESS BAR ==========
        PremiumProgressBar(
            currentPosition = currentPosition,
            duration = duration,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.weight(0.12f))
        
        // ========== PLAYBACK CONTROLS ==========
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassButton(
                onClick = onPrevious,
                icon = Icons.Filled.SkipPrevious,
                size = GlassButtonSize.MEDIUM,
                variant = GlassButtonVariant.OUTLINED
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            GlassFAB(
                onClick = onPlayToggle,
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                glowEffect = true
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            GlassButton(
                onClick = onNext,
                icon = Icons.Filled.SkipNext,
                size = GlassButtonSize.MEDIUM,
                variant = GlassButtonVariant.OUTLINED
            )
        }
        
        Spacer(modifier = Modifier.weight(0.15f))
    }
}

// ========== PAGE 2: EQUALIZER ==========

@Composable
fun EqualizerPagePremium(
    eqGains: List<Float>,
    subBass: Float,
    clarity: Float,
    onEqChange: (Int, Float) -> Unit,
    onSubBassChange: (Float) -> Unit,
    onClarityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalBassPlayerThemeTokens.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ========== HEADER ==========
        Text(
            "Equalizer",
            style = MaterialTheme.typography.displayMedium,
            color = BassPlayerColors.Text.primary,
            fontWeight = FontWeight.Bold
        )
        
        // ========== 10-BAND EQ ==========
        GlassPremiumCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            glassEffect = GlassEffect.NORMAL
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "10-Band Graphic EQ",
                    style = MaterialTheme.typography.titleMedium,
                    color = BassPlayerColors.Text.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val frequencies = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
                    eqGains.forEachIndexed { index, gain ->
                        VerticalEQSlider(
                            value = gain,
                            onValueChange = { onEqChange(index, it) },
                            frequencyLabel = frequencies[index]
                        )
                    }
                }
            }
        }
        
        // ========== BASS BOOST CARD ==========
        GlassPremiumCard(
            modifier = Modifier.fillMaxWidth(),
            glassEffect = GlassEffect.GLOWING
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sub-Bass Boost",
                        style = MaterialTheme.typography.titleMedium,
                        color = BassPlayerColors.Text.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Badge(
                        modifier = Modifier.background(
                            color = BassPlayerColors.Status.success.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            "${(subBass * 100).toInt()}%",
                            color = BassPlayerColors.Status.success,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(4.dp, 2.dp)
                        )
                    }
                }
                
                Slider(
                    value = subBass,
                    onValueChange = onSubBassChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    "20Hz - 60Hz | Basshead Focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = BassPlayerColors.Text.tertiary
                )
            }
        }
        
        // ========== CLARITY BOOST CARD ==========
        GlassPremiumCard(
            modifier = Modifier.fillMaxWidth(),
            glassEffect = GlassEffect.NORMAL
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Clarity Boost",
                        style = MaterialTheme.typography.titleMedium,
                        color = BassPlayerColors.Text.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Badge(
                        modifier = Modifier.background(
                            color = BassPlayerColors.Primary.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                    ) {
                        Text(
                            "${(clarity * 100).toInt()}%",
                            color = BassPlayerColors.Primary.gradient_start,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(4.dp, 2.dp)
                        )
                    }
                }
                
                Slider(
                    value = clarity,
                    onValueChange = onClarityChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    "8kHz - 16kHz | Presence & Air",
                    style = MaterialTheme.typography.bodySmall,
                    color = BassPlayerColors.Text.tertiary
                )
            }
        }
    }
}

// ========== PAGE 3: LIBRARY (Placeholder) ==========

@Composable
fun LibraryPagePremium(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Library Coming Soon",
            style = MaterialTheme.typography.headlineMedium,
            color = BassPlayerColors.Text.primary
        )
    }
}

// ========== VISUALIZER CARD ==========

@Composable
fun PremiumVisualizerCard(
    frequencyBands: FloatArray,
    bassPunchIntensity: Float,
    modifier: Modifier = Modifier
) {
    GlassPremiumCard(
        modifier = modifier,
        glassEffect = if (bassPunchIntensity > 0.5f) GlassEffect.GLOWING else GlassEffect.PREMIUM
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width / frequencyBands.size
            val baseColor = BassPlayerColors.Primary.gradient_start
            val bassColor = BassPlayerColors.Secondary.gradient_start
            
            for (i in frequencyBands.indices) {
                val height = frequencyBands[i] * size.height
                val x = i * barWidth
                val y = size.height - height
                
                val color = if (i < 3) {
                    bassColor.copy(
                        alpha = (0.5f + bassPunchIntensity * 0.5f).coerceIn(0f, 1f)
                    )
                } else {
                    baseColor.copy(alpha = frequencyBands[i])
                }
                
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(x + 4.dp.toPx(), y),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, height.coerceAtLeast(1f))
                )
            }
        }
    }
}

// ========== PROGRESS BAR ==========

@Composable
fun PremiumProgressBar(
    currentPosition: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { newValue ->
                onSeek((newValue * duration).toLong())
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
                style = MaterialTheme.typography.labelSmall,
                color = BassPlayerColors.Text.secondary
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.labelSmall,
                color = BassPlayerColors.Text.secondary
            )
        }
    }
}

// ========== BOTTOM TAB NAVIGATOR ==========

@Composable
fun BottomTabNavigator(
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalBassPlayerThemeTokens.current
    
    GlassPremiumCard(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(12.dp),
        glassEffect = GlassEffect.PREMIUM
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple(Icons.Filled.MusicNote, "Now Playing", 0),
                Triple(Icons.Filled.EqualizerSettings, "EQ", 1),
                Triple(Icons.Filled.LibraryMusic, "Library", 2)
            ).forEach { (icon, label, page) ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cornerRadiusMedium.dp))
                        .clickable { onPageChange(page) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = if (currentPage == page) {
                            BassPlayerColors.Primary.gradient_start
                        } else {
                            BassPlayerColors.Text.secondary
                        }
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (currentPage == page) {
                            BassPlayerColors.Primary.gradient_start
                        } else {
                            BassPlayerColors.Text.secondary
                        },
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ========== DSP BOTTOM SHEET ==========

@Composable
fun PremiumDSPBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    GlassPremiumCard(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(12.dp),
        glassEffect = GlassEffect.PREMIUM
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Advanced DSP",
                    style = MaterialTheme.typography.headlineSmall,
                    color = BassPlayerColors.Text.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = BassPlayerColors.Text.primary
                    )
                }
            }
            
            Text(
                "Pitch • Tempo • Virtualizer • Reverb controls coming soon",
                style = MaterialTheme.typography.bodySmall,
                color = BassPlayerColors.Text.secondary
            )
        }
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

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
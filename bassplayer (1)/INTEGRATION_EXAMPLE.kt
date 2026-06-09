/**
 * EJEMPLO DE INTEGRACIÓN COMPLETA - PlayerScreens.kt
 * 
 * Este archivo muestra cómo integrar todos los componentes profesionales
 * de BassPlayer en una pantalla Compose completamente funcional.
 * 
 * Nota: Este es un ejemplo de referencia. Actualiza tu PlayerScreens.kt existente
 * con los patrones aquí mostrados.
 */

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.*

@Composable
fun BassPlayerMainScreen(viewModel: MainViewModel = viewModel()) {
    // ========== UI STATE ==========
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    // Audio Visualization (FFT real-time)
    val frequencyBands by viewModel.frequencyBands.collectAsState()
    val bassPunchIntensity by viewModel.bassPunchIntensity.collectAsState()
    
    // DSP Controls
    val pitchValue by viewModel.pitchValue.collectAsState()
    val tempoValue by viewModel.tempoValue.collectAsState()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsState()
    val reverbIntensity by viewModel.reverbIntensity.collectAsState()
    val subBass by viewModel.subBassSlider.collectAsState()
    val eqGains by viewModel.eqGainsList.collectAsState()
    
    // Navigation
    var isFullPlayer by remember { mutableStateOf(false) }
    var showDSPPanel by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main Content
        if (isFullPlayer) {
            // Full Player Screen with FFT Visualizer
            FullPlayerScreen(
                track = currentTrack,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                frequencyBands = frequencyBands,
                bassPunchIntensity = bassPunchIntensity,
                onPlayToggle = { viewModel.togglePlayPause() },
                onPrevious = { viewModel.skipPrevious() },
                onNext = { viewModel.skipNext() },
                onCollapse = { isFullPlayer = false }
            )
        } else {
            // List + Mini Player
            Column(modifier = Modifier.fillMaxSize()) {
                // Playlist (scrollable)
                TrackListView(
                    tracks = emptyList(),  // Populate from viewModel.tracks
                    onTrackClick = { viewModel.playTrack(it) },
                    currentTrack = currentTrack,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                
                // Mini Player (sticky bottom)
                MiniPlayer(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    onExpand = { isFullPlayer = true },
                    onPlayToggle = { viewModel.togglePlayPause() },
                    onNext = { viewModel.skipNext() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Floating DSP Control Button
        FloatingActionButton(
            onClick = { showDSPPanel = !showDSPPanel },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "DSP Settings"
            )
        }
    }
    
    // DSP Control Panel Modal
    if (showDSPPanel) {
        DSPControlModal(
            viewModel = viewModel,
            onDismiss = { showDSPPanel = false },
            pitchValue = pitchValue,
            tempoValue = tempoValue,
            virtualizerStrength = virtualizerStrength,
            reverbIntensity = reverbIntensity,
            subBass = subBass,
            eqGains = eqGains
        )
    }
}

// ========== MINI PLAYER WITH ENHANCED CONTROLS ==========

@Composable
fun MiniPlayerEnhanced(
    track: Track?,
    isPlaying: Boolean,
    bassPunchIntensity: Float = 0f,
    frequencyBands: FloatArray = FloatArray(10),
    onExpand: () -> Unit,
    onPlayToggle: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return
    
    // Mini visualizer in the mini player
    val miniVizHeight = 12.dp
    
    GlassmorphicContainer(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onExpand() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mini Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(miniVizHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                AudioVisualizerDisplay(
                    frequencyBands = frequencyBands,
                    bassPunchIntensity = bassPunchIntensity
                )
            }
            
            // Player Info & Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Album art (smaller)
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Track info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = track.artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Controls
                IconButton(onClick = onPlayToggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ========== DSP CONTROL MODAL ==========

@Composable
fun DSPControlModal(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    pitchValue: Float,
    tempoValue: Float,
    virtualizerStrength: Float,
    reverbIntensity: Float,
    subBass: Float,
    eqGains: List<Float>,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced DSP Controls") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ===== PITCH & TEMPO =====
                GroupLabel("Pitch & Tempo")
                
                SliderWithLabel(
                    label = "Pitch",
                    value = pitchValue,
                    range = 0.5f..2.0f,
                    onValueChange = { viewModel.setPitch(it) },
                    formatter = { "%.2f x".format(it) }
                )
                
                SliderWithLabel(
                    label = "Tempo",
                    value = tempoValue,
                    range = 0.5f..2.0f,
                    onValueChange = { viewModel.setTempo(it) },
                    formatter = { "%.2f x".format(it) }
                )
                
                // ===== SPATIAL EFFECTS =====
                GroupLabel("Spatial Effects")
                
                SliderWithLabel(
                    label = "Virtualizer",
                    value = virtualizerStrength,
                    range = 0f..1f,
                    onValueChange = { viewModel.setVirtualizer(it) },
                    formatter = { "${(it * 100).toInt()}%" }
                )
                
                SliderWithLabel(
                    label = "Reverb",
                    value = reverbIntensity,
                    range = 0f..1f,
                    onValueChange = { viewModel.setReverb(it) },
                    formatter = { "${(it * 100).toInt()}%" }
                )
                
                // ===== BASS BOOST =====
                GroupLabel("Bass Boost")
                
                SliderWithLabel(
                    label = "Sub-Bass",
                    value = subBass,
                    range = 0f..1f,
                    onValueChange = { viewModel.setSubBassBoost(it) },
                    formatter = { "${(it * 100).toInt()}%" }
                )
                
                // ===== 10-BAND EQ (COMPACT) =====
                GroupLabel("Equalizer (10-Band)")
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(eqGains.size) { bandIndex ->
                        EQBandSlider(
                            bandIndex = bandIndex,
                            value = eqGains[bandIndex],
                            onValueChange = { viewModel.setEqBandGain(bandIndex, it) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// ========== UI COMPONENTS ==========

@Composable
fun GroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    formatter: (Float) -> String = { it.toString() }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                formatter(value),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EQBandSlider(
    bandIndex: Int,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val frequencies = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                frequencies[bandIndex],
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp
            )
            Text(
                "${value.toInt()}dB",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -15f..15f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TrackListView(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    currentTrack: Track?,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(tracks) { track ->
            TrackItemRow(
                track = track,
                isActive = track.id == currentTrack?.id,
                onClick = { onTrackClick(track) }
            )
        }
    }
}

@Composable
fun TrackItemRow(
    track: Track,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art thumbnail
        AsyncImage(
            model = track.artworkUri,
            contentDescription = "Album art",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        
        // Track info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Duration
        Text(
            text = formatTrackDuration(track.durationMs),
            style = MaterialTheme.typography.labelSmall
        )
        
        // Favorite indicator
        if (track.isFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                modifier = Modifier.size(20.dp),
                tint = Color.Red
            )
        }
    }
}

// ========== UTILITY FUNCTIONS ==========

private fun formatTrackDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 60000) % 60
    return "%d:%02d".format(minutes, seconds)
}

// ========== IMPORTS NEEDED ==========
/*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.MainViewModel
import com.example.data.model.Track
*/

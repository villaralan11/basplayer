package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.EqPreset
import com.example.data.model.Track
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPanel
import com.example.ui.components.getPaletteForTrack
import kotlinx.coroutines.delay

@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.tracks.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    var showOnlyFavorites by remember { mutableStateOf(false) }
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val displayedTracks = if (showOnlyFavorites) favoriteTracks else tracks

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title and Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Biblioteca",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sonido Hi-Res Local",
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Favorites filter button
                IconButton(
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    modifier = Modifier
                        .clip(CircleShape)
                        .shadow(4.dp)
                        .testTag("favorites_filter_button")
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Show Favorites",
                        tint = if (showOnlyFavorites) Color(0xFFFF2D55) else Color.White
                    )
                }

                // Scan trigger
                Button(
                    onClick = { viewModel.scanAndLoadSongs() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x35FFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("scan_storage_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escaneando...", fontSize = 12.sp, color = Color.White)
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = "Scan", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escanear", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        val errorState by viewModel.errorState.collectAsState()
        if (errorState != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x33FF3B30)),
                modifier = Modifier.fillMaxWidth().testTag("error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = errorState!!,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close error", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (displayedTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = "Empty music",
                            tint = Color(0x99FFFFFF),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (showOnlyFavorites) "No hay favoritos registrados" else "Almacenamiento Vacío",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showOnlyFavorites) "Toca el ícono de corazón en Now Playing para agregar canciones." else "Copia archivos .FLAC o .WAV a tu dispositivo o toca 'Escanear' para generar señales acústicas de prueba.",
                            color = Color(0xFFB3B3B3),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(displayedTracks) { track ->
                    val isPlaying = currentTrack?.id == track.id
                    val paletteColors = getPaletteForTrack(track)
                    val accentColor = paletteColors.first

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playTrack(track) }
                            .testTag("track_item_${track.id}"),
                        shapeValue = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Synthesized artwork indicator
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    imageVector = if (isPlaying) Icons.Filled.MusicNote else Icons.Outlined.QueueMusic,
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            if (isPlaying) {
                                                // Decorative shadow accent
                                                shadowElevation = 8f
                                            }
                                        }
                                )
                                // Ambient glass artwork hue overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .graphicsLayer {
                                            alpha = 0.45f
                                        }
                                        .clickable { viewModel.playTrack(track) }
                                        .graphicsLayer {
                                            // Extract background color dynamically
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = if (isPlaying) accentColor else Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    color = Color(0xFFB3B3B3),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Hi-Res Dynamic badge tags
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        accentColor.copy(alpha = 0.25f),
                                                        accentColor.copy(alpha = 0.05f)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = track.format,
                                            color = accentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }

                                    Text(
                                        text = "${track.bitDepth}-bit / ${track.sampleRate / 1000}kHz",
                                        color = Color(0x99FFFFFF),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = track.durationString,
                                color = Color(0x99FFFFFF),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NowPlayingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    val paletteColors = getPaletteForTrack(currentTrack)
    val accentColor = paletteColors.first

    // Infinite beat effect simulating a dynamic bass pulsating ripple
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_art")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Floating Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 44.dp)
        ) {
            Text(
                text = "AHORA REPRODUCIENDO",
                color = Color(0x8CFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Precision Signal Rate indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Adjust,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Gold badge
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HI-RES AUDIO DIRECT LINK",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Center album artwork with frosted reflections and bass-pulsing shadow
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = accentColor.copy(alpha = 0.85f),
                        ambientColor = accentColor.copy(alpha = 0.35f)
                    )
                    .background(Color(0x1A000000)),
                contentAlignment = Alignment.Center
            ) {
                // Glass-cut physical record mock
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0x18FFFFFF), Color(0x3BFFFFFF))
                            )
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Album,
                            contentDescription = "Vinyl",
                            tint = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.size(160.dp)
                        )
                    }
                }

                // Small center hi-res emblem
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF212121), Color(0xFF090909))
                            ),
                            shape = CircleShape
                        )
                        .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.7f), CircleShape)
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("24 BIT", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text("MQA", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Track Text Metadata
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentTrack?.title ?: "Sin reproducir",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTrack?.artist ?: "Selecciona una pista",
                        color = Color(0xFFB3B3B3),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite action
                IconButton(
                    onClick = { currentTrack?.let { viewModel.toggleFavorite(it) } },
                    modifier = Modifier.testTag("favorite_toggle")
                ) {
                    Icon(
                        imageVector = if (currentTrack?.isFavorite == true) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentTrack?.isFavorite == true) Color(0xFFFF2D55) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File properties strip (e.g. 192kHz)
            currentTrack?.let {
                Text(
                    text = "${it.format} • Direct Stream • ${it.bitDepth}-bit/${it.sampleRate / 1000}kHz",
                    color = accentColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seek timeline sliders
        Column(modifier = Modifier.fillMaxWidth()) {
            val progress = remember(position, duration) {
                if (duration > 0) position.toFloat() / duration else 0f
            }

            Slider(
                value = progress,
                onValueChange = {
                    if (duration > 0) {
                        viewModel.seekTo((it * duration).toLong())
                    }
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0x35FFFFFF),
                    thumbColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_progress_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatProgress(position),
                    color = Color(0x80FFFFFF),
                    fontSize = 12.sp
                )
                Text(
                    text = formatProgress(duration),
                    color = Color(0x80FFFFFF),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // High contrast vector player operations button set
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 110.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous track
            FilledIconButton(
                onClick = { viewModel.skipPrevious() },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0x13FFFFFF)),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("skip_previous_button")
            ) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(26.dp))
            }

            // Play / Pause Circle
            Button(
                onClick = { viewModel.togglePlayPause() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .shadow(16.dp, CircleShape, spotColor = accentColor)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next track
            FilledIconButton(
                onClick = { viewModel.skipNext() },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0x13FFFFFF)),
                modifier = Modifier
                    .size(56.dp)
                    .testTag("skip_next_button")
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
fun EqualizerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val eqGains by viewModel.eqGainsList.collectAsState()
    val subBassFactor by viewModel.subBassSlider.collectAsState()
    val clarityFactor by viewModel.claritySlider.collectAsState()
    val presetsList by viewModel.presets.collectAsState()
    val activePresetId by viewModel.activePresetId.collectAsState()

    var customPresetName by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title and Save preset block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EQ de Precisión",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ecualización Paramétrica DSP",
                    color = Color(0xFFB3B3B3),
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("save_preset_button")
            ) {
                Icon(Icons.Filled.AddCard, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Presets scroll catalog
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presetsList) { preset ->
                val isActive = preset.id == activePresetId
                Text(
                    text = preset.name,
                    color = if (isActive) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .background(
                            brush = if (isActive) {
                                Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0x13FFFFFF), Color(0x13FFFFFF)))
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.selectPreset(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("preset_pill_${preset.id}")
                )
            }
        }

        // Custom EQ Sliders container block over beautiful Glass Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = "CONTROLES GRÁFICOS (10 BANDAS)",
                    color = Color(0x99FFFFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable vertical sliders row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(10) { index ->
                        val gainDb = eqGains.getOrElse(index) { 0.0f }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(56.dp)
                                .fillMaxHeight()
                                .testTag("eq_band_$index")
                        ) {
                            Text(
                                text = String.format("%+.1fdB", gainDb),
                                color = if (gainDb != 0.0f) Color(0xFFFFD700) else Color(0x80FFFFFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Vertical Slider Box wrapper
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background vertical bar track
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight(0.75f)
                                        .background(Color(0x18FFFFFF), RoundedCornerShape(2.dp))
                                )

                                Slider(
                                    value = gainDb,
                                    onValueChange = { viewModel.setEqBandGain(index, it) },
                                    valueRange = -15f..15f,
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = Color(0xFFFFD700),
                                        inactiveTrackColor = Color.Transparent,
                                        thumbColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .rotate(-90f)
                                        .width(135.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = bandLabels[index],
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Basshead Sub-bass / Clarity crystal sliders
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Algoritmo Sub-bass compressor slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Speaker, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BASSHEAD SUB-BASS (20Hz - 60Hz)",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "${(subBassFactor * 100).toInt()}% (+${String.format("%.1f", subBassFactor * 16.5f)}dB)",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = subBassFactor,
                        onValueChange = { viewModel.setSubBassBoost(it) },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFFFF5252),
                            inactiveTrackColor = Color(0x35FFFFFF),
                            thumbColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sub_bass_slider")
                    )

                    Text(
                        text = "Ajusta la presencia física del bajo y activa el compresor analógico.",
                        color = Color(0xFFB3B3B3),
                        fontSize = 10.sp
                    )
                }

                // Algoritmo Clarity slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Hearing, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CRISTALIZADOR CLARIDAD (8kHz - 16kHz)",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "${(clarityFactor * 100).toInt()}% (+${String.format("%.1f", clarityFactor * 12.0f)}dB)",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = clarityFactor,
                        onValueChange = { viewModel.setClarityBoost(it) },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0x35FFFFFF),
                            thumbColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clarity_slider")
                    )

                    Text(
                        text = "Añade separación instrumental y brillo armónico en frecuencias ultra altas.",
                        color = Color(0xFFB3B3B3),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // Modal popup to name and commit custom presets
    if (showSaveDialog) {
        Dialog(onDismissRequest = { showSaveDialog = false }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Guardar Preset Customizado",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    TextField(
                        value = customPresetName,
                        onValueChange = { customPresetName = it },
                        placeholder = { Text("Ej: Subwoofer Master") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x22FFFFFF),
                            unfocusedContainerColor = Color(0x13FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preset_name_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showSaveDialog = false }
                        ) {
                            Text("Cancelar", color = Color(0x99FFFFFF))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (customPresetName.isNotBlank()) {
                                    viewModel.saveCustomPreset(customPresetName)
                                    customPresetName = ""
                                    showSaveDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Quick helpers
fun formatProgress(ms: Long): String {
    val totalSeconds = ms / 1000
    val min = totalSeconds / 60
    val sec = totalSeconds % 60
    return String.format("%02d:%02d", min, sec)
}

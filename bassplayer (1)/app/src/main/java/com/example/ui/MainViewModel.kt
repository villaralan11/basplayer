package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.room.Room
import com.example.audio.AudioVisualizerManager
import com.example.audio.PlaybackService
import com.example.data.database.AppDatabase
import com.example.data.model.EqPreset
import com.example.data.model.Track
import com.example.data.repository.TrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Enhanced ViewModel with real-time audio visualization and advanced DSP controls
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "bass_player_db"
    ).build()

    private val repository = TrackRepository(
        application,
        database.trackDao(),
        database.presetDao()
    )

    // Audio Visualizer
    private val _visualizerManager = AudioVisualizerManager(viewModelScope)
    val visualizerManager = _visualizerManager

    // ========== PLAYBACK STATES ==========
    val tracks = repository.allTracks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favoriteTracks = repository.favoriteTracks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val presets = repository.allPresets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> get() = _isScanning

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> get() = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> get() = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> get() = _duration

    // ========== EQUALIZER STATES ==========
    private val _eqGainsList = MutableStateFlow(List(10) { 0.0f })
    val eqGainsList: StateFlow<List<Float>> get() = _eqGainsList

    private val _subBassSlider = MutableStateFlow(0.0f)
    val subBassSlider: StateFlow<Float> get() = _subBassSlider

    private val _claritySlider = MutableStateFlow(0.0f)
    val claritySlider: StateFlow<Float> get() = _claritySlider

    private val _activePresetId = MutableStateFlow("flat")
    val activePresetId: StateFlow<String> get() = _activePresetId

    // ========== ADVANCED DSP STATES ==========
    // Pitch & Tempo (independent control via PlaybackParameters)
    private val _pitchValue = MutableStateFlow(1.0f)  // 0.5f to 2.0f
    val pitchValue: StateFlow<Float> get() = _pitchValue

    private val _tempoValue = MutableStateFlow(1.0f)  // 0.5f to 2.0f
    val tempoValue: StateFlow<Float> get() = _tempoValue

    // Spatial Effects
    private val _virtualizerStrength = MutableStateFlow(0.0f)  // 0f to 1f
    val virtualizerStrength: StateFlow<Float> get() = _virtualizerStrength

    private val _reverbIntensity = MutableStateFlow(0.0f)     // 0f to 1f
    val reverbIntensity: StateFlow<Float> get() = _reverbIntensity

    // ========== VISUALIZER STATES ==========
    private val _frequencyBands = MutableStateFlow(FloatArray(10))
    val frequencyBands: StateFlow<FloatArray> get() = _frequencyBands

    private val _bassPunchIntensity = MutableStateFlow(0.0f)
    val bassPunchIntensity: StateFlow<Float> get() = _bassPunchIntensity

    private val _isVisualizerActive = MutableStateFlow(false)
    val isVisualizerActive: StateFlow<Boolean> get() = _isVisualizerActive

    private var currentTracksList: List<Track> = emptyList()

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrentTrack()
        }

        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val service = PlaybackService.activeInstance ?: return
            _duration.value = service.player.duration.coerceAtLeast(0L)
        }
    }

    init {
        // Start background playback service
        val intent = Intent(application, PlaybackService::class.java)
        application.startService(intent)

        viewModelScope.launch {
            repository.initializeDefaultPresets()
            scanAndLoadSongs()
            loadLastPreset()
            observePlayerState()
            observeVisualizer()
        }

        viewModelScope.launch {
            tracks.collect {
                currentTracksList = it
                if (_currentTrack.value == null && it.isNotEmpty()) {
                    _currentTrack.value = it.first()
                }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            while (true) {
                val service = PlaybackService.activeInstance
                if (service != null) {
                    val player = service.player
                    _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                    _duration.value = player.duration.coerceAtLeast(0L)
                    _isPlaying.value = player.isPlaying

                    // Automatically attach listener if not attached
                    player.removeListener(playerListener)
                    player.addListener(playerListener)

                    // Wire DSP error reporting to UI
                    service.dspManager.onDspError = { msg ->
                        _errorState.value = msg
                    }

                    // Listen for playback parameter changes from DSP
                    service.dspManager.onPlaybackParametersChanged = { params ->
                        player.playbackParameters = params
                        _pitchValue.value = params.pitch
                        _tempoValue.value = params.speed
                    }

                    // Sync DSP active parameters on service wakeup
                    service.dspManager.setEqGains(_eqGainsList.value.toFloatArray())
                    service.dspManager.setSubBass(_subBassSlider.value)
                    service.dspManager.setClarity(_claritySlider.value)
                    service.dspManager.setPitch(_pitchValue.value)
                    service.dspManager.setTempo(_tempoValue.value)
                    service.dspManager.setVirtualizer(_virtualizerStrength.value)
                    service.dspManager.setReverb(_reverbIntensity.value)
                }
                delay(500)
            }
        }
    }

    /**
     * Observe real-time audio visualization data from FFT processor
     * Runs on background thread internally, safe to consume in UI
     */
    private fun observeVisualizer() {
        viewModelScope.launch {
            visualizerManager.frequencyBands.collect { data ->
                _frequencyBands.value = data.bands
            }
        }

        viewModelScope.launch {
            visualizerManager.bassPunch.collect { event ->
                _bassPunchIntensity.value = event.intensity
                // Fade out bass punch visual after 200ms
                delay(200)
                if (_bassPunchIntensity.value == event.intensity) {
                    _bassPunchIntensity.value = 0f
                }
            }
        }
    }

    fun scanAndLoadSongs() {
        viewModelScope.launch {
            _isScanning.value = true
            _errorState.value = null
            try {
                repository.scanStorageForAudio()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorState.value = "Error al escanear música local: ${e.localizedMessage ?: e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearError() {
        _errorState.value = null
    }

    private suspend fun loadLastPreset() {
        val savedPreset = repository.allPresets.first().find { it.id == "basshead" }
        savedPreset?.let { selectPreset(it) }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch(Dispatchers.Main) {
            val service = PlaybackService.activeInstance ?: return@launch
            val player = service.player

            player.clearMediaItems()
            val listToUse = currentTracksList.ifEmpty { tracks.value }
            val index = listToUse.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

            listToUse.forEach { t ->
                player.addMediaItem(MediaItem.fromUri(t.uriString))
            }

            player.seekTo(index, 0L)
            player.prepare()
            player.play()

            // Initialize visualizer for this audio session
            if (!_visualizerManager.isInitialized()) {
                val audioSessionId = player.audioSessionId
                if (audioSessionId != 0) {
                    _visualizerManager.initialize(audioSessionId)
                    _isVisualizerActive.value = true
                }
            }

            _currentTrack.value = track
            _isPlaying.value = true
        }
    }

    fun togglePlayPause() {
        val service = PlaybackService.activeInstance ?: return
        val player = service.player
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.mediaItemCount == 0 && _currentTrack.value != null) {
                playTrack(_currentTrack.value!!)
            } else {
                player.play()
            }
        }
        _isPlaying.value = player.isPlaying
    }

    fun skipNext() {
        val service = PlaybackService.activeInstance ?: return
        val player = service.player
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        } else if (currentTracksList.isNotEmpty()) {
            player.seekTo(0, 0L)
        }
        syncCurrentTrack()
    }

    fun skipPrevious() {
        val service = PlaybackService.activeInstance ?: return
        val player = service.player
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        } else if (currentTracksList.isNotEmpty()) {
            player.seekTo(currentTracksList.size - 1, 0L)
        }
        syncCurrentTrack()
    }

    fun seekTo(positionMs: Long) {
        val service = PlaybackService.activeInstance ?: return
        service.player.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
            // Sync active track's favorite state
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = track.copy(isFavorite = !track.isFavorite)
            }
        }
    }

    // ========== EQUALIZER CONTROLS ==========
    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        val mutableGains = _eqGainsList.value.toMutableList()
        mutableGains[bandIndex] = gainDb.coerceIn(-15.0f, 15.0f)
        _eqGainsList.value = mutableGains

        val service = PlaybackService.activeInstance
        service?.dspManager?.setEqGains(mutableGains.toFloatArray())
        _activePresetId.value = "custom"
    }

    fun setSubBassBoost(gain: Float) {
        val value = gain.coerceIn(0.0f, 1.0f)
        _subBassSlider.value = value

        val service = PlaybackService.activeInstance
        service?.dspManager?.setSubBass(value)
        _activePresetId.value = "custom"
    }

    fun setClarityBoost(gain: Float) {
        val value = gain.coerceIn(0.0f, 1.0f)
        _claritySlider.value = value

        val service = PlaybackService.activeInstance
        service?.dspManager?.setClarity(value)
        _activePresetId.value = "custom"
    }

    // ========== PITCH & TEMPO CONTROLS ==========
    /**
     * Set pitch (formant-preserving time-stretch)
     * Range: 0.5f to 2.0f (does NOT change playback speed)
     */
    fun setPitch(pitch: Float) {
        val value = pitch.coerceIn(0.5f, 2.0f)
        _pitchValue.value = value

        val service = PlaybackService.activeInstance ?: return
        service.dspManager.setPitch(value)
        // Apply to player
        val currentParams = service.player.playbackParameters
        service.player.playbackParameters = PlaybackParameters(currentParams.speed, value)
    }

    /**
     * Set tempo (playback speed without pitch change)
     * Range: 0.5f to 2.0f (does NOT change pitch)
     */
    fun setTempo(tempo: Float) {
        val value = tempo.coerceIn(0.5f, 2.0f)
        _tempoValue.value = value

        val service = PlaybackService.activeInstance ?: return
        service.dspManager.setTempo(value)
        // Apply to player
        val currentParams = service.player.playbackParameters
        service.player.playbackParameters = PlaybackParameters(value, currentParams.pitch)
    }

    // ========== SPATIAL EFFECTS CONTROLS ==========
    /**
     * Set virtualizer strength (spatial width enhancement)
     * Range: 0f (no effect) to 1f (maximum)
     */
    fun setVirtualizer(strength: Float) {
        val value = strength.coerceIn(0.0f, 1.0f)
        _virtualizerStrength.value = value

        val service = PlaybackService.activeInstance
        service?.dspManager?.setVirtualizer(value)
        _activePresetId.value = "custom"
    }

    /**
     * Set reverb intensity (spatial depth/ambience)
     * Range: 0f (none) to 1f (maximum)
     */
    fun setReverb(intensity: Float) {
        val value = intensity.coerceIn(0.0f, 1.0f)
        _reverbIntensity.value = value

        val service = PlaybackService.activeInstance
        service?.dspManager?.setReverb(value)
        _activePresetId.value = "custom"
    }

    // ========== PRESET MANAGEMENT ==========
    fun selectPreset(preset: EqPreset) {
        viewModelScope.launch {
            val list = preset.getGainsList()
            _eqGainsList.value = list
            _subBassSlider.value = preset.subBassGain
            _claritySlider.value = preset.clarityGain
            _activePresetId.value = preset.id

            val service = PlaybackService.activeInstance ?: return@launch
            service.dspManager.setEqGains(list.toFloatArray())
            service.dspManager.setSubBass(preset.subBassGain)
            service.dspManager.setClarity(preset.clarityGain)
        }
    }

    fun saveCustomPreset(name: String) {
        viewModelScope.launch {
            val gainsStr = EqPreset.serializeGains(_eqGainsList.value)
            val newPreset = EqPreset(
                id = "custom_" + System.currentTimeMillis(),
                name = name,
                bandGains = gainsStr,
                subBassGain = _subBassSlider.value,
                clarityGain = _claritySlider.value,
                isSystem = false
            )
            repository.savePreset(newPreset)
            _activePresetId.value = newPreset.id
        }
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            repository.deletePreset(id)
            if (_activePresetId.value == id) {
                _activePresetId.value = "flat"
                val flat = presets.value.firstOrNull { it.id == "flat" }
                if (flat != null) {
                    selectPreset(flat)
                }
            }
        }
    }

    private fun syncCurrentTrack() {
        val service = PlaybackService.activeInstance ?: return
        val index = service.player.currentMediaItemIndex
        val listToUse = currentTracksList.ifEmpty { tracks.value }
        if (index in listToUse.indices) {
            _currentTrack.value = listToUse[index]
        }
    }

    override fun onCleared() {
        val service = PlaybackService.activeInstance
        service?.player?.removeListener(playerListener)
        _visualizerManager.release()
        super.onCleared()
    }
}

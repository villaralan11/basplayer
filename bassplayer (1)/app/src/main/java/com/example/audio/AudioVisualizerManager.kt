package com.example.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.*

/**
 * Real-time audio visualizer using FFT (Fast Fourier Transform)
 * Runs on background dispatcher to prevent UI thread blocking
 *
 * Features:
 * - Frequency spectrum extraction via FFT
 * - Bass punch detection (20Hz-60Hz sub-bass focus)
 * - 10-band visual representation
 * - Non-blocking background thread execution
 * - Reactive data stream via Kotlin Flow
 */
class AudioVisualizerManager(private val scope: CoroutineScope) {
    private var visualizer: Visualizer? = null
    private var fftTask: Job? = null

    private val _frequencyBands = MutableSharedFlow<FrequencyData>(replay = 1)
    val frequencyBands: Flow<FrequencyData> = _frequencyBands.asSharedFlow()

    // Bass punch detection
    private val _bassPunch = MutableSharedFlow<BassPunchEvent>(replay = 1)
    val bassPunch: Flow<BassPunchEvent> = _bassPunch.asSharedFlow()

    private val TAG = "AudioVisualizer"

    // Configuration
    private val visualizationBands = 10
    private val fftUpdateRateMs = 50L  // 50ms = 20 FPS
    private var isEnabled = false

    // Bass detection
    private var lastBassPunchTime = 0L
    private var lastBassLevel = 0f
    private val bassPunchThreshold = 0.7f
    private val bassPunchCooldownMs = 200L  // Min time between bass punches

    /**
     * Initialize visualizer with audio session ID
     */
    fun initialize(audioSessionId: Int): Boolean {
        return try {
            visualizer = Visualizer(audioSessionId).apply {
                enabled = false
                captureSize = Visualizer.getMaxCaptureSize()
                Log.d(TAG, "Visualizer initialized - Capture size: ${captureSize}")
            }
            isEnabled = true
            startFFTProcessing()
            Log.d(TAG, "✓ AudioVisualizer ready")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize visualizer: ${e.message}")
            false
        }
    }

    /**
     * Start background FFT processing loop
     * Runs on Default dispatcher (multi-threaded) to avoid main thread blocking
     */
    private fun startFFTProcessing() {
        stopFFTProcessing()  // Cancel any existing task

        fftTask = scope.launch(Dispatchers.Default) {
            val fftBuffer = ByteArray(0)
            var lastUpdateTime = System.currentTimeMillis()

            while (isActive && isEnabled && visualizer != null) {
                try {
                    val currentTime = System.currentTimeMillis()

                    // Rate-limit updates to prevent excessive UI updates
                    if (currentTime - lastUpdateTime >= fftUpdateRateMs) {
                        processFFT()
                        lastUpdateTime = currentTime
                    }

                    // Yield to prevent thread starvation
                    yield()
                    delay(5)  // Small sleep to allow other coroutines to run

                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "FFT processing error: ${e.message}")
                }
            }
        }
    }

    /**
     * Process FFT data and extract frequency spectrum
     * This runs on background thread - NO MAIN THREAD ACCESS
     */
    private suspend fun processFFT() {
        val viz = visualizer ?: return

        try {
            viz.enabled = true

            // Get FFT data from audio buffer
            val fftData = viz.fft  // Byte array of FFT magnitude data
            if (fftData.isEmpty()) return

            // Extract 10-band frequency spectrum
            val bandData = extractFrequencyBands(fftData)

            // Emit frequency data (safe - Flow handles thread switching)
            _frequencyBands.emit(FrequencyData(
                bands = bandData,
                timestamp = System.currentTimeMillis(),
                peakFrequency = findPeakFrequency(bandData)
            ))

            // Detect bass punch (kick drum)
            detectBassPunch(bandData)

        } catch (e: Exception) {
            Log.w(TAG, "FFT extraction error: ${e.message}")
        }
    }

    /**
     * Extract 10 bands from FFT data
     * FFT output is split into frequency bands for visualization
     */
    private fun extractFrequencyBands(fftData: ByteArray): FloatArray {
        val bands = FloatArray(visualizationBands)

        // Nyquist frequency = Sample rate / 2
        // Typical: 44.1kHz -> Nyquist = 22.05kHz
        // FFT bins are linearly distributed across frequency range

        val fftSize = fftData.size
        val binWidth = fftSize / visualizationBands

        for (band in 0 until visualizationBands) {
            val startBin = band * binWidth
            val endBin = (band + 1) * binWidth

            // Calculate average magnitude for this band
            var sum = 0
            for (bin in startBin until endBin) {
                if (bin < fftData.size) {
                    // Convert byte to unsigned int and accumulate
                    sum += (fftData[bin].toInt() and 0xFF)
                }
            }

            // Normalize to 0-1 range
            val average = (sum / (endBin - startBin)) / 256f
            bands[band] = average.coerceIn(0f, 1f)
        }

        // Apply logarithmic scale for better visual representation
        // (low frequencies appear more prominent)
        return bands.mapIndexed { index, value ->
            // Bass frequencies (0-2 bands) get amplified
            val boost = if (index < 3) 1.5f else 1.0f
            val logValue = if (value > 0) {
                log10(value * 10f + 1f) / log10(11f)  // log compression
            } else {
                0f
            }
            (logValue * boost).coerceIn(0f, 1f)
        }.toFloatArray()
    }

    /**
     * Detect bass punch (kick drum) from sub-bass frequencies
     * Used to highlight visual feedback during bass-heavy moments
     */
    private suspend fun detectBassPunch(bandData: FloatArray) {
        if (bandData.isEmpty()) return

        val currentTime = System.currentTimeMillis()

        // Bass punch typically occurs in first 2-3 bands (20-250Hz)
        val bassBands = bandData.take(3).average()

        // Detect sudden spike in bass level
        val isPunch = (bassBands > bassPunchThreshold) &&
                (bassBands > lastBassLevel * 1.5f) &&  // At least 50% increase
                (currentTime - lastBassPunchTime > bassPunchCooldownMs)

        if (isPunch) {
            lastBassPunchTime = currentTime
            _bassPunch.emit(BassPunchEvent(
                intensity = bassBands,
                timestamp = currentTime
            ))
            Log.d(TAG, "🔊 Bass punch detected: ${(bassBands * 100).toInt()}%")
        }

        lastBassLevel = bassBands
    }

    /**
     * Find the dominant frequency band
     */
    private fun findPeakFrequency(bandData: FloatArray): Int {
        return if (bandData.isNotEmpty()) {
            bandData.indices.maxByOrNull { bandData[it] } ?: 0
        } else {
            0
        }
    }

    /**
     * Stop FFT processing
     */
    private fun stopFFTProcessing() {
        fftTask?.cancel()
        fftTask = null
    }

    /**
     * Release all resources
     */
    fun release() {
        isEnabled = false
        stopFFTProcessing()
        visualizer?.let {
            try {
                it.enabled = false
                it.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing visualizer: ${e.message}")
            }
        }
        visualizer = null
        Log.d(TAG, "Visualizer released")
    }

    fun isInitialized(): Boolean = visualizer != null && isEnabled

    // ========== DATA CLASSES ==========

    data class FrequencyData(
        val bands: FloatArray,        // 10 frequency bands (0-1f each)
        val timestamp: Long,          // When this data was captured
        val peakFrequency: Int        // Index of dominant frequency band
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FrequencyData) return false
            return bands.contentEquals(other.bands) && timestamp == other.timestamp
        }

        override fun hashCode(): Int {
            return bands.contentHashCode() + timestamp.hashCode()
        }
    }

    data class BassPunchEvent(
        val intensity: Float,         // 0-1f
        val timestamp: Long
    )
}

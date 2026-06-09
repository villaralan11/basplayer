package com.example.audio

import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import androidx.media3.common.PlaybackParameters

/**
 * Professional DSP Manager for high-fidelity audio processing:
 * - 10-band Graphic/Parametric Equalizer
 * - Advanced Bass Boost (20-60Hz sub-bass focus)
 * - Independent Pitch & Tempo control via PlaybackParameters
 * - Virtualizer for spatial enhancement
 * - Environmental Reverb for spatial ambience
 * - Multi-band compressor with peak limiter (anti-clipping)
 */
class DspManager {
    private var dynamicsProcessing: DynamicsProcessing? = null
    private var virtualizer: Virtualizer? = null
    private var reverb: EnvironmentalReverb? = null

    // Pitch and Tempo tracking
    private var currentPitch = 1.0f   // 0.5f to 2.0f
    private var currentTempo = 1.0f   // 0.5f to 2.0f

    var onDspError: ((String) -> Unit)? = null
    var onPlaybackParametersChanged: ((PlaybackParameters) -> Unit)? = null

    // 10-band graphic equalizer frequencies (Hz)
    private val bandFrequencies = floatArrayOf(
        31f, 62f, 125f, 250f, 500f,
        1000f, 2000f, 4000f, 8000f, 16000f
    )

    private var currentGains = FloatArray(10) { 0f }
    private var subBassFactor = 0.0f     // 0f to 1f (bass boost intensity)
    private var clarityFactor = 0.0f     // 0f to 1f (treble presence)
    private var virtualizerStrength = 0.0f  // 0f to 1f (spatial width)
    private var reverbIntensity = 0.0f      // 0f to 1f (spatial depth)

    private val TAG = "DspManager"

    @Synchronized
    fun onAudioSessionId(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            release()

            // ========== DYNAMICS PROCESSING (EQ + Compressor + Limiter) ==========
            val dpBuilder = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                2,     // Stereo channels
                true, 10, // Pre-EQ enabled with 10 bands
                true, 2,  // Multiband compressor (MBC) with 2 bands
                true, 10, // Post-EQ enabled with 10 bands
                true      // Peak limiter enabled
            )

            val dpConfig = dpBuilder.build()
            val dp = DynamicsProcessing(0, audioSessionId, dpConfig)
            dp.enabled = true
            dynamicsProcessing = dp
            Log.d(TAG, "✓ DynamicsProcessing initialized")

            // ========== VIRTUALIZER (Spatial Enhancement) ==========
            try {
                virtualizer = Virtualizer(0, audioSessionId)
                virtualizer?.enabled = true
                virtualizer?.strength = (virtualizerStrength * 1000).toShort()
                Log.d(TAG, "✓ Virtualizer initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Virtualizer not available: ${e.message}")
                virtualizer = null
            }

            // ========== ENVIRONMENTAL REVERB (Spatial Ambience) ==========
            try {
                reverb = EnvironmentalReverb(0, audioSessionId)
                reverb?.enabled = true
                updateReverbSettings()
                Log.d(TAG, "✓ EnvironmentalReverb initialized")
            } catch (e: Exception) {
                Log.w(TAG, "EnvironmentalReverb not available: ${e.message}")
                reverb = null
            }

            applyAllSettings()
            Log.d(TAG, "✓ DSP chain fully initialized")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize DSP: ${e.message}", e)
            onDspError?.invoke("Motor DSP error: ${e.message}")
        }
    }

    @Synchronized
    fun release() {
        try {
            dynamicsProcessing?.let {
                it.enabled = false
                it.release()
            }
            virtualizer?.let {
                it.enabled = false
                it.release()
            }
            reverb?.let {
                it.enabled = false
                it.release()
            }
            dynamicsProcessing = null
            virtualizer = null
            reverb = null
            Log.d(TAG, "DSP resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing DSP: ${e.message}")
        }
    }

    // ========== CONTROL API ==========

    fun setEqGains(gains: FloatArray) {
        if (gains.size == 10) {
            currentGains = gains.clone()
            applyAllSettings()
        }
    }

    fun setSubBass(factor: Float) {
        subBassFactor = factor.coerceIn(0f, 1f)
        applyAllSettings()
    }

    fun setClarity(factor: Float) {
        clarityFactor = factor.coerceIn(0f, 1f)
        applyAllSettings()
    }

    /**
     * Set independent pitch (formant-preserving time-stretch)
     * Range: 0.5f to 2.0f
     * Does NOT affect playback speed
     */
    fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        notifyPlaybackParametersChanged()
    }

    /**
     * Set independent tempo (playback speed without pitch change)
     * Range: 0.5f to 2.0f
     * Does NOT affect pitch
     */
    fun setTempo(tempo: Float) {
        currentTempo = tempo.coerceIn(0.5f, 2.0f)
        notifyPlaybackParametersChanged()
    }

    /**
     * Set virtualizer strength (spatial width enhancement)
     * Range: 0f (no effect) to 1f (maximum width)
     */
    fun setVirtualizer(strength: Float) {
        virtualizerStrength = strength.coerceIn(0f, 1f)
        virtualizer?.let {
            try {
                it.strength = (virtualizerStrength * 1000).toShort()
                Log.d(TAG, "Virtualizer set to ${(virtualizerStrength * 100).toInt()}%")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting virtualizer: ${e.message}")
            }
        }
    }

    /**
     * Set reverb intensity (spatial depth/ambience)
     * Range: 0f (none) to 1f (maximum)
     */
    fun setReverb(intensity: Float) {
        reverbIntensity = intensity.coerceIn(0f, 1f)
        updateReverbSettings()
    }

    /**
     * Get current playback parameters for integration with ExoPlayer
     */
    fun getPlaybackParameters(): PlaybackParameters {
        return PlaybackParameters(currentTempo, currentPitch)
    }

    // ========== INTERNAL DSP APPLICATION ==========

    private fun applyAllSettings() {
        val dp = dynamicsProcessing ?: return
        try {
            for (channel in 0..1) {
                // ========== STAGE 1: PRE-EQ (Graphic Equalizer 10-band) ==========
                applyGraphicEqualizer(dp, channel)

                // ========== STAGE 2: MULTIBAND COMPRESSOR (Bass-focused) ==========
                applyMultiBandCompressor(dp, channel)

                // ========== STAGE 3: POST-EQ (Treble enhancement optional) ==========
                applyPostEqualizer(dp, channel)

                // ========== STAGE 4: PEAK LIMITER (Anti-clipping safety) ==========
                applyPeakLimiter(dp, channel)
            }
            Log.d(TAG, "DSP settings applied successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying DSP settings: ${e.message}")
        }
    }

    private fun applyGraphicEqualizer(dp: DynamicsProcessing, channel: Int) {
        val preEq = dp.getPreEqByChannelIndex(channel)
        for (bandIndex in 0..9) {
            val band = preEq.getBand(bandIndex)
            band.cutoffFrequency = bandFrequencies[bandIndex]

            var gain = currentGains[bandIndex]

            // High-frequency presence boost (8kHz - 16kHz) for clarity
            if (bandFrequencies[bandIndex] >= 8000f) {
                gain += clarityFactor * 12.0f
            }

            band.gain = gain.coerceIn(-24.0f, 24.0f)
            preEq.setBand(bandIndex, band)
        }
        dp.setPreEqAllChannelsTo(preEq)
    }

    private fun applyMultiBandCompressor(dp: DynamicsProcessing, channel: Int) {
        val mbc = dp.getMbcByChannelIndex(channel)

        // ===== BAND 0: SUB-BASS MASTERY (20Hz - 60Hz) =====
        // Focused on low-frequency enhancement without distortion
        val subBassBand = mbc.getBand(0)
        subBassBand.cutoffFrequency = 60f // Low-pass filter at 60Hz

        // Dynamic threshold based on bass boost intensity
        subBassBand.threshold = -24f - (subBassFactor * 12f)
        // Compression ratio increases with bass intensity
        subBassBand.ratio = 2.0f + (subBassFactor * 5.0f) // Up to 7:1 ratio

        // Fast attack for transient capture, smooth release for sub-bass tail
        subBassBand.attackTime = 4f      // 4ms - instant transient response
        subBassBand.releaseTime = 75f    // 75ms - smooth decompression

        subBassBand.kneeWidth = 5f       // Smooth knee for natural compression

        // Makeup gain distributed to avoid clipping (split pre/post)
        val bassBoostDb = subBassFactor * 16.5f  // Max +16.5dB
        subBassBand.preGain = bassBoostDb / 2f
        subBassBand.postGain = bassBoostDb / 2f

        mbc.setBand(0, subBassBand)
        Log.d(TAG, "Bass boost: ${(subBassFactor * 100).toInt()}% (${bassBoostDb}dB)")

        // ===== BAND 1: MID/HIGH TRANSPARENCY (60Hz - 20kHz) =====
        // Keep mid/high frequencies relatively uncompressed for clarity
        val midHighBand = mbc.getBand(1)
        midHighBand.cutoffFrequency = 20000f

        midHighBand.threshold = -12f
        midHighBand.ratio = 1.0f         // No compression (unity ratio)
        midHighBand.attackTime = 25f
        midHighBand.releaseTime = 150f

        midHighBand.preGain = 0f
        midHighBand.postGain = 0f

        mbc.setBand(1, midHighBand)

        dp.setMbcAllChannelsTo(mbc)
    }

    private fun applyPostEqualizer(dp: DynamicsProcessing, channel: Int) {
        // Post-EQ for final polish (subtle treble enhancement)
        val postEq = dp.getPostEqByChannelIndex(channel)
        for (bandIndex in 0..9) {
            val band = postEq.getBand(bandIndex)
            band.cutoffFrequency = bandFrequencies[bandIndex]

            // Subtle presence peak at 10kHz for articulation
            if (bandIndex == 8) { // 8kHz band
                band.gain = (clarityFactor * 2.0f).coerceIn(-12.0f, 12.0f)
            } else {
                band.gain = 0f
            }
            postEq.setBand(bandIndex, band)
        }
        dp.setPostEqAllChannelsTo(postEq)
    }

    private fun applyPeakLimiter(dp: DynamicsProcessing, channel: Int) {
        // Peak Limiter: Emergency safety against digital clipping
        val limiter = dp.getLimiterByChannelIndex(channel)

        limiter.threshold = -3.0f         // -3dB headroom
        limiter.attackTime = 0.5f         // Ultra-fast attack (0.5ms)
        limiter.releaseTime = 60f         // Smooth release (60ms)
        limiter.ratio = 10f               // Hard limiting (10:1)
        limiter.postGain = 2.5f           // Make-up gain

        dp.setLimiterAllChannelsTo(limiter)
        Log.d(TAG, "Peak limiter configured: -3dB threshold, 0.5ms attack")
    }

    private fun updateReverbSettings() {
        reverb?.let {
            try {
                if (reverbIntensity == 0f) {
                    it.enabled = false
                } else {
                    it.enabled = true
                    // Map intensity to reverb room size and decay time
                    val roomSizePercent = (reverbIntensity * 100).toShort()
                    val decayTime = (100 + reverbIntensity * 1000).toShort()

                    it.roomLevel = (-6000 + reverbIntensity * 4000).toShort()
                    it.reverbLevel = (-4000 + reverbIntensity * 2000).toShort()
                    it.decayTime = decayTime.coerceIn(100, 7000)

                    Log.d(TAG, "Reverb set to ${(reverbIntensity * 100).toInt()}%")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting reverb: ${e.message}")
            }
        }
    }

    private fun notifyPlaybackParametersChanged() {
        val params = getPlaybackParameters()
        onPlaybackParametersChanged?.invoke(params)
        Log.d(TAG, "Playback params - Tempo: $currentTempo, Pitch: $currentPitch")
    }

    // ========== DEBUG & INFO ==========

    fun getState(): String {
        return """
        === DSP STATE ===
        EQ Gains: ${currentGains.joinToString(",") { String.format("%.1f", it) }}dB
        Bass Boost: ${(subBassFactor * 100).toInt()}%
        Clarity: ${(clarityFactor * 100).toInt()}%
        Pitch: $currentPitch
        Tempo: $currentTempo
        Virtualizer: ${(virtualizerStrength * 100).toInt()}%
        Reverb: ${(reverbIntensity * 100).toInt()}%
        """.trimIndent()
    }
}

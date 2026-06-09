package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eq_presets")
data class EqPreset(
    @PrimaryKey val id: String,
    val name: String,
    val bandGains: String, // Comma-separated floating point values (10 bands)
    val subBassGain: Float, // 0.0f to 1.0f (Sub-bass lowpass + compressor slider)
    val clarityGain: Float, // 0.0f to 1.0f (Crystalizer shelf boost slider)
    val isSystem: Boolean = false
) {
    fun getGainsList(): List<Float> {
        return try {
            bandGains.split(",").map { it.toFloat() }
        } catch (e: Exception) {
            List(10) { 0.0f }
        }
    }

    companion object {
        fun serializeGains(gains: List<Float>): String {
            return gains.joinToString(",")
        }
    }
}

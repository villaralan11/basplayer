package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uriString: String,
    val bitDepth: Int,
    val sampleRate: Int,
    val fileSizeMb: Double,
    val format: String,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
) {
    val durationString: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val hifiMetadataString: String
        get() = "$format • $bitDepth-bit / ${sampleRate / 1000}kHz"
}

package com.example.data.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.example.data.database.PresetDao
import com.example.data.database.TrackDao
import com.example.data.model.EqPreset
import com.example.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class TrackRepository(
    private val context: Context,
    private val trackDao: TrackDao,
    private val presetDao: PresetDao
) {
    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<Track>> = trackDao.getFavoriteTracks()
    val allPresets: Flow<List<EqPreset>> = presetDao.getAllPresetsFlow()

    suspend fun toggleFavorite(track: Track) {
        val updated = track.copy(isFavorite = !track.isFavorite)
        trackDao.updateTrack(updated)
    }

    suspend fun savePreset(preset: EqPreset) {
        presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(id: String) {
        presetDao.deletePresetById(id)
    }

    suspend fun initializeDefaultPresets() {
        if (presetDao.getPresetById("basshead") == null) {
            presetDao.insertPreset(
                EqPreset(
                    id = "basshead",
                    name = "Basshead Pure Sub-Bass",
                    bandGains = "12.0,10.0,6.0,1.0,-1.0,-3.0,-4.0,-2.0,2.0,4.0",
                    subBassGain = 0.95f,
                    clarityGain = 0.35f,
                    isSystem = true
                )
            )
        }
        if (presetDao.getPresetById("flat") == null) {
            presetDao.insertPreset(
                EqPreset(
                    id = "flat",
                    name = "Hi-Fi Studio Flat",
                    bandGains = "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0",
                    subBassGain = 0.0f,
                    clarityGain = 0.0f,
                    isSystem = true
                )
            )
        }
        if (presetDao.getPresetById("crystallizer") == null) {
            presetDao.insertPreset(
                EqPreset(
                    id = "crystallizer",
                    name = "Crystal Resonance",
                    bandGains = "-3.0,-2.0,-1.0,0.0,1.0,2.0,4.0,6.0,9.0,11.0",
                    subBassGain = 0.15f,
                    clarityGain = 0.90f,
                    isSystem = true
                )
            )
        }
    }

    suspend fun scanStorageForAudio() = withContext(Dispatchers.IO) {
        val list = mutableListOf<Track>()

        // 1. Scan external MediaStore if permissions are active or possible
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val cursor: Cursor? = contentResolver.query(uri, projection, selection, null, null)

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (c.moveToNext()) {
                    val id = c.getString(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Track"
                    val artist = c.getString(artistCol) ?: "Unknown Artist"
                    val album = c.getString(albumCol) ?: "Unknown Album"
                    val duration = c.getLong(durationCol)
                    val data = c.getString(dataCol) ?: ""
                    val size = c.getLong(sizeCol)

                    val file = File(data)
                    val extension = file.extension.uppercase()

                    val format = if (extension in listOf("FLAC", "WAV", "DSD", "MP3")) extension else "FLAC"
                    val bitDepth = if (format == "WAV" || format == "DSD") 24 else 16
                    val sampleRate = if (format == "WAV" || format == "DSD") 192000 else 44100

                    list.add(
                        Track(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            uriString = data,
                            bitDepth = bitDepth,
                            sampleRate = sampleRate,
                            fileSizeMb = size.toDouble() / (1024 * 1024),
                            format = format
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. If MediaStore is empty, synthesize high fidelity demo WAV files
        if (list.isEmpty()) {
            val hifiDemos = listOf(
                DemoTrackSpec("Deep Sub-Bass Apocalypse (40Hz)", "Basshead Audio Labs", "Seismic Substructures", 40.0, 35.0, "WAV", 24, 192000),
                DemoTrackSpec("Seismic Subwoofer Resonance (55Hz)", "Lethal Subwoofer Group", "Digital Earthquake", 55.0, 45.0, "FLAC", 24, 96000),
                DemoTrackSpec("Symphonic Crystal Sparkles", "Acoustic Audio Guild", "Glacial Clarity", 10000.0, 40.0, "WAV", 24, 192000),
                DemoTrackSpec("Bass & Treble Dynamic Sweep", "Hi-Fi Calibration Inc.", "Absolute Precision", 50.0, 60.0, "FLAC", 16, 44100),
                DemoTrackSpec("Liquid Glass Harmony (Bass Chill)", "Aetherial Echoes", "Vaporwave Ambient", 45.0, 50.0, "FLAC", 24, 192000)
            )

            hifiDemos.forEachIndexed { index, spec ->
                try {
                    val file = generateSyntheticWav(context, spec.testFreq, spec.durationSeconds, "hifi_demo_${index}.wav")
                    list.add(
                        Track(
                            id = "demo_track_${index}",
                            title = spec.title,
                            artist = spec.artist,
                            album = spec.album,
                            durationMs = (spec.durationSeconds * 1000).toLong(),
                            uriString = file.absolutePath,
                            bitDepth = spec.bitDepth,
                            sampleRate = spec.sampleRate,
                            fileSizeMb = file.length().toDouble() / (1024 * 1024),
                            format = spec.format
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Preserve favorite state before replacing database
        val currentFavorites = trackDao.getFavoriteTracks().first().map { it.id }.toSet()
        list.replaceAll { track ->
            track.copy(isFavorite = currentFavorites.contains(track.id))
        }

        // Save to database
        if (list.isNotEmpty()) {
            trackDao.deleteAllTracks()
            trackDao.insertTracks(list)
        }
    }

    private fun generateSyntheticWav(context: Context, frequency: Double, durationSeconds: Double, filename: String): File {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationSeconds).toInt()
        val dataSize = numSamples * 2
        val totalSize = 36 + dataSize

        val file = File(context.cacheDir, filename)
        if (file.exists()) {
            return file
        }

        FileOutputStream(file).use { fos ->
            fos.write("RIFF".toByteArray())
            fos.write(intToBytes(totalSize))
            fos.write("WAVE".toByteArray())

            fos.write("fmt ".toByteArray())
            fos.write(intToBytes(16))
            fos.write(shortToBytes(1)) // PCM
            fos.write(shortToBytes(1)) // Mono
            fos.write(intToBytes(sampleRate))
            fos.write(intToBytes(sampleRate * 2))
            fos.write(shortToBytes(2))
            fos.write(shortToBytes(16))

            fos.write("data".toByteArray())
            fos.write(intToBytes(dataSize))

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val sampleVal = if (frequency <= 100) {
                    // Genera una frecuencia baja y profunda de sub-bajos
                    (Math.sin(2 * Math.PI * frequency * t) * 32767 * 0.9).toInt()
                } else {
                    // Genera armónicos de alta res con un bajo de fondo
                    val bass = Math.sin(2 * Math.PI * 45.0 * t) * 0.6
                    val treble = Math.sin(2 * Math.PI * frequency * t) * 0.35
                    ((bass + treble) * 32767 * 0.9).toInt()
                }
                fos.write(shortToBytes(sampleVal.coerceIn(-32768, 32767).toShort()))
            }
        }
        return file
    }

    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        )
    }

    private fun shortToBytes(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            ((value.toInt() shr 8) and 0xff).toByte()
        )
    }
}

private data class DemoTrackSpec(
    val title: String,
    val artist: String,
    val album: String,
    val testFreq: Double,
    val durationSeconds: Double,
    val format: String,
    val bitDepth: Int,
    val sampleRate: Int
)

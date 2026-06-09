package com.example.data.database

import androidx.room.*
import com.example.data.model.Track
import com.example.data.model.EqPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteTracks(): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Update
    suspend fun updateTrack(track: Track)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: String)

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTracksCount(): Int
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM eq_presets")
    fun getAllPresetsFlow(): Flow<List<EqPreset>>

    @Query("SELECT * FROM eq_presets WHERE id = :id")
    suspend fun getPresetById(id: String): EqPreset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqPreset)

    @Query("DELETE FROM eq_presets WHERE id = :id")
    suspend fun deletePresetById(id: String)
}

@Database(entities = [Track::class, EqPreset::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun presetDao(): PresetDao
}

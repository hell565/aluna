package com.kl.aluna.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val trackId: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val trackId: Long)

@Entity(tableName = "blacklist")
data class BlacklistEntity(@PrimaryKey val trackId: Long)

@Entity(tableName = "recent")
data class RecentEntity(
    @PrimaryKey val trackId: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "all_tracks")
data class TrackEntity(
    @PrimaryKey val mediaStoreId: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val dateAdded: Long,
    val orderIndex: Int
)

@Dao
interface MusicDao {
    @Query("SELECT * FROM all_tracks ORDER BY orderIndex ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("DELETE FROM all_tracks WHERE mediaStoreId = :id")
    suspend fun deleteTrack(id: Long)

    @Query("SELECT MAX(orderIndex) FROM all_tracks")
    suspend fun getMaxOrderIndex(): Int?

    @Query("SELECT * FROM all_tracks")
    suspend fun getAllTracksSnapshot(): List<TrackEntity>

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId")
    fun getTracksForPlaylist(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("DELETE FROM playlist_tracks WHERE id = :id")
    suspend fun removeTrackFromPlaylistById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackToPlaylist(track: PlaylistTrackEntity)

    @Query("SELECT trackId FROM favorites")
    fun getFavoriteIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM recent ORDER BY timestamp DESC LIMIT 50")
    fun getRecent(): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)

    @Query("SELECT trackId FROM blacklist")
    fun getBlacklistIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklist(blacklist: BlacklistEntity)

    @Delete
    suspend fun deleteBlacklist(blacklist: BlacklistEntity)
}

@Database(entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class, FavoriteEntity::class, RecentEntity::class, BlacklistEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "aluna.db")
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}

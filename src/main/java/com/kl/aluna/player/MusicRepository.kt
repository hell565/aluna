package com.kl.aluna.player

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.kl.aluna.data.db.AppDatabase
import com.kl.aluna.data.db.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object MusicRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var observer: ContentObserver? = null

    fun setupObserver(context: Context, onUpdate: () -> Unit) {
        if (observer != null) return
        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                onUpdate()
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )
    }

    suspend fun syncWithMediaStore(context: Context): List<Track> {
        val db = AppDatabase.getInstance(context)
        val dao = db.musicDao()
        
        val mediaStoreTracks = scanMediaStore(context)
        val dbTracks = dao.getAllTracksSnapshot()
        
        val mediaStoreIds = mediaStoreTracks.map { it.id }.toSet()
        
        // Удалить те, которых нет в MediaStore
        dbTracks.filter { it.mediaStoreId !in mediaStoreIds }.forEach {
            dao.deleteTrack(it.mediaStoreId)
        }
        
        // Добавить новые или обновить существующие для синхронизации метаданных
        val entities = mediaStoreTracks.mapIndexed { index, track ->
            TrackEntity(
                mediaStoreId = track.id,
                uri = track.uri.toString(),
                title = track.title,
                artist = track.artist,
                duration = track.duration,
                dateAdded = track.dateAdded,
                orderIndex = index // Новые (сверху в scanMediaStore из-за DESC) получают индекс 0, 1, 2...
            )
        }
        dao.insertTracks(entities)
        
        return dao.getAllTracksSnapshot().sortedBy { it.orderIndex }.map { it.toTrack() }
    }

    private fun scanMediaStore(context: Context): List<Track> {
        val tracks = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        )
        
        // Фильтрация: Музыка (>30 сек) и корректные форматы через расширение файла
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000"
        // Сортировка: Самые новые по дате добавления сверху
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            
            val musicExtensions = setOf("mp3", "wav", "flac", "ogg", "m4a", "aac", "wma")
            
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol) ?: ""
                val extension = path.substringAfterLast('.', "").lowercase()
                
                if (extension in musicExtensions) {
                    val id = cursor.getLong(idCol)
                    tracks.add(Track(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown",
                        duration = cursor.getLong(durCol),
                        uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString()),
                        dateAdded = cursor.getLong(dateCol)
                    ))
                }
            }
        }
        return tracks
    }

    private fun TrackEntity.toTrack() = Track(
        id = mediaStoreId,
        uri = Uri.parse(uri),
        title = title,
        artist = artist,
        duration = duration,
        dateAdded = dateAdded
    )
}

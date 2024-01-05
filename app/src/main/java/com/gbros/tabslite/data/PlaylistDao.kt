package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getPlaylists(): LiveData<List<Playlist>>

    @Query("UPDATE playlist SET date_modified = :dateModified WHERE id = :playlistId")
    fun updateTimestamp(playlistId: Int, dateModified: Long)

    @Query("UPDATE playlist SET title = :newTitle WHERE id = :playlistId")
    fun updateTitle(playlistId: Int, newTitle: String)

    @Query("UPDATE playlist SET description = :newDescription WHERE id = :playlistId")
    fun updateDescription(playlistId: Int, newDescription: String)

    @Query("SELECT * FROM playlist")
    suspend fun getCurrentPlaylists(): List<Playlist>

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Int): Playlist

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    fun getPlaylistLive(playlistId: Int): LiveData<Playlist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

}
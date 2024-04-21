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
    fun getLivePlaylists(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlist")
    suspend fun getPlaylists(): List<Playlist>

    @Query("UPDATE playlist SET date_modified = :dateModified WHERE id = :playlistId")
    fun updateTimestamp(playlistId: Int, dateModified: Long)

    @Query("UPDATE playlist SET title = :newTitle WHERE id = :playlistId")
    suspend fun updateTitle(playlistId: Int, newTitle: String)

    @Query("UPDATE playlist SET description = :newDescription WHERE id = :playlistId")
    suspend fun updateDescription(playlistId: Int, newDescription: String)

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    fun getLivePlaylist(playlistId: Int): LiveData<Playlist>

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Int): Playlist

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

}
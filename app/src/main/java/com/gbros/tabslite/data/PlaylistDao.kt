package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getPlaylists(): LiveData<List<Playlist>>

    @Query("UPDATE playlist SET date_modified = :dateModified WHERE id = :playlistId")
    fun updateTimestamp(playlistId: Int, dateModified: Long)

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
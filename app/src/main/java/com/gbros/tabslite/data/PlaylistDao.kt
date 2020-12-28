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

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Int): Playlist

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)
}
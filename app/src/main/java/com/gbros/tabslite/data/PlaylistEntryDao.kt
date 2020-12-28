package com.gbros.tabslite.data

import androidx.room.*

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface PlaylistEntryDao {
    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId")
    suspend fun getPlaylistItems(playlistId: Int): List<PlaylistEntry>

    @Query("SELECT playlist_id FROM playlist_entry WHERE tab_id = :tabId")
    suspend fun getPlaylistsForTab(tabId: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PlaylistEntry)

    @Delete
    suspend fun deleteEntry(entry: PlaylistEntry)
}
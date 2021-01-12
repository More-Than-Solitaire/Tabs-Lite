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

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId AND next_entry_id IS NULL")
    suspend fun getLastEntryInPlaylist(playlistId: Int): PlaylistEntry?

    @Query("SELECT * FROM playlist_entry WHERE id = :entryId")
    suspend fun getEntryById(entryId: Int): PlaylistEntry?

    @Query("UPDATE playlist_entry SET next_entry_id = :nextEntryId WHERE id = :thisEntryId")
    fun setNextEntryId(thisEntryId: Int, nextEntryId: Int)

    @Update
    fun update(entry: PlaylistEntry)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PlaylistEntry): Long

    @Delete
    suspend fun deleteEntry(entry: PlaylistEntry)
}
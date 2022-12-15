package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface PlaylistEntryDao {
    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId")
    fun getLivePlaylistItems(playlistId: Int): LiveData<List<PlaylistEntry>>

    @Query("SELECT playlist_id FROM playlist_entry WHERE tab_id = :tabId")
    suspend fun getPlaylistsForTab(tabId: Int): List<Int>

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId AND next_entry_id IS NULL")
    suspend fun getLastEntryInPlaylist(playlistId: Int): PlaylistEntry?

    @Query("SELECT * FROM playlist_entry WHERE entry_id = :entryId")
    suspend fun getEntryById(entryId: Int): PlaylistEntry?

    @Query("UPDATE playlist_entry SET next_entry_id = :nextEntryId WHERE entry_id = :thisEntryId")
    fun setNextEntryId(thisEntryId: Int?, nextEntryId: Int?)

    @Query("UPDATE playlist_entry SET prev_entry_id = :prevEntryId WHERE entry_id = :thisEntryId")
    fun setPrevEntryId(thisEntryId: Int?, prevEntryId: Int?)

    @Query("""
            UPDATE playlist_entry SET next_entry_id = (CASE entry_id
                    when :srcPrv then :srcNxt
                    when :src then :destNxt
                    when :destPrv then :src
                    else next_entry_id
                    END),
                prev_entry_id = (CASE entry_id
                    when :srcNxt then :srcPrv
                    when :src then :destPrv
                    when :destNxt then :src
                    else prev_entry_id
                    END)
            """)
    fun moveEntry(srcPrv: Int?, srcNxt: Int?, src: Int, destPrv: Int?, destNxt: Int?)

    @Update
    fun update(entry: PlaylistEntry)

    @Query("INSERT INTO playlist_entry (playlist_id, tab_id, next_entry_id, prev_entry_id, date_added, transpose) VALUES (:playlistId, :tabId, :nextEntryId, :prevEntryId, :dateAdded, :transpose)")
    fun insert(playlistId: Int, tabId: Int, nextEntryId: Int?, prevEntryId: Int?, dateAdded: Long, transpose: Int)

    fun insertToFavorites(tabId: Int, transpose: Int)
        = insert(-1, tabId, null, null, System.currentTimeMillis(), transpose)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PlaylistEntry): Long

    @Query("DELETE FROM playlist_entry WHERE entry_id = :entry_id")
    fun deleteEntry(entry_id: Int)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId AND tab_id = :tabId")
    fun deleteTabFromPlaylist(tabId: Int, playlistId: Int)

    fun deleteTabFromFavorites(tabId: Int) = deleteTabFromPlaylist(tabId, -1)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId")
    fun clearPlaylist(playlistId: Int)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = -2")
    fun clearTopTabsPlaylist()

    @Query("SELECT EXISTS(SELECT * FROM playlist_entry WHERE playlist_id = -1 AND tab_id = :tabId)")
    suspend fun tabExistsInFavorites(tabId: Int): Boolean

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = -1 AND tab_id = :tabId")
    suspend fun getFavoritesPlaylistEntry(tabId: Int): PlaylistEntry?

    @Query("UPDATE playlist_entry SET transpose = :transpose WHERE playlist_id = -1 AND tab_id = :tabId")
    fun updateFavoriteTabTransposition(tabId: Int, transpose: Int)
}
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

    @Query("SELECT * FROM playlist_entry WHERE id = :entryId")
    suspend fun getEntryById(entryId: Int): PlaylistEntry?

    @Query("UPDATE playlist_entry SET next_entry_id = :nextEntryId WHERE id = :thisEntryId")
    fun setNextEntryId(thisEntryId: Int?, nextEntryId: Int?)

    @Query("UPDATE playlist_entry SET prev_entry_id = :prevEntryId WHERE id = :thisEntryId")
    fun setPrevEntryId(thisEntryId: Int?, prevEntryId: Int?)

    @Query("""
            UPDATE playlist_entry SET next_entry_id = (CASE id
                    when :srcPrv then :srcNxt
                    when :src then :destNxt
                    when :destPrv then :src
                    else next_entry_id
                    END),
                prev_entry_id = (CASE id
                    when :srcNxt then :srcPrv
                    when :src then :destPrv
                    when :destNxt then :src
                    else prev_entry_id
                    END)
            """)
    fun moveEntry(srcPrv: Int?, srcNxt: Int?, src: Int, destPrv: Int?, destNxt: Int?)

    @Update
    fun update(entry: PlaylistEntry)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PlaylistEntry): Long

    @Query("DELETE FROM playlist_entry WHERE id = :id")
    fun deleteEntry(id: Int)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId")
    fun deletePlaylist(playlistId: Int)
}
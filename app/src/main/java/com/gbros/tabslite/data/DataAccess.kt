package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.playlist.DataPlaylistEntry
import com.gbros.tabslite.data.playlist.IDataPlaylistEntry
import com.gbros.tabslite.data.playlist.IPlaylist
import com.gbros.tabslite.data.playlist.IPlaylistEntry
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.playlist.Playlist.Companion.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.data.playlist.Playlist.Companion.TOP_TABS_PLAYLIST_ID
import com.gbros.tabslite.data.playlist.SelfContainedPlaylist
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabDataType
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry

/**
 * The Data Access Object for the Tab Full class.
 */
@Dao
interface DataAccess {
    //#region tab table

    @Query("SELECT * FROM tabs LEFT JOIN (SELECT IFNULL(transpose, null) as transpose, tab_id FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID) ON tab_id = id WHERE id = :tabId")
    fun getTab(tabId: Int): LiveData<Tab>

    @Query("SELECT * FROM tabs WHERE id = :tabId")
    suspend fun getTabInstance(tabId: Int): TabDataType

    @Query("SELECT * FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id LEFT JOIN (SELECT id AS playlist_id, user_created, title, date_created, date_modified, description FROM playlist ) AS playlist ON playlist_entry.playlist_id = playlist.playlist_id WHERE playlist_entry.entry_id = :playlistEntryId")
    fun getTabFromPlaylistEntryId(playlistEntryId: Int): LiveData<TabWithDataPlaylistEntry?>

    @Query("SELECT DISTINCT tab_id FROM playlist_entry LEFT JOIN tabs ON tabs.id = playlist_entry.tab_id WHERE tabs.content is NULL OR tabs.content is ''")
    suspend fun getEmptyPlaylistTabIds(): List<Int>

    @Query("SELECT DISTINCT tab_id FROM playlist_entry LEFT JOIN tabs ON tabs.id = playlist_entry.tab_id WHERE playlist_entry.playlist_id = :playlistId AND (tabs.content is NULL OR tabs.content is '')")
    suspend fun getEmptyPlaylistTabIds(playlistId: Int): List<Int>
    
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id INNER JOIN playlist ON playlist_entry.playlist_id = playlist.id WHERE playlist_entry.playlist_id = :playlistId")
    fun getPlaylistTabs(playlistId: Int): LiveData<List<TabWithDataPlaylistEntry>>

    @Query("SELECT EXISTS(SELECT 1 FROM tabs WHERE id = :tabId AND content != '' LIMIT 1)")
    suspend fun existsWithContent(tabId: Int): Boolean

    @Query("SELECT *, 0 as transpose FROM tabs WHERE song_id = :songId")
    fun getTabsBySongId(songId: Int): LiveData<List<Tab>>

    @Upsert
    suspend fun upsert(tab: TabDataType)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tab: TabDataType)

    /**
     * Get top 7 downloaded tabs whose id, title, or artist matches the provided query
     */
    @Query("SELECT *, 0 as transpose FROM tabs WHERE content != '' AND (id = :query OR song_name LIKE '%' || :query || '%' OR artist_name LIKE '%' || :query || '%') LIMIT 7")
    fun findMatchingTabs(query: String): LiveData<List<Tab>>

    //#endregion

    //#region playlist table

    @Query("SELECT * FROM playlist WHERE id != $FAVORITES_PLAYLIST_ID AND id != $TOP_TABS_PLAYLIST_ID")
    fun getLivePlaylists(): LiveData<List<Playlist>>

    @Query("SELECT * FROM playlist WHERE id != $FAVORITES_PLAYLIST_ID AND id != $TOP_TABS_PLAYLIST_ID")
    suspend fun getPlaylists(): List<Playlist>

    @Query("UPDATE playlist SET title = :newTitle WHERE id = :playlistId")
    suspend fun updateTitle(playlistId: Int, newTitle: String)

    @Query("UPDATE playlist SET description = :newDescription WHERE id = :playlistId")
    suspend fun updateDescription(playlistId: Int, newDescription: String)

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    fun getLivePlaylist(playlistId: Int): LiveData<Playlist>

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: Int): Playlist

    @Upsert
    suspend fun upsert(playlist: Playlist): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: Playlist): Long

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

    //#endregion

    //#region playlist entry table

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId AND next_entry_id IS NULL")
    suspend fun getLastEntryInPlaylist(playlistId: Int): DataPlaylistEntry?

    @Query("SELECT * FROM playlist_entry WHERE entry_id = :entryId")
    suspend fun getEntryById(entryId: Int): DataPlaylistEntry?

    @Query("UPDATE playlist_entry SET next_entry_id = :nextEntryId WHERE entry_id = :thisEntryId")
    suspend fun setNextEntryId(thisEntryId: Int?, nextEntryId: Int?)

    @Query("UPDATE playlist_entry SET prev_entry_id = :prevEntryId WHERE entry_id = :thisEntryId")
    suspend fun setPrevEntryId(thisEntryId: Int?, prevEntryId: Int?)

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
    suspend fun moveEntry(srcPrv: Int?, srcNxt: Int?, src: Int, destPrv: Int?, destNxt: Int?)

    /**
     * Move an entry to before another entry
     */
    suspend fun moveEntryBefore(entry: IDataPlaylistEntry, beforeEntry: IDataPlaylistEntry) {
        moveEntry(entry.prevEntryId, entry.nextEntryId, entry.entryId, beforeEntry.prevEntryId, beforeEntry.entryId)
    }

    /**
     * Move an entry to after another entry
     */
    suspend fun moveEntryAfter(entry: IDataPlaylistEntry, afterEntry: IDataPlaylistEntry) {
        moveEntry(entry.prevEntryId, entry.nextEntryId, entry.entryId, afterEntry.entryId, afterEntry.nextEntryId)
    }

    @Transaction
    suspend fun removeEntryFromPlaylist(entry: IDataPlaylistEntry) {
        if (entry.prevEntryId != null) {
            // Update the next entry ID of the previous entry to skip the removed entry
            setNextEntryId(entry.prevEntryId, entry.nextEntryId)
        }

        if (entry.nextEntryId != null) {
            // Update the previous entry ID of the next entry to skip the removed entry
            setPrevEntryId(entry.nextEntryId, entry.prevEntryId)
        }

        // Remove the entry itself
        deleteEntry(entry.entryId)
    }

    @Update
    fun update(entry: DataPlaylistEntry)

    @Query("INSERT INTO playlist_entry (playlist_id, tab_id, next_entry_id, prev_entry_id, date_added, transpose) VALUES (:playlistId, :tabId, :nextEntryId, :prevEntryId, :dateAdded, :transpose)")
    suspend fun insert(playlistId: Int, tabId: Int, nextEntryId: Int?, prevEntryId: Int?, dateAdded: Long, transpose: Int)

    suspend fun insertToFavorites(tabId: Int, transpose: Int)
            = insert(FAVORITES_PLAYLIST_ID, tabId, null, null, System.currentTimeMillis(), transpose)

    @Transaction
    suspend fun appendToPlaylist(playlistId: Int, tabId: Int, transpose: Int) {
        val lastEntry = getLastEntryInPlaylist(playlistId = playlistId)
        val newEntry = DataPlaylistEntry(entryId = 0, playlistId = playlistId, tabId = tabId, nextEntryId = null, prevEntryId = lastEntry?.entryId, dateAdded = System.currentTimeMillis(), transpose = transpose )
        val newEntryId = insert(newEntry).toInt()

        if (lastEntry != null) {
            val updatedLastEntry = DataPlaylistEntry(entryId = lastEntry.entryId, playlistId = lastEntry.playlistId, tabId = lastEntry.tabId, nextEntryId = newEntryId, prevEntryId = lastEntry.prevEntryId, dateAdded = lastEntry.dateAdded, transpose = lastEntry.transpose)
            update(updatedLastEntry)
        }
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: DataPlaylistEntry): Long

    @Query("DELETE FROM playlist_entry WHERE entry_id = :entryId")
    suspend fun deleteEntry(entryId: Int)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId AND tab_id = :tabId")
    suspend fun deleteTabFromPlaylist(tabId: Int, playlistId: Int)

    suspend fun deleteTabFromFavorites(tabId: Int) = deleteTabFromPlaylist(tabId, FAVORITES_PLAYLIST_ID)

    @Query("DELETE FROM playlist_entry WHERE playlist_id = :playlistId")
    fun clearPlaylist(playlistId: Int)

    fun clearTopTabsPlaylist() = clearPlaylist(TOP_TABS_PLAYLIST_ID)

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId")
    suspend fun getAllEntriesInPlaylist(playlistId: Int): List<DataPlaylistEntry>

    suspend fun getSortedEntriesInPlaylist(playlistId: Int): List<DataPlaylistEntry> {
        val allEntries = getAllEntriesInPlaylist(playlistId = playlistId)
        return DataPlaylistEntry.sortLinkedList(allEntries)
    }

    suspend fun getSelfContainedPlaylists(playlists: List<IPlaylist>): List<SelfContainedPlaylist> {
        val selfContainedPlaylists: MutableList<SelfContainedPlaylist> = mutableListOf()
        for (playlist in playlists) {
            selfContainedPlaylists.add(SelfContainedPlaylist(playlist, getSortedEntriesInPlaylist(playlist.playlistId) as List<IPlaylistEntry>))
        }

        return selfContainedPlaylists
    }

    @Query("SELECT EXISTS(SELECT * FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId)")
    fun tabExistsInFavoritesLive(tabId: Int): LiveData<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM playlist_entry as favorites INNER JOIN (SELECT * FROM playlist_entry WHERE entry_id = :entryId) AS source ON source.tab_id = favorites.tab_id WHERE favorites.playlist_id = $FAVORITES_PLAYLIST_ID)")
    fun playlistEntryExistsInFavorites(entryId: Int): LiveData<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId)")
    suspend fun tabExistsInFavorites(tabId: Int): Boolean

    @Query("UPDATE playlist_entry SET transpose = :transpose WHERE playlist_id = $FAVORITES_PLAYLIST_ID AND tab_id = :tabId")
    suspend fun updateFavoriteTabTransposition(tabId: Int, transpose: Int)

    @Query("UPDATE playlist_entry SET transpose = :transpose WHERE entry_id = :entryId")
    suspend fun updateEntryTransposition(entryId: Int, transpose: Int)

    //#endregion

    //#region chord variation table

    @Query("SELECT * FROM chord_variation WHERE chord_id = :chordId")
    suspend fun getChordVariations(chordId: String): List<ChordVariation>

    @Query("SELECT * FROM chord_variation WHERE chord_id = :chordId")
    fun chordVariations(chordId: String): LiveData<List<ChordVariation>>

    @Query("SELECT DISTINCT chord_id FROM chord_variation WHERE chord_id IN (:chordIds)")
    suspend fun findAll(chordIds: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chords: List<ChordVariation>)

    //#endregion

    //#region preference table

    @Query("SELECT * FROM preferences WHERE name = :name")
    fun getLivePreference(name: String): LiveData<Preference>

    @Query("SELECT value FROM preferences WHERE name = :name")
    suspend fun getPreferenceValue(name: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pref: Preference)

    @Upsert
    suspend fun upsert(preference: Preference)

    //#endregion

    //#region search suggestions table

    @Upsert
    suspend fun upsert(searchSuggestions: SearchSuggestions)

    /**
     * Gets raw search suggestion data from the database. Note that the query string must be 5
     * characters or fewer - no search suggestions. You should probably use [getSearchSuggestions]
     * unless you specifically need this function
     */
    @Query("SELECT * FROM search_suggestions WHERE `query` = :query")
    fun getRawSearchSuggestions(query: String): LiveData<SearchSuggestions?>

    fun getSearchSuggestions(query: String): LiveData<List<String>> = getRawSearchSuggestions(query.take(5)).map { s ->
        s?.suggestedSearches?.filter { suggestion -> suggestion.contains(other = query, ignoreCase = true) } ?: listOf()
    }

    //#endregion
}
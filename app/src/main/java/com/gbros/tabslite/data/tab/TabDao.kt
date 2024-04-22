package com.gbros.tabslite.data.tab

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import com.gbros.tabslite.data.playlist.Playlist.Companion.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.data.playlist.Playlist.Companion.TOP_TABS_PLAYLIST_ID

/**
 * The Data Access Object for the Tab Full class.
 */
@Dao
interface TabDao {
    @Query("SELECT * FROM tabs LEFT JOIN (SELECT IFNULL(transpose, 0) as transpose, tab_id FROM playlist_entry WHERE playlist_id = $FAVORITES_PLAYLIST_ID) ON tab_id = id WHERE id = :tabId")
    fun getTab(tabId: Int): LiveData<Tab>

    @Query("SELECT * FROM tabs WHERE id = :tabId")
    suspend fun getTabInstance(tabId: Int): TabDataType

    @Query("SELECT * FROM tabs WHERE id IN (:tabIds)")
    fun getTabs(tabIds: List<Int>): LiveData<List<TabDataType>>

    @Query("SELECT * FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id LEFT JOIN (SELECT id AS playlist_id, user_created, title, date_created, date_modified, description FROM playlist ) AS playlist ON playlist_entry.playlist_id = playlist.playlist_id WHERE playlist_entry.entry_id = :playlistEntryId")
    fun getTabFromPlaylistEntryId(playlistEntryId: Int): LiveData<TabWithDataPlaylistEntry?>

    @Query("SELECT * FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id LEFT JOIN (SELECT id AS playlist_id, user_created, title, date_created, date_modified, description FROM playlist ) AS playlist ON playlist_entry.playlist_id = playlist.playlist_id WHERE playlist_entry.playlist_id = :playlistId")
    fun getTabsFromPlaylistEntryId(playlistId: Int): LiveData<List<TabWithDataPlaylistEntry>>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, 1 as user_created, 'Favorites' as title, 0 as date_created, 0 as date_modified, 'Tabs you have favorited in the app' as description FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id WHERE playlist_entry.playlist_id = $FAVORITES_PLAYLIST_ID")
    fun getFavoriteTabs(): LiveData<List<TabWithDataPlaylistEntry>>

    @Query("SELECT DISTINCT tab_id, content FROM playlist_entry LEFT JOIN tabs ON tabs.id = playlist_entry.tab_id WHERE tabs.content is NULL OR tabs.content is ''")
    suspend fun getEmptyPlaylistTabIds(): List<Int>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, 0 as user_created, 'Popular Tabs' as title, 0 as date_created, 0 as date_modified, 'Top tabs of today' as description FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id WHERE playlist_entry.playlist_id = $TOP_TABS_PLAYLIST_ID")
    fun getPopularTabs(): LiveData<List<TabWithDataPlaylistEntry>>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM tabs INNER JOIN playlist_entry ON tabs.id = playlist_entry.tab_id INNER JOIN playlist ON playlist_entry.playlist_id = playlist.id WHERE playlist_entry.playlist_id = :playlistId")
    fun getPlaylistTabs(playlistId: Int): LiveData<List<TabWithDataPlaylistEntry>>

    @Query("SELECT EXISTS(SELECT 1 FROM tabs WHERE id = :tabId AND content != '' LIMIT 1)")
    suspend fun existsWithContent(tabId: Int): Boolean

    @Query("SELECT * FROM tabs WHERE song_name LIKE :songName + '%'")
    fun getTabsByName(songName: String): LiveData<List<TabDataType>>

    @Query("SELECT *, 0 as transpose FROM tabs WHERE song_id = :songId")
    fun getTabsBySongId(songId: Int): LiveData<List<Tab>>

    @Query("SELECT * FROM tabs WHERE artist_name LIKE '%' + :artist + '%'")
    fun getTabsByArtist(artist: String): LiveData<List<TabDataType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun forceInsert(tab: TabDataType)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tab: TabDataType)
}
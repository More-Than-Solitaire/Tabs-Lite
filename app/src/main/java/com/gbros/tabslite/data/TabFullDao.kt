package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Data Access Object for the Tab Full class.
 */
@Dao
interface TabFullDao {
    @Query("SELECT * FROM tabs WHERE id = :tabId")
    suspend fun getTab(tabId: Int): TabFull

    @Query("SELECT * FROM tabs WHERE id IN (:tabIds)")
    fun getTabs(tabIds: List<Int>): LiveData<List<TabFull>>

    @Query("SELECT * FROM tabs WHERE id IN (SELECT tab_id FROM playlist_entry WHERE playlist_id = -1)")
    fun getFavoriteTabs(): LiveData<List<TabFull>>

    @Query("SELECT EXISTS(SELECT 1 FROM tabs WHERE id = :tabId LIMIT 1)")
    suspend fun exists(tabId: Int): Boolean

    @Query("SELECT * FROM tabs WHERE song_name LIKE :songName + '%'")
    fun getTabsByName(songName: String): LiveData<List<TabFull>>

    @Query("SELECT * FROM tabs WHERE song_id = :songId")
    fun getTabsBySongId(songId: Int): LiveData<List<TabFull>>

    @Query("SELECT * FROM tabs WHERE artist_name LIKE '%' + :artist + '%'")
    fun getTabsByArtist(artist: String): LiveData<List<TabFull>>

    @Query("UPDATE tabs SET transposed = :transposed WHERE id = :tabId")
    suspend fun updateTransposed(tabId: Int, transposed: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tab: TabFull)
}
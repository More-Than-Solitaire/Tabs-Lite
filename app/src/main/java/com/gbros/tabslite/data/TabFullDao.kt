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

    @Query("SELECT EXISTS(SELECT 1 FROM tabs WHERE id = :tabId LIMIT 1)")
    suspend fun exists(tabId: Int): Boolean

    @Query("SELECT * FROM tabs WHERE favorite = 1")
    fun getFavoriteTabs(): LiveData<List<TabFull>>

    @Query("UPDATE tabs SET favorite = 1 WHERE id = :tabId")
    suspend fun favoriteTab(tabId: Int)

    @Query("UPDATE tabs SET favorite = 0 WHERE id = :tabId")
    suspend fun unfavoriteTab(tabId: Int)

    @Query("SELECT * FROM tabs WHERE song_name LIKE :songName + '%'")
    fun getTabsByName(songName: String): LiveData<List<TabFull>>

    @Query("SELECT * FROM tabs WHERE song_id = :songId")
    fun getTabsBySongId(songId: Int): LiveData<List<TabFull>>

    @Query("SELECT * FROM tabs WHERE artist_name LIKE '%' + :artist + '%'")
    fun getTabsByArtist(artist: String): LiveData<List<TabFull>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tab: TabFull)

}
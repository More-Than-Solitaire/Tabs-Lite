package com.gbros.tabslite.data

class PlaylistEntryRepository private constructor(private val playlistEntryDao: PlaylistEntryDao) {
    fun getLivePlaylistItems(playlistId: Int) = playlistEntryDao.getLivePlaylistItems(playlistId)
    suspend fun getPlaylistsForTab(tabId: Int) = playlistEntryDao.getPlaylistsForTab(tabId)
    suspend fun getEntryById(entryId: Int) = playlistEntryDao.getEntryById(entryId)
    suspend fun insert(entry: PlaylistEntry) = playlistEntryDao.insert(entry)
    suspend fun deleteEntry(id: Int) = playlistEntryDao.deleteEntry(id)
    suspend fun getLastEntryInPlaylist(playlistId: Int) = playlistEntryDao.getLastEntryInPlaylist(playlistId)
    fun update(entry: PlaylistEntry) = playlistEntryDao.update(entry)
    fun setNextEntryId(nextEntryId: Int?, thisEntryId: Int?) = playlistEntryDao.setNextEntryId(nextEntryId, thisEntryId)
    fun deletePlaylist(playlistId: Int) = playlistEntryDao.clearPlaylist(playlistId)
    fun getFavoriteTabEntries() = playlistEntryDao.getLivePlaylistItems(-1)
    fun addToFavorites(tabId: Int, transpose: Int = 0) = playlistEntryDao.insert(-1, tabId, null, null, System.currentTimeMillis(), transpose)
    fun removeFromFavorites(tabId: Int) = playlistEntryDao.deleteTabFromPlaylist(tabId, -1)

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: PlaylistEntryRepository? = null

        fun getInstance(playlistEntryDao: PlaylistEntryDao) =
                instance ?: synchronized(this) {
                    instance ?: PlaylistEntryRepository(playlistEntryDao).also { instance = it }
                }
    }
}
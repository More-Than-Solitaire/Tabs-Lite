package com.gbros.tabslite.data

class PlaylistEntryRepository private constructor(private val playlistEntryDao: PlaylistEntryDao) {
    suspend fun getPlaylistItems(playlistId: Int) = playlistEntryDao.getPlaylistItems(playlistId)
    suspend fun getPlaylistsForTab(tabId: Int) = playlistEntryDao.getPlaylistsForTab(tabId)
    suspend fun getEntryById(entryId: Int) = playlistEntryDao.getEntryById(entryId)
    suspend fun insert(entry: PlaylistEntry) = playlistEntryDao.insert(entry)
    suspend fun deleteEntry(entry: PlaylistEntry) = playlistEntryDao.deleteEntry(entry)
    suspend fun getLastEntryInPlaylist(playlistId: Int) = playlistEntryDao.getLastEntryInPlaylist(playlistId)
    fun update(entry: PlaylistEntry) = playlistEntryDao.update(entry)
    fun setNextEntryId(nextEntryId: Int, thisEntryId: Int) = playlistEntryDao.setNextEntryId(nextEntryId, thisEntryId)


    companion object {
        // For Singleton instantiation
        @Volatile private var instance: PlaylistEntryRepository? = null

        fun getInstance(playlistEntryDao: PlaylistEntryDao) =
                instance ?: synchronized(this) {
                    instance ?: PlaylistEntryRepository(playlistEntryDao).also { instance = it }
                }
    }
}
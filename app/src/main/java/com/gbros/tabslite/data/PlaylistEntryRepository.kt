package com.gbros.tabslite.data

class PlaylistEntryRepository private constructor(private val playlistEntryDao: PlaylistEntryDao) {
    suspend fun getPlaylistItems(playlistId: Int) = playlistEntryDao.getPlaylistItems(playlistId)
    suspend fun getChordVariation(tabId: Int) = playlistEntryDao.getPlaylistsForTab(tabId)
    suspend fun insert(entry: PlaylistEntry) = playlistEntryDao.insert(entry)
    suspend fun deleteEntry(entry: PlaylistEntry) = playlistEntryDao.deleteEntry(entry)


    companion object {
        // For Singleton instantiation
        @Volatile private var instance: PlaylistEntryRepository? = null

        fun getInstance(playlistEntryDao: PlaylistEntryDao) =
                instance ?: synchronized(this) {
                    instance ?: PlaylistEntryRepository(playlistEntryDao).also { instance = it }
                }
    }
}
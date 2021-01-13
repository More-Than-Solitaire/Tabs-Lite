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
    fun deletePlaylist(playlistId: Int) = playlistEntryDao.deletePlaylist(playlistId)


    companion object {
        // For Singleton instantiation
        @Volatile private var instance: PlaylistEntryRepository? = null

        fun getInstance(playlistEntryDao: PlaylistEntryDao) =
                instance ?: synchronized(this) {
                    instance ?: PlaylistEntryRepository(playlistEntryDao).also { instance = it }
                }
    }
}
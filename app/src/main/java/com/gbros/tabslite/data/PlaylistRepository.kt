package com.gbros.tabslite.data

class PlaylistRepository private constructor(private val playlistDao: PlaylistDao) {
    fun getPlaylists() = playlistDao.getPlaylists()
    suspend fun getPlaylist(playlistId: Int) = playlistDao.getPlaylist(playlistId)
    suspend fun savePlaylist(playlist: Playlist) = playlistDao.savePlaylist(playlist)
    suspend fun deletePlaylist(playlistId: Int) = playlistDao.deletePlaylist(playlistId)
    fun updateTimestamp(playlistId: Int, dateModified: Long) = playlistDao.updateTimestamp(playlistId, dateModified)

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: PlaylistRepository? = null

        fun getInstance(playlistDao: PlaylistDao) =
                instance ?: synchronized(this) {
                    instance ?: PlaylistRepository(playlistDao).also { instance = it }
                }
    }
}
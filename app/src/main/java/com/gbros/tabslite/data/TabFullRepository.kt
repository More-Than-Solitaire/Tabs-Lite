package com.gbros.tabslite.data

class TabFullRepository private constructor(private val tabDao: TabFullDao) {
    suspend fun getTab(tabId: Int) = tabDao.getTab(tabId)
    fun getFavoriteTabs() = tabDao.getFavoriteTabs()
    suspend fun favoriteTab(tabId: Int) = tabDao.favoriteTab(tabId)
    suspend fun unfavoriteTab(tabId: Int) = tabDao.unfavoriteTab(tabId)
    fun getTabsByName(songName: String) = tabDao.getTabsByName(songName)
    fun getTabsBySongId(songId: Int) = tabDao.getTabsBySongId(songId)
    fun getTabsByArtist(artist: String) = tabDao.getTabsByArtist(artist)
    suspend fun insert(tab: TabFull) = tabDao.insert(tab)

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: TabFullRepository? = null

        fun getInstance(tabDao: TabFullDao) =
                instance ?: synchronized(this) {
                    instance ?: TabFullRepository(tabDao).also { instance = it }
                }
    }
}
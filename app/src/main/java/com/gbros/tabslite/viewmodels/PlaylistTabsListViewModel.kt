package com.gbros.tabslite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.data.TabFullDao
import com.gbros.tabslite.data.TabFullWithPlaylistEntry

class PlaylistTabsListViewModel internal constructor(
    tabFullDao: TabFullDao, playlistId: Int
) : ViewModel() {
    val tabList: LiveData<List<TabFullWithPlaylistEntry>> =
            tabFullDao.getPlaylistTabs(playlistId)
}
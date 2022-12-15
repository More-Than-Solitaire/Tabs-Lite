package com.gbros.tabslite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.data.TabDao
import com.gbros.tabslite.data.TabFullWithPlaylistEntry

class PlaylistTabsListViewModel internal constructor(
    tabDao: TabDao, playlistId: Int
) : ViewModel() {
    val tabList: LiveData<List<TabFullWithPlaylistEntry>> =
            tabDao.getPlaylistTabs(playlistId)
}
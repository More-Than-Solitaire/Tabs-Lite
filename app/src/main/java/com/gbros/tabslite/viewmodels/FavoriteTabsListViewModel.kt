package com.gbros.tabslite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.data.TabFull
import com.gbros.tabslite.data.TabFullRepository

class FavoriteTabsListViewModel internal constructor(
        tabsRepository: TabFullRepository
) : ViewModel() {
    val favoriteTabs: LiveData<List<TabFull>> =
            tabsRepository.getFavoriteTabs()
}
package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gbros.tabslite.data.TabFullDao
import com.gbros.tabslite.data.TabFullRepository

/**
 * Factory for creating a [PlaylistTabsListViewModel] with a constructor that takes a
 * [TabFullRepository].
 */
class PlaylistTabsViewModelFactory(
        private val tabFullDao: TabFullDao, private val playlistId: Int
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaylistTabsListViewModel(tabFullDao, playlistId) as T
    }
}
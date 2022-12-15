package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gbros.tabslite.data.TabDao

/**
 * Factory for creating a [PlaylistTabsListViewModel] with a constructor that takes a
 * [TabFullRepository].
 */
class PlaylistTabsViewModelFactory(
    private val tabDao: TabDao, private val playlistId: Int
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaylistTabsListViewModel(tabDao, playlistId) as T
    }
}
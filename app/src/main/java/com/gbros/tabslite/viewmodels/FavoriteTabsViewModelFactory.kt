package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gbros.tabslite.data.TabFullRepository

/**
 * Factory for creating a [FavoriteTabsListViewModel] with a constructor that takes a
 * [TabFullRepository].
 */
class FavoriteTabsViewModelFactory(
        private val repository: TabFullRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoriteTabsListViewModel(repository) as T
    }
}
package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gbros.tabslite.data.PlaylistDao

/**
 * Factory for creating a [PlaylistsViewModelFactory] with a constructor that takes a
 * [PlaylistRepository] and a [PlaylistEntryRepository]
 */
class PlaylistsViewModelFactory(
        private val playlistDao: PlaylistDao
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaylistsViewModel(playlistDao) as T
    }
}
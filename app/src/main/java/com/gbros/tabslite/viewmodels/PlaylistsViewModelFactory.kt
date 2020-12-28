package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gbros.tabslite.data.PlaylistEntryRepository
import com.gbros.tabslite.data.PlaylistRepository
import com.gbros.tabslite.data.TabFullRepository

/**
 * Factory for creating a [PlaylistsViewModelFactory] with a constructor that takes a
 * [PlaylistRepository] and a [PlaylistEntryRepository]
 */
class PlaylistsViewModelFactory(
        private val playlistRepository: PlaylistRepository,
        private val playlistEntryRepository: PlaylistEntryRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaylistsViewModel(playlistRepository, playlistEntryRepository) as T
    }
}
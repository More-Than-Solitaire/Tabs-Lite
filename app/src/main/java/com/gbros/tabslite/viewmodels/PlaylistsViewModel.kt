package com.gbros.tabslite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.data.*

class PlaylistsViewModel constructor(
        playlistRepository: PlaylistRepository
) : ViewModel() {
    val playlists: LiveData<List<Playlist>> =
            playlistRepository.getPlaylists()
}
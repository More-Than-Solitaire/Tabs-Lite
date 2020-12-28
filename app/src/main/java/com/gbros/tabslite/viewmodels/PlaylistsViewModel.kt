package com.gbros.tabslite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.data.*

class PlaylistsViewModel constructor(
        playlistRepository: PlaylistRepository,
        playlistEntryRepository: PlaylistEntryRepository
) : ViewModel() {
    val playlists: LiveData<List<Playlist>> =
            playlistRepository.getPlaylists()

    private val per = playlistEntryRepository
    suspend fun getPlaylistEntries(pid: Int) = per.getPlaylistItems(pid)
}
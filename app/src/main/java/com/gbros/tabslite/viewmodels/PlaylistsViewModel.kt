package com.gbros.tabslite.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistDao

class PlaylistsViewModel constructor(
    playlistDao: PlaylistDao
) : ViewModel() {
    val playlists: LiveData<List<Playlist>> =
            playlistDao.getPlaylists()
}
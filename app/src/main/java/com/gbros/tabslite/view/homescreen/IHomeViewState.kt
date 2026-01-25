package com.gbros.tabslite.view.homescreen

import androidx.lifecycle.LiveData
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.FontStyle
import com.gbros.tabslite.data.ThemeSelection
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.view.playlists.PlaylistsSortBy

interface IHomeViewState {
    /**
     * The percent value (0 to 100) for any ongoing import/export operation
     */
    val playlistImportProgress: LiveData<Float>

    /**
     * The current state of any import/export operations
     */
    val playlistImportState: LiveData<LoadingState>

    /**
     * The user's saved playlists
     */
    val playlists: LiveData<List<Playlist>>

    /**
     * The selected sort option for playlists
     */
    val playlistsSortBy: LiveData<PlaylistsSortBy>

    /**
     * The selected theme (system, dark, or light)
     */
    val selectedAppTheme: LiveData<ThemeSelection>

    /**
     * The selected font style
     */
    val selectedFontStyle: LiveData<FontStyle>
}
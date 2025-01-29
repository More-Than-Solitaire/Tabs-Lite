package com.gbros.tabslite.view.playlists

import androidx.lifecycle.LiveData
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry

interface IPlaylistViewState {
    /**
     * The title of the playlist to display
     */
    val title: LiveData<String>

    /**
     * The description of the playlist to display
     */
    val description: LiveData<String>

    /**
     * The ordered list of songs in the playlist
     */
    val songs: LiveData<List<TabWithDataPlaylistEntry>>
}
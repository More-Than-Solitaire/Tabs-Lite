package com.gbros.tabslite.view.songlist

import androidx.lifecycle.LiveData
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry

interface ISongListViewState {
    /**
     * The tabs to display in this song list
     */
    val songs: LiveData<List<TabWithDataPlaylistEntry>>

    /**
     * How these tabs are currently sorted
     */
    val sortBy: LiveData<SortBy>
}
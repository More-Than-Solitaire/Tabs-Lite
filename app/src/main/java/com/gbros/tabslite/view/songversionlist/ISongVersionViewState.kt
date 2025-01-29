package com.gbros.tabslite.view.songversionlist

import androidx.lifecycle.LiveData
import com.gbros.tabslite.data.tab.ITab

interface ISongVersionViewState {
    /**
     * The search query to be displayed in the search bar
     */
    val songName: LiveData<String>

    /**
     * The versions of the selected song to be displayed
     */
    val songVersions: LiveData<List<ITab>>
}
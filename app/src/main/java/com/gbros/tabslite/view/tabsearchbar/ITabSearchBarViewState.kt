package com.gbros.tabslite.view.tabsearchbar

import androidx.lifecycle.LiveData
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry

interface ITabSearchBarViewState {
    /**
     * The current query to be displayed in the search bar
     */
    val query: LiveData<String>

    /**
     * A couple suggested tabs already loaded in the database
     */
    val tabSuggestions: LiveData<List<TabWithDataPlaylistEntry>>

    /**
     * The current search suggestions to be displayed
     */
    val searchSuggestions: LiveData<List<String>>
    val loadingState: LiveData<LoadingState>
}
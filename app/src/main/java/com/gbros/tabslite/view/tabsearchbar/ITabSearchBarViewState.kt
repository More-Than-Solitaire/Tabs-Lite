package com.gbros.tabslite.view.tabsearchbar

import androidx.lifecycle.LiveData

interface ITabSearchBarViewState {
    /**
     * The current query to be displayed in the search bar
     */
    val query: LiveData<String>

    /**
     * The current search suggestions to be displayed
     */
    val searchSuggestions: LiveData<List<String>>
}
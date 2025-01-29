package com.gbros.tabslite.view.tabsearchbar

import androidx.compose.runtime.Composable
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

    /**
     * The composable element to display at the beginning of the search bar
     */
    val leadingIcon: @Composable () -> Unit
}
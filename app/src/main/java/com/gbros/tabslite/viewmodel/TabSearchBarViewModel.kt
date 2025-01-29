package com.gbros.tabslite.viewmodel

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.gbros.tabslite.R
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.utilities.UgApi
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The view model to handle business logic for the [com.gbros.tabslite.view.tabsearchbar.TabsSearchBar]
 * view. Meant to be used as a sub-view underneath another view
 */
class TabSearchBarViewModel(
    /**
     * The initial query value to use
     */
    initialQuery: String,

    /**
     * The composable element to display at the beginning of the search bar. Defaults to the TabsLite
     * icon.
     */
    override val leadingIcon: @Composable () -> Unit = { Icon(
        imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )},

    /**
     * The data access element, for fetching and putting search suggestions
     */
    private val dataAccess: DataAccess
) : ITabSearchBarViewState {

    //#region view state

    /**
     * The current query to be displayed in the search bar
     */
    override val query: MutableLiveData<String> = MutableLiveData(initialQuery)

    override val searchSuggestions: LiveData<List<String>> = query.switchMap { currentQuery ->
        dataAccess.getSearchSuggestions(currentQuery)
    }

    //#endregion

    //#region public methods

    /**
     * To be called when the query changes. Updates the query display, and launches a fetch for the
     * most recent search suggestions for that query
     */
    fun onQueryChange(newQuery: String) {
        query.postValue(newQuery)

        CoroutineScope(Dispatchers.IO).launch {
            UgApi.searchSuggest(newQuery, dataAccess = dataAccess)
        }
    }

    //#endregion

    //#region init

    init {
        onQueryChange(initialQuery)  // preload search suggestions for initial query
    }

    //#endregion
}
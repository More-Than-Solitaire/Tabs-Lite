package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.utilities.TAG
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
    initialQuery: String = "",

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

    /**
     * A couple suggested tabs already loaded in the database
     */
    override val tabSuggestions: LiveData<List<TabWithDataPlaylistEntry>> = query.switchMap { currentQuery ->
        dataAccess.findMatchingTabs(currentQuery).map { a -> a }
    }

    /**
     * The current search suggestions to be displayed
     */
    override val searchSuggestions: LiveData<List<String>> = query.switchMap { currentQuery ->
        dataAccess.getSearchSuggestions(currentQuery)
    }

    override val loadingState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.Success)

    //#endregion

    //#region public methods

    /**
     * To be called when the query changes. Updates the query display, and launches a fetch for the
     * most recent search suggestions for that query
     */
    fun onQueryChange(newQuery: String) {
        query.value = newQuery

        CoroutineScope(Dispatchers.IO).launch {
            try {
                UgApi.searchSuggest(newQuery, dataAccess = dataAccess)
            } catch (ex: UgApi.NoInternetException) {
                // no internet access to fetch search results.
                loadingState.postValue(LoadingState.Error(R.string.message_search_suggestion_no_internet))
                Log.i(TAG, "No internet connection: ${ex.message}", ex)
            } catch (ex: Exception) {
                loadingState.postValue(LoadingState.Error(R.string.message_search_suggestion_unexpected_error))
            }
        }
    }

    //#endregion

    //#region init

    init {
        onQueryChange(initialQuery)  // preload search suggestions for initial query
    }

    //#endregion
}
package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.Search
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.view.searchresultsonglist.ISearchViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

private const val LOG_NAME = "tabslite.UgApi         "

@HiltViewModel(assistedFactory = SearchViewModel.SearchViewModelFactory::class)
class SearchViewModel
@AssistedInject constructor(
    @Assisted override val query: String,
    @Assisted dataAccess: DataAccess
) : ViewModel(), ISearchViewState {

    //#region dependency injection factory

    @AssistedFactory
    interface SearchViewModelFactory {
        fun create(query: String, dataAccess: DataAccess): SearchViewModel
    }

    //#endregion

    //#region view state

    /**
     * The search results returned by this query
     */
    override val results: MutableLiveData<List<ITab>> = MutableLiveData(listOf())

    /**
     * The current state of this search. Will be [LoadingState.Loading] if more search results are
     * being fetched, [LoadingState.Success] if the load process is complete
     */
    override val searchState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.Loading)

    //#endregion

    //#region private data

    /**
     * The last page of search results that's been fetched from the server
     */
    private var searchSession = Search(query, dataAccess)

    //#endregion

    //#region public methods

    fun onMoreSearchResultsNeeded() {
        searchState.postValue(LoadingState.Loading)
        val fetchSearchResultsJob = CoroutineScope(Dispatchers.IO).async {
            val newSearchResults = searchSession.fetchNextSearchResults()
            if (newSearchResults.isNotEmpty()) {
                val updatedResults = results.value?.toMutableList()
                updatedResults?.addAll(newSearchResults)
                results.postValue(updatedResults ?: newSearchResults)
            }
        }

        fetchSearchResultsJob.invokeOnCompletion { ex ->
            if (ex != null) {
                searchState.postValue(LoadingState.Error("Unexpected error loading search results: ${ex.message}"))
                Log.e(LOG_NAME, "Unexpected error loading search results: ${ex.message}", ex)
            }

            searchState.postValue(LoadingState.Success)
        }
    }

    //#endregion
}
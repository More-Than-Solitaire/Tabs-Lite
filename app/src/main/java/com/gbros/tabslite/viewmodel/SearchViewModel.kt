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
import kotlinx.coroutines.sync.Mutex

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

    /**
     * Whether the complete set of search results has already been loaded. Used to disable trying to
     * load more search results
     */
    override val allResultsLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    //#endregion

    //#region private data

    /**
     * The last page of search results that's been fetched from the server
     */
    private var searchSession = Search(query, dataAccess)

    private var searchMutex = Mutex(locked = false)

    //#endregion

    //#region public methods

    fun onMoreSearchResultsNeeded() {
        if (searchMutex.tryLock()) {  // only fetch one page of search results at a time
            searchState.postValue(LoadingState.Loading)
            val fetchSearchResultsJob = CoroutineScope(Dispatchers.IO).async {
                val newSearchResults = searchSession.fetchNextSearchResults()
                if (newSearchResults.isNotEmpty()) {
                    val updatedResults = results.value?.toMutableList()
                    updatedResults?.addAll(newSearchResults)
                    results.postValue(updatedResults ?: newSearchResults)
                } else {
                    allResultsLoaded.postValue(true)
                }
            }

            fetchSearchResultsJob.invokeOnCompletion { ex ->
                if (ex != null) {
                    searchState.postValue(LoadingState.Error("Unexpected error loading search results: ${ex.message}"))
                    Log.e(LOG_NAME, "Unexpected error loading search results: ${ex.message}", ex)
                } else {
                    searchState.postValue(LoadingState.Success)
                }

                searchMutex.unlock()
            }
        }
    }

    //#endregion

    init {
        onMoreSearchResultsNeeded()  // preload the first page of search results
    }
}
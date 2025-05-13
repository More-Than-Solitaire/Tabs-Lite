package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.Search
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.UgApi
import com.gbros.tabslite.view.searchresultsonglist.ISearchViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = SearchViewModel.SearchViewModelFactory::class)
class SearchViewModel
@AssistedInject constructor(
    @Assisted override val query: String,
    @Assisted val artistId: Int?,
    @Assisted dataAccess: DataAccess
) : ViewModel(), ISearchViewState {

    //#region dependency injection factory

    @AssistedFactory
    interface SearchViewModelFactory {
        fun create(query: String, artistId: Int?, dataAccess: DataAccess): SearchViewModel
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
    private var searchSession = Search(query, artistId, dataAccess)

    private var searchMutex = Mutex(locked = false)

    //#endregion

    //#region public data

    val tabSearchBarViewModel = TabSearchBarViewModel(
        initialQuery = query,
        dataAccess = dataAccess
    )

    //#endregion

    //#region public methods

    /**
     * Load another page of search results. Uses a mutex lock to only fetch a single page of results
     * at a time. On completion, sets [searchState] to [LoadingState.Success] (or [LoadingState.Error]
     * on error)
     */
    fun onMoreSearchResultsNeeded(retryOnTimeout: Boolean = true) {
        if (searchMutex.tryLock()) {  // only fetch one page of search results at a time
            val fetchSearchResultsJob = CoroutineScope(Dispatchers.IO).async {
                searchState.postValue(LoadingState.Loading)
                val newSearchResults = searchSession.fetchNextSearchResults()
                if (newSearchResults.isNotEmpty()) {
                    val updatedResults = results.value?.toMutableList()
                    updatedResults?.addAll(newSearchResults)
                    results.postValue(updatedResults?.distinct() ?: newSearchResults)
                } else {
                    allResultsLoaded.postValue(true)
                }
            }
            fetchSearchResultsJob.invokeOnCompletion { ex ->
                when (ex) {
                    null -> {
                        // success
                        searchState.postValue(LoadingState.Success)
                    }
                    is UgApi.NoInternetException -> {
                        searchState.postValue(LoadingState.Error(R.string.message_search_no_internet))
                    }
                    is CancellationException -> {
                        // probably job was cancelled due to timeout (see below)
                        searchState.postValue(LoadingState.Error(R.string.message_search_timeout))
                    }
                    else -> {
                        searchState.postValue(LoadingState.Error(R.string.message_search_unexpected_error))
                        Log.e(TAG, "Unexpected error loading search results: ${ex.message}", ex)
                    }
                }

                searchMutex.unlock()
            }

            // as a backup, if search takes more than 15 seconds to load, cancel and retry
            val searchTimeoutJob = CoroutineScope(Dispatchers.Default).async {
                // wait 15 seconds before cancelling the search job
                delay(15.seconds)
            }
            searchTimeoutJob.invokeOnCompletion {
                // the search job has been given 15 seconds. If it's still running, cancel.
                if (!fetchSearchResultsJob.isCompleted) {
                    fetchSearchResultsJob.cancel("Timeout while waiting for search results.")

                    if (retryOnTimeout) {
                        // retry once more
                        onMoreSearchResultsNeeded(retryOnTimeout = false)
                    }
                }
            }
        }
    }

    //#endregion

    //#region init

    init {
        onMoreSearchResultsNeeded()  // preload the first page of search results
    }

    //#endregion
}
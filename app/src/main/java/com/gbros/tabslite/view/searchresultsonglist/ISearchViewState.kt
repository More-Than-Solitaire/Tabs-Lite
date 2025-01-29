package com.gbros.tabslite.view.searchresultsonglist

import androidx.lifecycle.LiveData
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.tab.ITab

interface ISearchViewState {
    /**
     * The search query being searched
     */
    val query: String

    /**
     * The search results returned by this query
     */
    val results: LiveData<List<ITab>>

    /**
     * The current state of this search. Will be [LoadingState.Loading] if more search results are
     * being fetched, [LoadingState.Success] if the load process is complete
     */
    val searchState: LiveData<LoadingState>
}
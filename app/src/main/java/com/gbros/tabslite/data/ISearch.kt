package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import com.gbros.tabslite.data.tab.ITab

interface ISearch {
    /**
     * The search query being searched.  Normally the same as called in the constructor, but can be
     * changed if we had a Did You Mean (where we ran out of results in the initial search but there
     * was a related search that was suggested).  In that case, this will reflect the most current
     * search query being completed.
     */
    val query: String

    /**
     * The search results from this query.  This is updated as results come in.  You can check [searchInProgress]
     * to see if a search query is currently in progress.
     */
    val result: LiveData<List<ITab>>

    /**
     * True if all search results have been fetched.  This can be used to determine whether to load
     * additional pages or show a message that that's the end of the search results.
     */
    val allPagesLoaded: LiveData<Boolean>

    /**
     * Whether a search is currently in progress
     */
    val searchInProgress: Boolean

    /**
     * Fetch the next page of search results.  If this function is called multiple times before results
     * are in, multiple pages of search results will be loaded.
     */
    fun searchNextPage()
}
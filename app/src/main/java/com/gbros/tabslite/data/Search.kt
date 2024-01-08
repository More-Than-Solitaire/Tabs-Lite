package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.utilities.UgApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val LOG_NAME = "tabslite.Search        "

/**
 * Represents a search session with one search query.  Gets search results and provides a method to
 * retrieve more search results if the first page isn't enough.
 */
@OptIn(DelicateCoroutinesApi::class)
class Search(query: String, private val db: AppDatabase): ISearch {
    //region public data

    /**
     * The search query being searched.  Normally the same as called in the constructor, but can be
     * changed if we had a Did You Mean (where we ran out of results in the initial search but there
     * was a related search that was suggested).  In that case, this will reflect the most current
     * search query being completed.
     */
    override var query: String = query
        private set

    /**
     * The search results from this query.  This is updated as results come in.  You can check [searchInProgress]
     * to see if a search query is currently in progress.
     */
    override val result: LiveData<List<ITab>>
        get() = internalResult

    /**
     * True if all search results have been fetched.  This can be used to determine whether to load
     * additional pages or show a message that that's the end of the search results.
     */
    override val allPagesLoaded: LiveData<Boolean>
        get() = internalAllPagesLoaded

    /**
     * Whether a search is currently in progress
     */
    override val searchInProgress: Boolean
        get() = searchLock.isLocked

    //endregion

    //region private data

    /**
     * These are value-holders for the public members so that we can have *mutable* live data internally
     * and update the values as needed.
     */
    private val internalResult: MutableLiveData<List<ITab>> = MutableLiveData(listOf())
    private val internalAllPagesLoaded: MutableLiveData<Boolean> = MutableLiveData(false)
    private var tempAllPagesLoaded: Boolean = false

    /**
     * Tracks how many pages of search results we've successfully loaded.  Used to ensure we always
     * load the next page (and never re-load the same page twice)
     */
    private  var pagesLoaded = 0

    /**
     * Used to ensure that only one search is performed at a time.  This ensures that we never load
     * pages out of order.
     */
    private var searchLock = Mutex()

    //endregion

    //region init

    init {
        // search the first page by default
        searchNextPage()
    }

    //endregion

    //region public methods

    /**
     * Fetch the next page of search results.  If this function is called multiple times before results
     * are in, multiple pages of search results will be loaded.
     */
    override fun searchNextPage() {
        if (tempAllPagesLoaded) {
            internalAllPagesLoaded.value = true
            return
        }

        if (searchLock.isLocked) {
            return
        }

        GlobalScope.launch {
            searchLock.withLock {  // only run one search at a time
                search()
            }
        }
    }

    //endregion

    // region private methods

    /**
     * Perform a search using UgApi, and update the class variables with the results.  Always searches
     * for the query set in the class (but updates that query if we run out of results and have a Did
     * You Mean option).  Always searches for the next page of values (multiple calls does not mess
     * this up).  Only performs one search at a time; multiple calls to this function will load multiple
     * pages of search results.
     *
     * @param [maxRetryCount] The maximum number of times to try a new Did You Mean search query in an
     * attempt to get more search results.
     */
    private suspend fun search(maxRetryCount: Int = 5) {
        if (!tempAllPagesLoaded) {  // if all pages have already been loaded, don't make another internet fetch
            val searchResult = UgApi.search(query, (pagesLoaded + 1))  // always search the next page that hasn't been loaded yet

            if (!searchResult.didYouMean.isNullOrBlank() && maxRetryCount > 0) {
                // retry with the Did You Mean result
                query = searchResult.didYouMean!!
                pagesLoaded = 0
                search(maxRetryCount - 1)
            } else if (searchResult.getSongs().isEmpty()) {
                tempAllPagesLoaded = true
                internalAllPagesLoaded.postValue(true)  // all results have been collected, don't continue searching
            } else {
                // yay, we got mail!  Add these results to our search results.
                internalResult.postValue(internalResult.value!! + Tab.fromTabDataType(searchResult.getSongs()))

                // add this data to the database so we can display the individual song versions without fully loading all of them
                for (tab in searchResult.getAllTabs()) {
                    db.tabFullDao().insert(tab)
                }

                pagesLoaded++
            }
        }
    }

    // endregion
}
package com.gbros.tabslite.data

import android.util.Log
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.utilities.UgApi

private const val LOG_NAME = "tabslite.Search        "

/**
 * Represents a search session with one search query.  Gets search results and provides a method to
 * retrieve more search results if the first page isn't enough.
 */
class Search(private var query: String, private val dataAccess: DataAccess) {
    //#region private data

    /**
     * The most recently fetched search page
     */
    private var currentSearchPage = 0

    //#endregion

    //#region private methods

    /**
     * Perform a search using UgApi, and update the class variables with the results.  Always searches
     * for the query set in the class (but updates that query if we run out of results and have a Did
     * You Mean option).  Always searches for the next page of values (multiple calls does not mess
     * this up).  Only performs one search at a time; multiple calls to this function will load multiple
     * pages of search results.
     *
     * @param [page] The page of results to fetch
     * @param [query] The query to search for
     *
     * @return A list of search results, or an empty list if there are no search results
     *
     * @throws [SearchDidYouMeanException] if no results, but there's a suggested query
     */
    private suspend fun getSearchResults(page: Int, query: String): List<ITab> {
        Log.d(LOG_NAME, "starting search '$query' page $page")
        val searchResult = UgApi.search(query, (page))  // always search the next page that hasn't been loaded yet

        return if (!searchResult.didYouMean.isNullOrBlank()) {
            throw SearchDidYouMeanException(searchResult.didYouMean!!)
        } else if (searchResult.getSongs().isEmpty()) {
            listOf()  // all search results have been fetched
        } else {
            // add this data to the database so we can display the individual song versions without fully loading all of them
            for (tab in searchResult.getAllTabs()) {
                dataAccess.insert(tab)
            }

            Log.d(LOG_NAME, "Successful search for $query page $page.  Results: ${searchResult.getSongs().size}")
            Tab.fromTabDataType(searchResult.getSongs())
        }
    }

    //#endregion

    //#region public methods

    /**
     * Get the next page of search results for this query. Automatically follows through to "Did You
     * Mean" suggested search queries for misspelled, etc. queries.
     *
     * @return The next page of results, or an empty list if no further results exist, even in suggested Did You Mean queries.
     */
    suspend fun fetchNextSearchResults(): List<ITab> {

        var retriesLeft = 3
        while (retriesLeft-- > 0) {
            try {
                val results = getSearchResults(page = ++currentSearchPage, query = query)
                if (results.isEmpty()) {
                    currentSearchPage--
                }
                return results
            } catch (ex: SearchDidYouMeanException) {
                // no results, but a suggested alternate query available; automatically try that
                currentSearchPage = 0
                query = ex.didYouMean
            }
        }

        // fallback to empty result list. Normally we shouldn't get here
        Log.e(LOG_NAME, "Empty search result fallback after 3 Did You Mean tries. Shouldn't happen normally.")
        return listOf()
    }
}

class SearchDidYouMeanException(val didYouMean: String): Exception()

package com.gbros.tabslite.data

import android.util.Log
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.BackendConnection

/**
 * Represents a search session with one search query.  Gets search results and provides a method to
 * retrieve more search results if the first page isn't enough.
 */
class Search(
    /**
     * The query currently being searched for (in the title field)
     */
    private var query: String,

    /**
     * (Optional) the ID of the artist to filter by. Can be paired with an empty [query] to do an artist song list. Ignored if null or 0.
     */
    private var artistId: Int?,

    /**
     * The data access object interface into the data layer, for caching results and returning cached results
     */
    private val dataAccess: DataAccess
) {

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
     * @param [artistId] (Optional) Filter results by artist ID
     *
     * @return A list of search results, or an empty list if there are no search results
     *
     * @throws [SearchDidYouMeanException] if no results, but there's a suggested query
     */
    private suspend fun getSearchResults(page: Int, query: String, artistId: Int?): List<ITab> {
        Log.d(TAG, "starting search '$query' page $page artist $artistId")
        val searchResult = BackendConnection.search(query, artistId, page)  // always search the next page that hasn't been loaded yet

        return if (!searchResult.didYouMean.isNullOrBlank()) {
            throw SearchDidYouMeanException(searchResult.didYouMean!!)
        } else if (searchResult.getSongs().isEmpty()) {
            listOf()  // all search results have been fetched
        } else {
            // add this data to the database so we can display the individual song versions without fully loading all of them
            for (tab in searchResult.getAllTabs()) {
                dataAccess.insert(tab)
            }

            Log.d(TAG, "Successful search for $query page $page.  Results: ${searchResult.getSongs().size}")
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
                val results = getSearchResults(page = ++currentSearchPage, artistId = artistId, query = query)
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
        Log.e(TAG, "Empty search result fallback after 3 Did You Mean tries. Shouldn't happen normally.")
        return listOf()
    }
}

class SearchDidYouMeanException(val didYouMean: String): Exception()

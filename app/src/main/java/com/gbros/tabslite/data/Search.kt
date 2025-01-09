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
class Search(private val db: AppDatabase): ISearch {

    // region private methods

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
    override suspend fun getSearchResults(page: Int, query: String): List<ITab> {
        Log.d(LOG_NAME, "starting search page $page")
        val searchResult = UgApi.search(query, (page))  // always search the next page that hasn't been loaded yet

        return if (!searchResult.didYouMean.isNullOrBlank()) {
            throw SearchDidYouMeanException(searchResult.didYouMean!!)
        } else if (searchResult.getSongs().isEmpty()) {
            listOf()  // all search results have been fetched
        } else {
            // add this data to the database so we can display the individual song versions without fully loading all of them
            for (tab in searchResult.getAllTabs()) {
                db.dataAccess().insert(tab)
            }

            Log.d(LOG_NAME, "Successful search for $query page $page.  Results: ${searchResult.getSongs().size}")
            Tab.fromTabDataType(searchResult.getSongs())
        }
    }
}

class SearchDidYouMeanException(val didYouMean: String): Exception()

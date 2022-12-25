package com.gbros.tabslite.workers

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import android.util.Log
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gbros.tabslite.HomeActivity
import com.gbros.tabslite.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
private const val LOG_NAME = "tabslite.SearchHelper"

object SearchHelper {
    // search suggestions
    val from = arrayOf("suggestion")
    val to = intArrayOf(R.id.suggestion_text)

    var InitilizationComplete = false

    private fun getSearchSuggestionAdapter(context: Context): SimpleCursorAdapter {
        return SimpleCursorAdapter(context, R.layout.list_item_search_suggestion,
                null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
    }

    private fun getObserverForSuggestionAdapter(adapter: SimpleCursorAdapter): Observer<MatrixCursor> {
        return Observer<MatrixCursor> { newSuggestion -> adapter.changeCursor(newSuggestion) }
    }

    private val suggestionCursor: MutableLiveData<MatrixCursor> by lazy {
        MutableLiveData<MatrixCursor>()
    }
    private fun getSuggestionCursor(): LiveData<MatrixCursor> {
        return suggestionCursor
    }
    @ExperimentalCoroutinesApi
    fun updateSuggestions(q: String) {
        Log.d(LOG_NAME, "updating search suggestions based on query $q")
        val suggestionsJob = GlobalScope.async { UgApi.searchSuggest(q) }
        suggestionsJob.start()
        suggestionsJob.invokeOnCompletion { cause ->
            val c = MatrixCursor(arrayOf(BaseColumns._ID, "suggestion"))
            if(cause != null){
                Log.i(LOG_NAME, "updateSuggestions' call to searchSuggest was cancelled.  This could be due to no results, which is normal.")
            } else {
                val suggestions = suggestionsJob.getCompleted()
                for (i in suggestions.indices) {
                    c.addRow(arrayOf(i, suggestions[i]))
                }
            }
            suggestionCursor.postValue(c)
        }
    }

    fun initializeSearchBar(defaultQuery: String, searchView: SearchView, context: Context,
                            owner: LifecycleOwner, searchCallback: (query: String) -> Unit) {
        if (!InitilizationComplete) {
            Log.d(LOG_NAME, "init search")
            //setup search bar
            val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(
                searchManager.getSearchableInfo(
                    ComponentName(
                        context,
                        HomeActivity::class.java
                    )
                )
            )
            searchView.isIconified = false
            searchView.setQuery(defaultQuery, false)
            searchView.clearFocus()


            // don't allow stacking of search activities.  If we search again, get rid of this instance
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    updateSuggestions(newText) //update the suggestions
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    searchCallback(query)
                    return true // false means tell the searchview that we didn't handle the search so it still calls another search
                }
            })

            //set up search suggestions
            searchView.suggestionsAdapter = getSearchSuggestionAdapter(context)
            getSuggestionCursor().observe(
                owner,
                getObserverForSuggestionAdapter(searchView.suggestionsAdapter as SimpleCursorAdapter)
            )


            val onSuggestionListener = object : SearchView.OnSuggestionListener {
                override fun onSuggestionClick(position: Int): Boolean {
                    val cursor: Cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                    val columnIndex = cursor.getColumnIndex("suggestion")
                    return if (columnIndex >= 0) {
                        val txt: String = cursor.getString(columnIndex)
                        searchView.setQuery(txt, true)
                        true
                    } else {
                        false
                    }
                }

                // todo: what does this mean?
                override fun onSuggestionSelect(position: Int): Boolean {
                    // Your code here
                    Log.v(LOG_NAME, "onSuggestionSelect for position $position")
                    return true
                }
            }
            searchView.setOnSuggestionListener(onSuggestionListener)
            InitilizationComplete = true;
        }
    }
}
package com.gbros.tabslite.workers

import android.content.Context
import android.database.MatrixCursor
import android.provider.BaseColumns
import android.util.Log
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.TabRequestType
import kotlinx.coroutines.*
private const val LOG_NAME = "tabslite.SearchHelper"

class SearchHelper(val context: Context?) {
    val gson = Gson()

    // search suggestions
    val from = arrayOf("suggestion")
    val to = intArrayOf(R.id.suggestion_text)
    var mAdapter: SimpleCursorAdapter = SimpleCursorAdapter(context, R.layout.list_item_search_suggestion,
            null, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
    private val suggestionCursor: MutableLiveData<MatrixCursor> by lazy {
        MutableLiveData<MatrixCursor>()
    }
    fun getSuggestionCursor(): LiveData<MatrixCursor> {
        return suggestionCursor
    }
    @ExperimentalCoroutinesApi
    fun updateSuggestions(q: String) {

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
    val suggestionObserver = Observer<MatrixCursor> { newSuggestion ->
        mAdapter.changeCursor(newSuggestion)
    }



}
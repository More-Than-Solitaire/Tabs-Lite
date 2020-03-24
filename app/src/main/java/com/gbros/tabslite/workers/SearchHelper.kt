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
import com.gbros.tabslite.utilities.ApiHelper
import kotlinx.coroutines.*
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL

class SearchHelper(val context: Context?) {
    val api: UgApi? = context?.let { UgApi(it) }
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

        val suggestionsJob = GlobalScope.async { api?.searchSuggest(q) }
        suggestionsJob.start()
        suggestionsJob.invokeOnCompletion { cause ->
            val c = MatrixCursor(arrayOf(BaseColumns._ID, "suggestion"))
            if(cause != null){
                Log.i(javaClass.simpleName, "updateSuggestions' call to searchSuggest was cancelled.  This could be due to no results, which is normal.")
            } else {
                val suggestions = suggestionsJob.getCompleted()
                if (suggestions != null) {
                    for (i in suggestions.indices) {
                        c.addRow(arrayOf(i, suggestions[i]))
                    }
                }
            }
            suggestionCursor.postValue(c)
        }
    }
    val suggestionObserver = Observer<MatrixCursor> { newSuggestion ->
        mAdapter.changeCursor(newSuggestion)
    }


    suspend fun updateTabTransposeLevel(tabId: Int, currValue: Int) = coroutineScope {
        if(context == null){
            Log.e(javaClass.simpleName, "UpdateTabTransposeLevel failed because context was null.")
            Unit
        } else {
            val database = AppDatabase.getInstance(context).tabFullDao()
            database.updateTransposed(tabId, currValue)
        }
    }


    suspend fun fetchTab(tabId: Int, force: Boolean = false, tabAccessType: String = "public"): Boolean = coroutineScope {
        // get the tab and corresponding chords, and put them in the database.  Then return true
        try {
            if(context == null){
                Log.e(this.javaClass.simpleName,"Error getting TabFull $tabId. Context was null.")
                false
            } else {
                val database = AppDatabase.getInstance(context)
                if (database.tabFullDao().exists(tabId) && !force) {
                    true
                } else {
                    try {
                        while(ApiHelper.updatingApiKey){
                            delay(20)
                        }
                        var apiKey = ApiHelper.apiKey
                        val deviceId = ApiHelper.getDeviceId()


                        var conn = URL("https://api.ultimate-guitar.com/api/v1/tab/info?tab_id=$tabId&tab_access_type=$tabAccessType").openConnection() as HttpURLConnection
                        conn.setRequestProperty("Accept-Charset", "utf-8")
                        conn.setRequestProperty("Accept", "application/json")
                        conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
                        conn.setRequestProperty("x-ug-client-id", deviceId)                   // stays constant over time; api key and client id are related to each other.
                        conn.setRequestProperty("x-ug-api-key", apiKey)     // updates periodically.

                        // handle when the api key is outdated
                        if(conn.responseCode == 498) {
                            conn.disconnect()
                            ApiHelper.updateApiKey()

                            apiKey = ApiHelper.apiKey
                            conn = URL("https://api.ultimate-guitar.com/api/v1/tab/info?tab_id=$tabId&tab_access_type=$tabAccessType").openConnection() as HttpURLConnection
                            conn.setRequestProperty("Accept", "application/json")
                            conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
                            conn.setRequestProperty("x-ug-client-id", deviceId)                   // stays constant over time; api key and client id are related to each other.
                            conn.setRequestProperty("x-ug-api-key", apiKey)     // updates periodically.
                            conn.setRequestProperty("Accept-Encoding", "identity")
                        }


                        val inputStream = conn.getInputStream()
                        val jsonReader = JsonReader(inputStream.reader())
                        val tabRequestTypeToken = object : TypeToken<TabRequestType>() {}.type
                        val result: TabRequestType = gson.fromJson(jsonReader, tabRequestTypeToken)
                        database.tabFullDao().insert(result.getTabFull())
                        database.chordVariationDao().insertAll(result.getChords())  // save all the chords we come across.  Might as well since we already downloaded them.
                        inputStream.close()

                        true
                    } catch (ex: FileNotFoundException) {
                        Log.e(this.javaClass.simpleName, "Error; 404 returned on fetch tab.  Maybe tabId $tabId doesn't exist?  This shouldn't happen.", ex)
                        false
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(this.javaClass.simpleName, "Error getting TabFull (id: $tabId) from function SearchHelper.fetchTab", ex)
            false
        }
    }
}
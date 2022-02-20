package com.gbros.tabslite.utilities

import android.util.Log
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.TabRequestType
import com.gbros.tabslite.workers.UgApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope

private const val LOG_NAME = "tabslite.TabHelper   "

object TabHelper {
    suspend fun updateTabTransposeLevel(tabId: Int, currValue: Int, database: AppDatabase) = coroutineScope {
        database.tabFullDao().updateTransposed(tabId, currValue)
    }


    /**
     * Gets tab based on tabId.  First checks the internal database for a cache hit, and if successful returns that. If
     * not (or if the force parameter is enabled), the tab is loaded from the internet.  If an internet load is performed
     * then the resulting tab and chords are cached automatically in the app database.
     *
     * @param tabId         The ID of the tab to load
     * @param database      The database instance to load a tab from (or into)
     * @param force         (Optional) if true, the app will skip the database cache check and reload the tab from the internet
     * @param tabAccessType (Optional) string parameter for internet tab load request
     */
    suspend fun fetchTabFromInternet(tabId: Int, database: AppDatabase, force: Boolean = false, tabAccessType: String = "public"): Boolean = coroutineScope {
        // get the tab and corresponding chords, and put them in the database.  Then return true
        if (!force && database.tabFullDao().exists(tabId)) {
            Log.v(LOG_NAME, "Cache hit for tab $tabId.  Not fetching from internet.")
            true
        } else {
            Log.v(LOG_NAME, "Loading tab $tabId.")
            val url = "https://api.ultimate-guitar.com/api/v1/tab/info?tab_id=$tabId&tab_access_type=$tabAccessType"
            val inputStream = UgApi.authenticatedStream(url)
            if(inputStream != null) {
                Log.v(LOG_NAME, "Obtained input stream for tab $tabId.")
                val jsonReader = JsonReader(inputStream.reader())
                val tabRequestTypeToken = object : TypeToken<TabRequestType>() {}.type
                val result: TabRequestType = Gson().fromJson(jsonReader, tabRequestTypeToken)
                Log.v(LOG_NAME, "Parsed response for tab $tabId.")

                database.tabFullDao().insert(result.getTabFull())
                database.chordVariationDao().insertAll(result.getChordVariations())  // save all the chords we come across.  Might as well since we already downloaded them.
                Log.v(LOG_NAME, "Inserted tab and chords into database for tab $tabId.")

                inputStream.close()

                true
            } else {
                Log.e(LOG_NAME, "Error fetching tab with tabId $tabId.  This shouldn't happen")
                cancel("Error fetching tab with tabId $tabId.  This shouldn't happen")
                false
            }
        }
    }
}
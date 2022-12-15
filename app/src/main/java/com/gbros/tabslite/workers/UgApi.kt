package com.gbros.tabslite.workers

import android.util.Log
import com.gbros.tabslite.data.*
import com.gbros.tabslite.utilities.ApiHelper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.*

private const val LOG_NAME = "tabslite.UgApi"

object UgApi {
    private val gson = Gson()

    private var lastSuggestionRequest = ""
    private var lastResult = SearchSuggestionType(emptyList())
    suspend fun searchSuggest(q: String): List<String> = coroutineScope {

        var result = lastResult.suggestions
        var query = q
        try {
            if (query.length > 5) { // ug api only allows a max of 5 chars for search suggestion requests.  rest of processing is done in app
                query = query.slice(0 until 5)
            }

            if (query == lastSuggestionRequest) {
                //processing past 5 chars is done in app
                result = lastResult.suggestions.filter { s -> s.contains(q) }
            } else {

                val connection = URL("https://api.ultimate-guitar.com/api/v1/tab/suggestion?q=$query").openConnection() as HttpURLConnection
                val inputStream = connection.inputStream
                val jsonReader = JsonReader(inputStream.reader())
                val searchSuggestionTypeToken = object : TypeToken<SearchSuggestionType>() {}.type
                lastResult = gson.fromJson(jsonReader, searchSuggestionTypeToken)
                result = lastResult.suggestions
                inputStream.close()
            }
        } catch (ex: FileNotFoundException) {
            // no suggestions for this query
            Log.i(LOG_NAME, "Search suggestions file not found for query $query.", ex)
            this.cancel("SearchSuggest coroutine canceled due to 404", ex)
        } catch (ex: Exception) {
            Log.e(LOG_NAME, "SearchSuggest error while finding search suggestions.")
            this.cancel("SearchSuggest coroutine canceled due to unexpected error", ex)
        }


        // suggestion caching
        lastSuggestionRequest = query

        // processing past 5 chars is done in app
        result
    }

    suspend fun search(q: String, pageNum: Int = 1): SearchRequestType = coroutineScope {
        val url = "https://api.ultimate-guitar.com/api/v1/tab/search?title=$q&page=$pageNum&type[]=300&official[]=0"
        val inputStream = authenticatedStream(url)
        if (inputStream != null) {
            val jsonReader = JsonReader(inputStream.reader())
            var result = SearchRequestType()

            try {
                val searchResultTypeToken = object : TypeToken<SearchRequestType>() {}.type
                result = gson.fromJson(jsonReader, searchResultTypeToken)
            } catch (ex: JsonSyntaxException) {
                Log.v(LOG_NAME, "Search exception.  Probably just a 'did you mean' search suggestion at the end.")
                try {
                    val stringTypeToken = object : TypeToken<String>() {}.type
                    val suggestedSearch: String = gson.fromJson(jsonReader, stringTypeToken)

                    this.cancel("search:$suggestedSearch")
                } catch (ex: IllegalStateException) {
                    inputStream.close()
                    Log.e(LOG_NAME, "Search illegal state exception!  Check SearchRequestType for consistency with data.  Url: $url", ex)
                    this.cancel("Illegal State.", ex)
                    throw ex
                }
            }
            inputStream.close()

            Log.v(LOG_NAME, "Search for $q page $pageNum success.")
            result
        } else {
            Log.i(LOG_NAME, "Error getting search results.  Probably just the end of results for query $q")
            cancel("Error getting search results.  Probably just the end of results for query $q")
            SearchRequestType()
        }
    }

    suspend fun updateChordVariations(chordIds: List<CharSequence>, database: AppDatabase, force: Boolean = false, tuning: String = "E A D G B E",
                                   instrument: String = "guitar") = coroutineScope {
        var chordParam = ""
        for (chord in chordIds) {
            if (force || !database.chordVariationDao().chordExists(chord.toString())){ // if the chord already exists in the db at all, we can assume we have all variations of it.  Not often a new chord is created
                val uChord = URLEncoder.encode(chord.toString(), "utf-8")
                chordParam += "&chords[]=$uChord"
            }
        }

        if (chordParam.isNotEmpty()) {
            val uTuning = URLEncoder.encode(tuning, "utf-8")
            val uInstrument = URLEncoder.encode(instrument, "utf-8")
            val url = "https://api.ultimate-guitar.com/api/v1/tab/applicature?instrument=$uInstrument&tuning=$uTuning$chordParam"
            val inputStream = authenticatedStream(url)
            if (inputStream != null) {
                val jsonReader = JsonReader(inputStream.reader())
                val chordRequestTypeToken = object : TypeToken<List<TabRequestType.ChordInfo>>() {}.type
                val results: List<TabRequestType.ChordInfo> = gson.fromJson(jsonReader, chordRequestTypeToken)
                for (result in results) {
                    database.chordVariationDao().insertAll(result.getChordVariations())
                }
                inputStream.close()
            } else {
                val chordCount = chordIds.size
                Log.i(LOG_NAME, "Error fetching chords.  chordParam is empty.  That means all the chords are already in the database.  Chord count that we're looking for: $chordCount.")
                cancel("Error fetching chord(s).")
            }
        }
    }

    suspend fun getChordVariations(chordId: CharSequence, database: AppDatabase): List<ChordVariation> = coroutineScope {
        database.chordVariationDao().getChordVariations(chordId.toString())
    }

    suspend fun fetchTopTabs(appDatabase: AppDatabase, force: Boolean = false) = coroutineScope {
        // 'type[]=300' means just chords (all instruments? use 300, 400, 700, and 800)
        // 'order=hits_daily' means get top tabs today not overall.  For overall use 'hits'
        val inputStream = authenticatedStream("https://api.ultimate-guitar.com/api/v1/tab/explore?date=0&genre=0&level=0&order=hits_daily&page=1&type=0&official=0")
        val playlistEntryDao = appDatabase.playlistEntryDao()
        val tabFullDao = appDatabase.tabFullDao()
        if (inputStream != null) {
            val jsonReader = JsonReader(inputStream.reader())
            val typeToken = object : TypeToken<List<SearchRequestType.Tab>>() {}.type
            val topTabs: List<TabFull> = (gson.fromJson(jsonReader, typeToken) as List<SearchRequestType.Tab>).map { t -> t.tabFull() }
            inputStream.close()

            // clear top tabs playlist, then add all these to the top tabs playlist
            playlistEntryDao.clearTopTabsPlaylist()
            var prevId: Int?
            var currentId: Int? = null
            var nextId: Int? = null
            for (tab in topTabs) {
                prevId = currentId
                currentId = nextId
                nextId = tab.tabId
                if (currentId != null) {
                    playlistEntryDao.insert(-2, currentId, nextId, prevId, System.currentTimeMillis(), 0)
                }
                tabFullDao.insert(tab)
            }
            playlistEntryDao.insert(-2, nextId!!, null, currentId, System.currentTimeMillis(), 0)  // save the last one
        } else {
            Log.w(LOG_NAME, "Error fetching top tabs.  AuthenticatedStream returned null.  Could be due to no internet access.")
            cancel("Error fetching top tabs.  AuthenticatedStream returned null.  Could be due to no internet access.")
        }
    }

    suspend fun authenticatedStream(url: String): InputStream? = coroutineScope {
        Log.v(LOG_NAME, "Getting authenticated stream for url: $url.")
        while (ApiHelper.updatingApiKey) {
            delay(20)
        }
        Log.v(LOG_NAME, "API helper finished updating.")
        var apiKey: String
        if(!ApiHelper.apiInit){
            ApiHelper.updateApiKey()  // try to initialize ourselves

            // if that didn't work, we don't have internet.
            if(!ApiHelper.apiInit) {
                Log.w(LOG_NAME, "Not fetching url $url.  API Key initialization failed.  Likely no internet access.")
                cancel("API Key initialization failed.  Likely no internet access.")
                return@coroutineScope null
            }
        }
        apiKey = ApiHelper.apiKey
        val deviceId = ApiHelper.getDeviceId()
        Log.v(LOG_NAME, "Got api key $apiKey and device id $deviceId.")

        var responseCode = 0
        try {
            var conn = URL(url).openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept-Charset", "utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.12 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
            conn.setRequestProperty("x-ug-client-id", deviceId)             // stays constant over time; api key and client id are related to each other.
            conn.setRequestProperty("x-ug-api-key", apiKey)                 // updates periodically.
            conn.connectTimeout = (5000)  // timeout of 5 seconds
            conn.readTimeout = 6000
            responseCode = conn.responseCode
            Log.v(LOG_NAME, "Retrieved URL with response code $responseCode.")

            // handle when the api key is outdated
            if (responseCode == 498) {
                Log.i(LOG_NAME, "498 response code for old api key $apiKey and device id $deviceId.  Refreshing api key")
                conn.disconnect()

                apiKey = ApiHelper.updateApiKey() ?: ""
                while (ApiHelper.updatingApiKey) {
                    delay(20)
                }
                Log.v(LOG_NAME, "Got new api key ($apiKey)")

                if (apiKey != "") {
                    apiKey = ApiHelper.apiKey
                    conn = URL(url).openConnection() as HttpURLConnection
                    conn.setRequestProperty("Accept", "application/json")
                    conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.12 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
                    conn.setRequestProperty("x-ug-client-id", deviceId)                   // stays constant over time; api key and client id are related to each other.
                    conn.setRequestProperty("x-ug-api-key", apiKey)     // updates periodically.
                    conn.connectTimeout = (5000)  // timeout of 5 seconds
                    conn.readTimeout = 6000

                    responseCode = 0 - conn.responseCode
                    Log.v(LOG_NAME, "Retrieved URL with new API key, getting response code $responseCode (negative to signify that it's the second time through).")
                } else {
                    // we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.
                    val msg = "498 response code, but api key update returned null! Generally this means we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.  Either precisely perfect timing or something's wrong."
                    Log.e(LOG_NAME, msg)
                    throw Exception(msg)
                }
            }

            val inputStream = conn.getInputStream()
            Log.v(LOG_NAME, "Success fetching url $url")
            inputStream
        } catch (ex: FileNotFoundException) {
            Log.i(LOG_NAME, "404 NOT FOUND during fetch of url $url with parameters apiKey: $apiKey and deviceId: $deviceId.  Response code $responseCode (negative number means it was set after refreshing api key)")
            cancel("Not Found", ex)
            null
        } catch (ex: ConnectException){
            Log.i(LOG_NAME, "Could not fetch $url. Response code 0 (no internet access).  Java.net.ConnectException.")
            cancel("Not Connected to the Internet.")
            null
        }  catch (ex: SocketTimeoutException){
            Log.i(LOG_NAME, "Could not fetch $url. Response code 0 (no internet access).  Java.net.SocketTimeoutException.")
            cancel("Not Connected to the Internet.")
            null
        } catch (ex: Exception) {
            Log.e(LOG_NAME, "Exception during fetch of url $url with parameters apiKey: $apiKey and deviceId: $deviceId.  Response code $responseCode (negative number means it was set after refreshing api key)", ex)
            cancel("Exception!", ex)
            null
        }
    }
}
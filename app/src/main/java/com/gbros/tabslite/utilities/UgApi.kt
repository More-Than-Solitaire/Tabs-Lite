package com.gbros.tabslite.utilities

import android.util.Log
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.servertypes.SearchRequestType
import com.gbros.tabslite.data.servertypes.SearchSuggestionType
import com.gbros.tabslite.data.servertypes.TabRequestType
import com.gbros.tabslite.data.tab.TabDataType
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

private const val LOG_NAME = "tabslite.UgApi         "

/**
 * The API interface handling all API-specific logic to get data from the server (or send to the server)
 */
object UgApi {
    // region private data

    private val gson = Gson()

    private var lastSuggestionRequest = ""
    private var lastResult = SearchSuggestionType(emptyList())

    // endregion

    // region public methods

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
                val connection =
                    URL("https://api.ultimate-guitar.com/api/v1/tab/suggestion?q=$query").openConnection() as HttpURLConnection
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
            Log.e(LOG_NAME, "SearchSuggest error while finding search suggestions.", ex)
            this.cancel("SearchSuggest coroutine canceled due to unexpected error", ex)
        }

        // suggestion caching
        lastSuggestionRequest = query

        // processing past 5 chars is done in app
        result
    }

    /**
     * Perform a search for the given query, and get the tabs that match that search query.
     *
     * @param [query] The search term to find
     * @param [page] The page of the search results to fetch
     *
     * @return A [SearchRequestType] with the search results, or an empty [SearchRequestType] if there are no search results on that page
     */
    suspend fun search(query: String, page: Int): SearchRequestType {
        val url =
            "https://api.ultimate-guitar.com/api/v1/tab/search?title=$query&page=$page&type[]=300&official[]=0"

        val inputStream: InputStream?
        try {
            inputStream = authenticatedStream(url)
        } catch (ex: Exception) {
            // end of search results
            return SearchRequestType()
        }

        var result: SearchRequestType
        val jsonReader = JsonReader(inputStream.reader())

        try {
            val searchResultTypeToken = object : TypeToken<SearchRequestType>() {}.type
            result = gson.fromJson(jsonReader, searchResultTypeToken)
            Log.v(LOG_NAME, "Search for $query page $page success.")
        } catch (syntaxException: JsonSyntaxException) {
            // usually this block happens when the end of the exact query is reached and a 'did you mean' suggestion is available
            Log.v(
                LOG_NAME,
                "Search exception.  Probably just a 'did you mean' search suggestion at the end."
            )
            try {
                val stringTypeToken = object : TypeToken<String>() {}.type
                val suggestedSearch: String = gson.fromJson(jsonReader, stringTypeToken)

                result = SearchRequestType(suggestedSearch)
            } catch (ex: IllegalStateException) {
                inputStream.close()
                Log.e(
                    LOG_NAME,
                    "Search illegal state exception!  Check SearchRequestType for consistency with data.  Query: $query, page $page",
                    syntaxException
                )

                throw Exception(
                    "Search illegal state exception!  Check SearchRequestType for consistency with data.  Query: $query, page $page",
                    ex
                )
            }
        } finally {
            inputStream.close()
        }

        return result
    }

    suspend fun updateChordVariations(
        chordIds: List<CharSequence>,
        database: AppDatabase,
        force: Boolean = false,
        tuning: String = "E A D G B E",
        instrument: String = "guitar"
    ) = coroutineScope {
        var chordParam = ""
        for (chord in chordIds) {
            if (force || !database.chordVariationDao()
                    .chordExists(chord.toString())
            ) { // if the chord already exists in the db at all, we can assume we have all variations of it.  Not often a new chord is created
                val uChord = URLEncoder.encode(chord.toString(), "utf-8")
                chordParam += "&chords[]=$uChord"
            }
        }

        if (chordParam.isNotEmpty()) {
            val uTuning = URLEncoder.encode(tuning, "utf-8")
            val uInstrument = URLEncoder.encode(instrument, "utf-8")
            val url =
                "https://api.ultimate-guitar.com/api/v1/tab/applicature?instrument=$uInstrument&tuning=$uTuning$chordParam"
            try {
                val inputStream = authenticatedStream(url)
                val jsonReader = JsonReader(inputStream.reader())
                val chordRequestTypeToken =
                    object : TypeToken<List<TabRequestType.ChordInfo>>() {}.type
                val results: List<TabRequestType.ChordInfo> =
                    gson.fromJson(jsonReader, chordRequestTypeToken)
                for (result in results) {
                    database.chordVariationDao().insertAll(result.getChordVariations())
                }
                inputStream.close()
            } catch (ex: Exception) {
                val chordCount = chordIds.size
                Log.i(LOG_NAME, "Error fetching chords.  chordParam is empty.  That means all the chords are already in the database.  Chord count that we're looking for: $chordCount.")
                cancel("Error fetching chord(s).")
            }
        }
    }

    suspend fun fetchTopTabs(appDatabase: AppDatabase) = coroutineScope {
        try {
            // 'type[]=300' means just chords (all instruments? use 300, 400, 700, and 800)
            // 'order=hits_daily' means get top tabs today not overall.  For overall use 'hits'
            val inputStream: InputStream = authenticatedStream("https://api.ultimate-guitar.com/api/v1/tab/explore?date=0&genre=0&level=0&order=hits_daily&page=1&type=0&official=0")
            val playlistEntryDao = appDatabase.playlistEntryDao()
            val tabFullDao = appDatabase.tabFullDao()
            val jsonReader = JsonReader(inputStream.reader())
            val typeToken = object : TypeToken<List<SearchRequestType.SearchResultTab>>() {}.type
            val topTabs: List<TabDataType> = (gson.fromJson(
                jsonReader,
                typeToken
            ) as List<SearchRequestType.SearchResultTab>).map { t -> t.tabFull() }
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
                    playlistEntryDao.insert(
                        TOP_TABS_PLAYLIST_ID,
                        currentId,
                        nextId,
                        prevId,
                        System.currentTimeMillis(),
                        0
                    )
                }
                tabFullDao.insert(tab)
            }
            playlistEntryDao.insert(
                TOP_TABS_PLAYLIST_ID,
                nextId!!,
                null,
                currentId,
                System.currentTimeMillis(),
                0
            )  // save the last one
        } catch (ex: Exception) {
            Log.w(LOG_NAME, "Couldn't fetch top tabs.", ex)
        }
    }

    /**
     * Gets tab based on tabId.  Loads tab from internet and caches the result automatically in the
     * app database.
     *
     * @param tabId         The ID of the tab to load
     * @param database      The database instance to load a tab from (or into)
     * @param tabAccessType (Optional) string parameter for internet tab load request
     */
    suspend fun fetchTabFromInternet(
        tabId: Int,
        database: AppDatabase,
        tabAccessType: String = "public"
    ): TabDataType = withContext(Dispatchers.IO) {
        // get the tab and corresponding chords, and put them in the database.  Then return true
        Log.v(LOG_NAME, "Loading tab $tabId.")
        val url =
            "https://api.ultimate-guitar.com/api/v1/tab/info?tab_id=$tabId&tab_access_type=$tabAccessType"
        val requestResponse: TabRequestType = with(authenticatedStream(url)) {
            val jsonReader = JsonReader(reader())
            val tabRequestTypeToken = object : TypeToken<TabRequestType>() {}.type
            Gson().fromJson(jsonReader, tabRequestTypeToken)
        }

        Log.v(
            LOG_NAME,
            "Parsed response for tab $tabId. Name: ${requestResponse.song_name}, capo ${requestResponse.capo}"
        )

        // save all the chords we come across.  Might as well since we already downloaded them.
        database.chordVariationDao().insertAll(requestResponse.getChordVariations())

        val result = requestResponse.getTabFull()
        database.tabFullDao().insert(result)
        return@withContext result
    }

    // endregion

    // region private methods

    private suspend fun authenticatedStream(url: String): InputStream = withContext(Dispatchers.IO) {
        Log.v(LOG_NAME, "Getting authenticated stream for url: $url.")
        while (ApiHelper.updatingApiKey) {
            delay(20)
        }
        Log.v(LOG_NAME, "API helper finished updating.")
        var apiKey: String
        if (!ApiHelper.apiInit) {
            ApiHelper.updateApiKey()  // try to initialize ourselves

            // if that didn't work, we don't have internet.
            if (!ApiHelper.apiInit) {
                throw Exception("API Key initialization failed while fetching $url.  Likely no internet access.")
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
            conn.setRequestProperty(
                "User-Agent",
                "UGT_ANDROID/5.10.12 ("
            )  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
            conn.setRequestProperty(
                "x-ug-client-id",
                deviceId
            )             // stays constant over time; api key and client id are related to each other.
            conn.setRequestProperty(
                "x-ug-api-key",
                apiKey
            )                 // updates periodically.
            conn.connectTimeout = (5000)  // timeout of 5 seconds
            conn.readTimeout = 6000
            responseCode = conn.responseCode
            Log.v(LOG_NAME, "Retrieved URL with response code $responseCode.")

            // handle when the api key is outdated
            if (responseCode == 498) {
                Log.i(
                    LOG_NAME,
                    "498 response code for old api key $apiKey and device id $deviceId.  Refreshing api key"
                )
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
                    conn.setRequestProperty(
                        "User-Agent",
                        "UGT_ANDROID/5.10.12 ("
                    )  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
                    conn.setRequestProperty(
                        "x-ug-client-id",
                        deviceId
                    )                   // stays constant over time; api key and client id are related to each other.
                    conn.setRequestProperty("x-ug-api-key", apiKey)     // updates periodically.
                    conn.connectTimeout = (5000)  // timeout of 5 seconds
                    conn.readTimeout = 6000

                    responseCode = 0 - conn.responseCode
                    Log.v(
                        LOG_NAME,
                        "Retrieved URL with new API key, getting response code $responseCode (negative to signify that it's the second time through)."
                    )
                } else {
                    // we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.
                    val msg =
                        "498 response code, but api key update returned null! Generally this means we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.  Either precisely perfect timing or something's wrong."
                    Log.e(LOG_NAME, msg)
                    throw Exception(msg)
                }
            }

            val inputStream = conn.inputStream
            Log.v(LOG_NAME, "Success fetching url $url")
            return@withContext inputStream
        } catch (ex: FileNotFoundException) {
            throw Exception("Normal: 404 NOT FOUND during fetch of url $url with parameters apiKey: " +
                    "$apiKey and deviceId: $deviceId.  Response code $responseCode (negative number " +
                    "means it was set after refreshing api key)", ex)
        } catch (ex: ConnectException) {
            throw Exception("Normal: Could not fetch $url. Response code 0 (no internet access).", ex)
        } catch (ex: SocketTimeoutException) {
            throw Exception("Normal: Could not fetch $url. Response code 0 (no internet access).", ex)
        } catch (ex: Exception) {
            throw Exception("Unexpected exception during fetch of url $url with parameters apiKey: " +
                    "$apiKey and deviceId: $deviceId.  Response code $responseCode (negative number " +
                    "means it was set after refreshing api key)", ex)
        }
    }

    // endregion
}
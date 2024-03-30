package com.gbros.tabslite.utilities

import android.os.Build
import android.util.Log
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.servertypes.SearchRequestType
import com.gbros.tabslite.data.servertypes.SearchSuggestionType
import com.gbros.tabslite.data.servertypes.ServerTimestampType
import com.gbros.tabslite.data.servertypes.TabRequestType
import com.gbros.tabslite.data.tab.TabDataType
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.InputStream
import java.math.BigInteger
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

private const val LOG_NAME = "tabslite.UgApi         "

/**
 * The API interface handling all API-specific logic to get data from the server (or send to the server)
 */
object UgApi {
    // region private data

    private val gson = Gson()
    private val cachedSearchSuggestions = HashMap<String, List<String>>()

    private var apiKey: String? = null

    // endregion

    // region public data

    private var storeDeviceId: String? = null
    private val deviceId: String
        get() = fetchDeviceId()

    // endregion

    // region public methods

    /**
     * Get search suggestions for the given query.  Caches search suggestions internally for faster
     * update second time through.
     *
     * @param [q]: The query to fetch search suggestions for
     *
     * @return A string list of suggested searches, or an empty list if no suggestions could be found.
     */
    suspend fun searchSuggest(q: String): List<String> = withContext(Dispatchers.IO) {
        // If we've already cached search suggestions for this query, skip the internet call and return them directly
        if (cachedSearchSuggestions.contains(q)) {
            return@withContext cachedSearchSuggestions.getValue(q)
        } else if (q.length > 5 && cachedSearchSuggestions.contains(q.slice(0 until 5))) {
            // ug api only allows a max of 5 chars for search suggestion requests.  rest of processing is done in app
            val cachedSuggestions = cachedSearchSuggestions.getValue(q.slice(0 until 5))
            return@withContext cachedSuggestions.filter { s -> s.contains(q) }
        }

        // fetch search suggestions from the internet
        try {
            var query = q
            if (q.length > 5) { // ug api only allows a max of 5 chars for search suggestion requests.  rest of processing is done in app
                query = q.slice(0 until 5)
            }

            val connection = URL("https://api.ultimate-guitar.com/api/v1/tab/suggestion?q=$query").openConnection() as HttpURLConnection
            val suggestions = connection.inputStream.use {inputStream ->
                val jsonReader = JsonReader(inputStream.reader())
                val searchSuggestionTypeToken = object : TypeToken<SearchSuggestionType>() {}.type
                gson.fromJson<SearchSuggestionType?>(jsonReader, searchSuggestionTypeToken).suggestions
            }

            if (suggestions.isNotEmpty()) {
                cachedSearchSuggestions[query] = suggestions
            }

            if (q.length > 5) {
                return@withContext suggestions.filter { s -> s.contains(q) }
            } else {
                return@withContext suggestions
            }
        } catch (ex: Exception) {
            Log.e(LOG_NAME, "SearchSuggest ${ex.javaClass.canonicalName} while finding search suggestions. Probably no internet; returning empty search suggestion list", ex)
            return@withContext listOf()
        }
    }

    /**
     * Perform a search for the given query, and get the tabs that match that search query.
     *
     * @param [query] The search term to find
     * @param [page] The page of the search results to fetch
     *
     * @return A [SearchRequestType] with the search results, or an empty [SearchRequestType] if there are no search results on that page
     */
    suspend fun search(query: String, page: Int): SearchRequestType = withContext(Dispatchers.IO) {
        val url =
            "https://api.ultimate-guitar.com/api/v1/tab/search?title=$query&page=$page&type[]=300&official[]=0"

        val inputStream: InputStream?
        try {
            inputStream = authenticatedStream(url)
        } catch (ex: Exception) {
            // end of search results
            return@withContext SearchRequestType()
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

        return@withContext result
    }

    /**
     * Retrieves updated chord charts for the passed list of chords from the internet API, saves them
     * to the database, and returns a map from each chord name passed to the list of chord charts
     *
     * @param chordIds: List of chord names to fetch.  E.g. A#m7, Gsus, A
     * @param database: Database to save the updated chords to
     * @param tuning: The instrument tuning for the tabs to fetch
     * @param instrument: The instrument to fetch chords for.  Default: 'guitar'.
     *
     * @return Map from chord ID to the list of [ChordVariation] for that chord
     */
    suspend fun updateChordVariations(
        chordIds: List<CharSequence>,
        database: AppDatabase,
        tuning: String = "E A D G B E",
        instrument: String = "guitar"
    ): Map<String, List<ChordVariation>> = withContext(Dispatchers.IO) {
        val resultMap: MutableMap<String, List<ChordVariation>> = mutableMapOf()

        var chordParam = ""
        for (chord in chordIds) {
            val uChord = URLEncoder.encode(chord.toString(), "utf-8")
            chordParam += "&chords[]=$uChord"
        }

        val uTuning = URLEncoder.encode(tuning, "utf-8")
        val uInstrument = URLEncoder.encode(instrument, "utf-8")
        val url =
            "https://api.ultimate-guitar.com/api/v1/tab/applicature?instrument=$uInstrument&tuning=$uTuning$chordParam"
        try {
            val results: List<TabRequestType.ChordInfo> = authenticatedStream(url).use { inputStream ->
                val jsonReader = JsonReader(inputStream.reader())
                val chordRequestTypeToken =
                    object : TypeToken<List<TabRequestType.ChordInfo>>() {}.type
                gson.fromJson(jsonReader, chordRequestTypeToken)
            }
            for (result in results) {
                resultMap[result.chord] = result.getChordVariations()
                database.chordVariationDao().insertAll(result.getChordVariations())
            }
        } catch (ex: Exception) {
            val chordCount = chordIds.size
            Log.i(
                LOG_NAME,
                "Error fetching chords.  chordParam: $chordParam.  That means all the chords are already in the database.  Chord count that we're looking for: $chordCount."
            )
            cancel("Error fetching chord(s).")
        }

        return@withContext resultMap
    }

    suspend fun fetchTopTabs(appDatabase: AppDatabase) = withContext(Dispatchers.IO) {
        try {
            val playlistEntryDao = appDatabase.playlistEntryDao()
            val tabFullDao = appDatabase.tabFullDao()

            // 'type[]=300' means just chords (all instruments? use 300, 400, 700, and 800)
            // 'order=hits_daily' means get top tabs today not overall.  For overall use 'hits'
            val topTabSearchResults = authenticatedStream("https://api.ultimate-guitar.com/api/v1/tab/explore?date=0&genre=0&level=0&order=hits_daily&page=1&type=0&official=0").use { inputStream ->
                val jsonReader = JsonReader(inputStream.reader())
                val typeToken = object : TypeToken<List<SearchRequestType.SearchResultTab>>() {}.type

                 return@use (gson.fromJson(
                    jsonReader,
                    typeToken
                ) as List<SearchRequestType.SearchResultTab>)
            }
            val topTabs: List<TabDataType> = topTabSearchResults.map { t -> t.tabFull() }

            if (topTabs.isEmpty()) {
                // don't overwrite with an empty list
                throw Exception("Top tabs result was empty: ${topTabSearchResults.size} results")
            }

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
        try {
            database.chordVariationDao().insertAll(requestResponse.getChordVariations())
        } catch (ex: Exception) {
            Log.w(LOG_NAME, "Couldn't get chord variations from tab $tabId", ex)
        }

        val result = requestResponse.getTabFull()
        database.tabFullDao().insert(result)
        return@withContext result
    }

    // endregion

    // region private methods

    /**
     * Gets an authenticated input stream for the passed API URL, updating the API key if needed
     *
     * @param url: The UG API url to start an authenticated InputStream with
     *
     * @return An [InputStream], authenticated with a valid API key
     */
    private suspend fun authenticatedStream(url: String): InputStream = withContext(Dispatchers.IO) {
        Log.v(LOG_NAME, "Getting authenticated stream for url: $url.")
        if (apiKey == null) {
            try {
                updateApiKey()
            } catch (ex: Exception) {
                throw Exception("API Key initialization failed while fetching $url.  Likely no internet access.", ex)
            }
        }

        // api key is not null
        Log.v(LOG_NAME, "Api key: $apiKey, device id: $deviceId.")

        var responseCode = 0
        try {
            var numTries = 0
            do {
                numTries++
                val conn = URL(url).openConnection() as HttpURLConnection
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

                if (responseCode == 498 && numTries == 1) {  // don't bother the second time through
                    Log.i(
                        LOG_NAME,
                        "498 response code for old api key $apiKey and device id $deviceId.  Refreshing api key"
                    )
                    conn.disconnect()

                    try {
                        updateApiKey()
                        Log.v(LOG_NAME, "Got new api key ($apiKey)")
                    } catch (ex: Exception) {
                        // we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.
                        val msg =
                            "498 response code, but api key update returned null! Generally this means we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.  Either precisely perfect timing or something's wrong."
                        throw Exception(msg, ex)
                    }
                } else {
                    Log.v(
                        LOG_NAME,
                        "(final try) Retrieved URL $url (${conn.requestMethod}) with response code $responseCode after $numTries try(s)."
                    )

                    if (responseCode != 498) {
                        return@withContext conn.inputStream
                    } else {
                        var content = ""

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                content = conn.inputStream.readAllBytes().toString()
                            } catch (_: Exception) { }
                        }

                        throw Exception("Couldn't fetch authenticated stream (498: bad token).  Response code: $responseCode, content: \n$content")
                    }
                }
            } while (true)

            throw Exception("Unreachable: Could not create authenticated stream.")  // shouldn't get here
        } catch (ex: FileNotFoundException) {
            throw Exception("Normal: 404 NOT FOUND during fetch of url $url with parameters apiKey: " +
                    "$apiKey and deviceId: $deviceId.  Response code $responseCode", ex)
        } catch (ex: ConnectException) {
            throw Exception("Normal: Could not fetch $url. Response code 0 (no internet access).", ex)
        } catch (ex: SocketTimeoutException) {
            throw Exception("Normal: Could not fetch $url. Response code 0 (no internet access).", ex)
        } catch (ex: Exception) {
            throw Exception("Unexpected exception during fetch of url $url with parameters apiKey: " +
                    "$apiKey and deviceId: $deviceId.  Response code $responseCode", ex)
        }
    }

    /**
     * Sets an updated [apiKey], based on the most recent server time.  This needs to be called
     * whenever we get a 498 response code
     *
     * @throws Exception if api key could not be updated (e.g. no internet access)
     */
    private suspend fun updateApiKey() {
        apiKey = null
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd:H", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val stringBuilder = StringBuilder(deviceId)

        try {
            val serverTime = fetchServerTime()
            stringBuilder.append(serverTime)
            stringBuilder.append("createLog()")
            apiKey = getMd5(stringBuilder.toString())

            if (apiKey.isNullOrBlank()) {
                throw Exception("API key update completed without fetching API key.  Server time: $serverTime.  API key: $apiKey")
            }
        } catch(ex: Exception) {
            throw Exception("Unable to update API key", ex)
        }
    }

    /**
     * Gets the current server time, for use in API calls
     *
     * @return The current time according to the server
     *
     * @throws Exception if time fetch could not be completed (e.g. if no internet access)
     */
    private suspend fun fetchServerTime(): String = withContext(Dispatchers.IO) {
        val devId = deviceId
        val lastResult: ServerTimestampType
        val conn = URL("https://api.ultimate-guitar.com/api/v1/common/hello").openConnection() as HttpURLConnection
        conn.setRequestProperty("Accept", "application/json")
        conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.12 (")  // actual value "UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)". 5.10.11 is the app version.
        conn.setRequestProperty("x-ug-client-id", devId)                // stays constant over time; api key and client id are related to each other.

        val serverTimestamp = try {
            conn.inputStream.use {inputStream ->
                val jsonReader = JsonReader(inputStream.reader())
                val serverTimestampTypeToken = object : TypeToken<ServerTimestampType>() {}.type
                lastResult = Gson().fromJson(jsonReader, serverTimestampTypeToken)
                lastResult
            }
        } catch (ex: Exception){
            throw Exception( "Error getting hello handshake (server time).  We may not be connected to the internet.", ex)
        }

        // read server time into our date type of choice
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd:H", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val formattedDateString = simpleDateFormat.format(serverTimestamp.getServerTime().time)

        Log.i(LOG_NAME, "Fetched server time: $formattedDateString}")
        return@withContext formattedDateString
    }

    /**
     * Hash a string using the MD5 algorithm
     *
     * @param [stringToHash]: The string to hash using the MD5 algorithm
     *
     * @return The MD5-hashed version of [stringToHash]
     *
     * @throws RuntimeException if the MD5 algorithm doesn't exist on this device
     */
    private fun getMd5(stringToHash: String): String {
        var ret = stringToHash

        try {
            ret = BigInteger(1, MessageDigest.getInstance("MD5").digest(ret.toByteArray())).toString(16)
            while (ret.length < 32) {
                val stringBuilder = java.lang.StringBuilder()
                stringBuilder.append("0")
                stringBuilder.append(ret)
                ret = stringBuilder.toString()
            }
            return ret
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            val runtimeException = RuntimeException("Could not complete MD5 hash", noSuchAlgorithmException)
            throw runtimeException
        }
    }

    /**
     * Ensures that we have a current deviceId stored.  Creates new ID if needed.  Shouldn't be called
     * directly; use [deviceId] instead.
     *
     * @return The current deviceId (setting it if need be)
     */
    private fun fetchDeviceId(): String {
        val copyOfCurrentDeviceId = storeDeviceId
        return if (copyOfCurrentDeviceId != null) {
            copyOfCurrentDeviceId
        } else {
            // generate a new device id
            var newId = ""
            val charList = charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
            while(newId.length < 16) {
                newId += charList[Random.nextInt(0, 15)]
            }
            storeDeviceId = newId
            newId
        }
    }

    // endregion
}
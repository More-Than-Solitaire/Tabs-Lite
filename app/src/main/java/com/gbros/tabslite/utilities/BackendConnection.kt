package com.gbros.tabslite.utilities

import android.accounts.AuthenticatorException
import android.content.res.Resources.NotFoundException
import android.os.Build
import android.util.Log
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.SearchSuggestions
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.playlist.Playlist.Companion.TOP_TABS_PLAYLIST_ID
import com.gbros.tabslite.data.servertypes.ArtistRequestType
import com.gbros.tabslite.data.servertypes.SearchRequestType
import com.gbros.tabslite.data.servertypes.SearchSuggestionType
import com.gbros.tabslite.data.servertypes.ServerTimestampType
import com.gbros.tabslite.data.servertypes.SongRequestType
import com.gbros.tabslite.data.servertypes.TabRequestType
import com.gbros.tabslite.data.tab.TabDataType
import com.gbros.tabslite.utilities.BackendConnection.apiKey
import com.gbros.tabslite.utilities.BackendConnection.deviceId
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/**
 * The API interface handling all API-specific logic to get data from the server (or send to the server)
 */
object BackendConnection {
    //#region private data

    private val db = Firebase.firestore

    private val gson = Gson()

    private var apiKey: String? = null

    private val apiKeyFetchLock: Mutex = Mutex(locked = false)

    //#endregion

    //#region public data

    private var storeDeviceId: String? = null
    private val deviceId: String
        get() = fetchDeviceId()

    //#endregion

    //#region public methods

    /**
     * Create a new song in the database.
     *
     * @param [song] the song to create.
     *
     * @return The ID of the created song
     */
    suspend fun createSong(song: SongRequestType): String {
        require(song.song_name.isNotBlank()) { "Song name cannot be blank" }
        require(song.artist_name.isNotBlank()) { "Artist name cannot be blank" }
        require(song.artist_id.isNotBlank()) { "Artist ID cannot be blank - ensure the artist has been created first." }
        require(song.song_genre.isNotBlank()) { "Song genre cannot be blank" }

        val newSongRef = db.collection("songs").document()
        song.song_id = newSongRef.id
        val json = Json.encodeToString(song)
        newSongRef.set(song).await()
        Log.v(TAG, "Created song ${newSongRef.id}")
        return newSongRef.id
    }

    /**
     * Get the artist ID of the artist with the passed name.
     *
     * @param [artistName] the name of the artist
     *
     * @return The ID of the artist with the passed name
     *
     * @throws NotFoundException if the artist with the passed name is not found in the database
     */
    suspend fun fetchArtistId(artistName: String): String {
        if (artistName.isBlank()) return ""

        val artistCollectionRef = db.collection("artists")
        val query = artistCollectionRef
            .whereEqualTo("artist_name", artistName)
            .limit(1)
        val querySnapshot = query.get().await()
        if (querySnapshot.isEmpty) {
            throw NotFoundException("Artist $artistName not found in database")
        }

        return querySnapshot.documents[0].id
    }

    /**
     * Create a new artist in the database.
     *
     * @param [artistName] the name of the artist
     *
     * @return The ID of the created artist
     */
    suspend fun createNewArist(artistName: String): String {
        val newArtistRef = db.collection("artists").document()
        val newArtist = ArtistRequestType(artist_name = artistName, artist_id = newArtistRef.id)
        newArtistRef.set(newArtist).await()
        return newArtistRef.id
    }

    /**
     * Create a new tab in the database. Fetches song info to insert into tab.
     *
     * @param [tab] the tab to create. `id`, `artist_id`, `artist_name`, `song_genre`, and `song_name` fields not respected.
     *
     * @return The ID of the created tab
     */
    suspend fun createTab(tab: TabRequestType): TabDataType {
        // fetch song info to insert into tab
        val songDetails = db.collection("songs").document(tab.song_id).get().await().data
            ?: throw NotFoundException("Song ${tab.song_id} not found in database")

        tab.artist_id = songDetails["artist_id"] as String
        tab.artist_name = songDetails["artist_name"] as String
        tab.song_genre = songDetails["song_genre"] as String
        tab.song_name = songDetails["song_name"] as String

        // set the date to now as an int
        tab.date = System.currentTimeMillis()

        val newTabRef = db.collection("tabs").document()
        tab.id = newTabRef.id
        newTabRef.set(tab).await()
        Log.v(TAG, "Created tab ${newTabRef.id}")
        return tab.getTabFull()
    }

    /**
     * Get search suggestions for the given query.  Stores search suggestions to the local database,
     * overwriting any previous search suggestions for the specified query
     *
     * @param [q]: The query to fetch search suggestions for
     *
     * @return A string list of suggested searches, or an empty list if no suggestions could be found.
     */
    suspend fun searchSuggest(q: String, dataAccess: DataAccess) = withContext(Dispatchers.IO) {
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
                gson.fromJson<SearchSuggestionType>(jsonReader, searchSuggestionTypeToken).suggestions
            }

            if (suggestions.isNotEmpty()) {
                dataAccess.upsert(SearchSuggestions(query = query, suggestions))
            }
            return@withContext
        } catch (_: FileNotFoundException) {
            // no search suggestions for this query
            return@withContext
        } catch (ex: UnknownHostException) {
            // no internet access
            throw NoInternetException("No internet access to fetch search suggestions for query $q", ex)
        } catch (ex: Exception) {
            val message = "SearchSuggest ${ex.javaClass.canonicalName} while finding search suggestions. Probably no internet; no search suggestions added"
            Log.e(TAG, message, ex)
            throw SearchException(message, ex)
        }
    }

    /**
     * Perform a search for the given query, and get the tabs that match that search query.
     *
     * @param [title] The search term to find
     * @param [artistId] (Optional) The ID of the artist to filter search results to. Can be paired with the [title] set to an empty string to perform an artist song list. If null or 0, this parameter will be ignored
     * @param [page] The 1-indexed page of the search results to fetch
     *
     * @return A [SearchRequestType] with the search results, or an empty [SearchRequestType] if there are no search results on that page
     */
    suspend fun search(title: String, artistId: String? = null, page: Int): SearchRequestType = withContext(Dispatchers.IO) {
        var artistQuery = artistId
        if (artistQuery != null) {
            artistQuery = artistId.replace("7567-", "") // search can't handle prefixed ids
            artistQuery = URLEncoder.encode(artistQuery, "utf-8")
            artistQuery = "&artist_id=$artistQuery"
        }
        else {
            artistQuery = ""
        }

        val url =
            "https://api.ultimate-guitar.com/api/v1/tab/search?title=$title&page=$page$artistQuery&type[]=300&official[]=0"

        val inputStream: InputStream?
        try {
            inputStream = authenticatedStream(url)
        } catch (_: NotFoundException) {
            // end of search results
            return@withContext SearchRequestType()
        } catch (ex: NoInternetException) {
            throw ex  // pass through NoInternetExceptions
        } catch (ex: Exception) {
            Log.e(TAG, "Unexpected exception reading search results for page $page of query '$title': ${ex.message}", ex)
            throw SearchException("Couldn't fetch search results for page $page of query '$title': ${ex.message}", ex)
        }

        var result: SearchRequestType
        val jsonReader = JsonReader(inputStream.reader())

        try {
            val searchResultTypeToken = object : TypeToken<SearchRequestType>() {}.type
            result = gson.fromJson(jsonReader, searchResultTypeToken)
            Log.v(TAG, "Search for $title page $page success.")
        } catch (syntaxException: JsonSyntaxException) {
            // usually this block happens when the end of the exact query is reached and a 'did you mean' suggestion is available
            try {
                val stringTypeToken = object : TypeToken<String>() {}.type
                val suggestedSearch: String = gson.fromJson(jsonReader, stringTypeToken)

                result = SearchRequestType(suggestedSearch)
            } catch (_: IllegalStateException) {
                inputStream.close()
                val message = "Search illegal state exception!  Check SearchRequestType for consistency with data.  Query: $title, page $page"
                Log.e(TAG, message, syntaxException)
                throw SearchException(message, syntaxException)
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
     * @param dataAccess: Database to save the updated chords to
     * @param instrument: The instrument to fetch chords for.
     *
     * @return Map from chord ID to the list of [ChordVariation] for that chord
     */
    suspend fun updateChordVariations(
        chordIds: List<CharSequence>,
        dataAccess: DataAccess,
        instrument: Instrument,
    ): Map<String, List<ChordVariation>> = withContext(Dispatchers.IO) {
        if (chordIds.isEmpty()) {
            return@withContext emptyMap()
        }

        val (instrumentPath, tuningPath) = when (instrument) {
            Instrument.Guitar -> "guitar" to "E A D G B E"
            Instrument.Ukulele -> "ukulele" to "G C E A"
        }

        val chordVariationsRef = db.collection("chordVariations")
            .document(instrumentPath)
            .collection(tuningPath)
            .whereIn("chord", chordIds)

        val allChords = chordVariationsRef.get().await()

        val resultMap = mutableMapOf<String, List<ChordVariation>>()

        for (documentSnapshot in allChords) {
            try {
                // Assuming the Firestore document can be directly deserialized into a list of ChordVariation objects.
                // You might need a data class that matches the Firestore structure if it's more complex.
                val chordDoc = documentSnapshot.toObject<TabRequestType.ChordInfo>()
                resultMap[chordDoc.chord] = chordDoc.variations.map { variation -> variation.toChordVariation(chordDoc.chord, instrument) }
                dataAccess.insertAll(resultMap[chordDoc.chord]!!) // Cache the results in the local DB
            } catch (ex: Exception) {
                // Log the exception and continue to the next chord
                Log.e(TAG, "Failed to fetch chord variation for '${documentSnapshot.id}'.", ex)
            }
        }

        if (resultMap.isEmpty() && chordIds.isNotEmpty()) {
            val message = "Couldn't fetch any of the requested chords: $chordIds"
            Log.w(TAG, message)
        }

        return@withContext resultMap
    }

    /**
     * Add today's most popular tabs to the database
     */
    suspend fun fetchTopTabs(dataAccess: DataAccess) = withContext(Dispatchers.IO) {
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
            throw NotFoundException("Top tabs result was empty: ${topTabSearchResults.size} results")
        }

        // clear top tabs playlist, then add all these to the top tabs playlist
        dataAccess.clearTopTabsPlaylist()
        for (tab in topTabs) {
            // add playlist entry
            dataAccess.appendToPlaylist(
                playlistId = TOP_TABS_PLAYLIST_ID,
                tabId = tab.tabId,
                transpose = 0
            )

            // add empty tab so it'll show up in the Popular list
            dataAccess.insert(tab)
        }
        return@withContext
    }

    suspend fun fetchTabsFromInternet(
        tabIds: List<String>,
        dataAccess: DataAccess,
    ): List<TabDataType> = withContext(Dispatchers.IO) {
        // get the tab and put it in the database, then return true
        Log.v(TAG, "Loading tabs ${tabIds.joinToString()}.")

        val tabCollection = db.collection("tabs")
        val tabQuery = tabCollection
            .whereIn("status", listOf("approved", "pending")) // must filter so firestore rules know that this is a valid query
            .whereIn("id", tabIds)
        val tabDocs = tabQuery.get().await()
        if (tabDocs.isEmpty) {
            throw NotFoundException("Tabs (${tabIds.joinToString()}) not found in database")
        }
        try {
            val requestResponse = tabDocs.toObjects<TabRequestType>()
            Log.v(TAG, "Parsed response for tabs ${requestResponse.joinToString { tab -> "${tab.id} (${tab.song_name})" }}")

            val result = requestResponse.map { requestType -> requestType.getTabFull() }
            dataAccess.upsertAll(result)
            return@withContext result
        } catch (ex: Exception) {
            throw TabFetchException("Error parsing tabs from firestore", ex)
        }
    }


    /**
     * Gets tab based on tabId.  Loads tab from internet and caches the result automatically in the
     * app database.
     *
     * @param tabId         The ID of the tab to load
     * @param dataAccess      The database instance to load a tab from (or into)
     */
    suspend fun fetchTabFromInternet(
        tabId: String,
        dataAccess: DataAccess,
    ): TabDataType {
        return fetchTabsFromInternet(listOf(tabId), dataAccess).first()
    }

    /**
     * Fetch all tabs from Firestore with the passed songId and insert them into the database. This is useful for search results to ensure
     * that user-generated tabs are included in the song version list.
     *
     * @param songId The ID of the song to fetch tabs for
     * @param dataAccess The database to insert the tabs into
     */
    suspend fun fetchAllTabsBySongId(songId: String, dataAccess: DataAccess) = withContext(Dispatchers.IO) {
        val tabCollectionRef = db.collection("tabs")
        val query = tabCollectionRef
                        .whereEqualTo("status", "approved")
                        .whereEqualTo("song_id", songId)
                        .whereEqualTo("type", "Chords") //todo: support Tabs, Ukulele Chords, etc
                        .limit(150)

        try {
            val querySnapshot = query.get().await()
            if (querySnapshot.isEmpty) {
                Log.i(TAG, "No tabs found for songId: $songId")
                return@withContext // No tabs found, so we're done.
            }

            for (document in querySnapshot.documents) {
                try {
                    val tabRequest = document.toObject<TabRequestType>()
                    val tab = tabRequest?.getTabFull()
                    if (tab?.content?.isBlank() == false) {
                        dataAccess.upsert(tab)
                    } else {
                        Log.e(TAG, "Fetched tab ${document.id} for song $songId couldn't be parsed, or has blank content. Skipping.")
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error processing a tab document (${document.id}) for songId $songId", ex)
                    // Continue processing other tabs
                }
            }
        } catch (ex: Exception) {
            throw TabFetchException("Error fetching tabs for songId $songId from Firestore.", ex)
        }
    }

    /**
     * Fetch all tabs from Firestore with the passed song name and insert them into the database.
     */
    suspend fun fetchAllTabsBySongName(songName: String, dataAccess: DataAccess, allowPending: Boolean = false) = withContext(Dispatchers.IO) {
        val tabCollectionRef = db.collection("tabs")
        val allowedStatuses = if (allowPending) listOf("approved", "pending") else listOf("approved")
        val query = tabCollectionRef
            .whereIn("status", allowedStatuses)
            .whereEqualTo("song_name", songName)
            .limit(20)

        try {
            val querySnapshot = query.get().await()
            if (querySnapshot.isEmpty) {
                Log.d(TAG, "No tabs found for song: $songName")
                return@withContext // No tabs found, so we're done.
            }

            for (document in querySnapshot.documents) {
                try {
                    val tabRequest = document.toObject<TabRequestType>()
                    val tab = tabRequest?.getTabFull()
                    if (tab?.content?.isBlank() == false) {
                        dataAccess.upsert(tab)
                    } else {
                        Log.e(TAG, "Fetched tab ${document.id} for song $songName couldn't be parsed, or has blank content. Skipping.")
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error processing a tab document (${document.id}) for songId $songName", ex)
                    // Continue processing other tabs
                }
            }
        } catch (ex: Exception) {
            throw TabFetchException("Error fetching tabs for songName $songName from Firestore.", ex)
        }
    }


    //#endregion

    //#region private methods

    /**
     * Gets an authenticated input stream for the passed API URL, updating the API key if needed
     *
     * @param url: The UG API url to start an authenticated InputStream with
     *
     * @return An [InputStream], authenticated with a valid API key
     *
     * @throws NoInternetException if no internet access
     * @throws Exception if an unknown error occurs (could still be an internet access issue)
     */
    private suspend fun authenticatedStream(url: String): InputStream = withContext(Dispatchers.IO) {
        Log.v(TAG, "Getting authenticated stream for url: $url.")
        try {
            apiKeyFetchLock.lock()

            if (apiKey == null) {
                updateApiKey()
            }
        } catch (ex: NoInternetException) {
            throw NoInternetException("Can't fetch $url. No internet access.", ex)
        } catch (ex: Exception) {
            throw Exception("Unexpected API Key initialization failure while fetching $url! Maybe an internet issue?", ex)
        } finally {
            apiKeyFetchLock.unlock()
        }

        // api key is not null
        Log.v(TAG, "Api key: $apiKey, device id: $deviceId.")

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
                Log.v(TAG, "Retrieved URL with response code $responseCode.")

                if (responseCode == 498 && numTries == 1) {  // don't bother the second time through
                    Log.i(
                        TAG,
                        "498 response code for old api key $apiKey and device id $deviceId.  Refreshing api key"
                    )
                    conn.disconnect()

                    try {
                        updateApiKey()
                        Log.v(TAG, "Got new api key ($apiKey)")
                    } catch (ex: Exception) {
                        // we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.
                        val msg =
                            "498 response code, but api key update returned null! Generally this means we don't have an internet connection.  Strange, because we shouldn't have gotten a 498 error code if we had no internet.  Either precisely perfect timing or something's wrong."
                        throw Exception(msg, ex)
                    }
                } else {
                    Log.v(TAG, "Fetch attempt $numTries - valid token or max retries reached.")
                    Log.v(TAG, "Response code $responseCode on try $numTries for url $url (${conn.requestMethod}).")

                    if (responseCode == 498) {
                        // read response content if our api level includes the function
                        var content = ""
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                content = conn.inputStream.readAllBytes().toString()
                            } catch (_: Exception) { }
                        }
                        throw AuthenticatorException("Couldn't fetch authenticated stream (498: bad token).  Response code: $responseCode, content: \n$content")
                    } else if (responseCode == 451) {
                        // read response content if our api level includes the function
                        var content = ""
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                content = conn.inputStream.readAllBytes().toString()
                            } catch (_: Exception) { }
                        }

                        Log.i(TAG, "Url not available (451: unavailable for legal reasons). content: \n$content")
                        throw UnavailableForLegalReasonsException("Url '$url' unavailable for legal reasons: 451.\n$content")
                    } else {
                        return@withContext conn.inputStream
                    }
                }
            } while (true)

            throw Exception("Unreachable: Could not create authenticated stream.")  // shouldn't get here
        } catch (ex: UnavailableForLegalReasonsException) {
            throw ex  // pass through UnavailableForLegalReasonsExceptions
        }
        catch (ex: FileNotFoundException) {
            throw NotFoundException("NOT FOUND during fetch of url $url. Response code $responseCode.", ex)
        } catch (ex: ConnectException) {
            throw NoInternetException("Could not fetch $url. ConnectException (no internet access)", ex)
        } catch (ex: NoInternetException) {
            throw NoInternetException("Could not fetch $url. No internet.", ex)
        } catch (ex: SocketTimeoutException) {
            throw NoInternetException("Could not fetch $url. Socket timeout (no internet access).", ex)
        } catch (ex: IOException) {
            throw NoInternetException("Could not fetch $url. IOException (no internet access). ${ex.message}", ex)
        } catch (ex: Exception) {
            throw Exception("Unexpected exception during fetch of url $url with parameters apiKey: " +
                    "$apiKey and deviceId: $deviceId.  Response code $responseCode", ex)
        }
    }

    /**
     * Sets an updated [apiKey], based on the most recent server time.  This needs to be called
     * whenever we get a 498 response code
     *
     * @throws NoInternetException if no internet access
     * @throws Exception if api key could not be updated for an unknown reason
     */
    private suspend fun updateApiKey() {
        apiKey = null
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd:H", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val stringBuilder = StringBuilder(deviceId)

        val serverTime = fetchServerTime()
        stringBuilder.append(serverTime)
        stringBuilder.append("createLog()")
        apiKey = getMd5(stringBuilder.toString())

        if (apiKey.isNullOrBlank()) {
            throw Exception("API key update completed without fetching API key.  Server time: $serverTime.  API key: $apiKey")
        }
    }

    /**
     * Gets the current server time, for use in API calls
     *
     * @return The current time according to the server
     *
     * @throws NoInternetException if not connected to the internet
     * @throws Exception if time fetch could not be completed for an unknown reason
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
        } catch (ex: IllegalStateException) {
            throw IllegalStateException("Error converting types while performing hello handshake. Check proguard rules.", ex)
        } catch (ex: UnknownHostException) {
            throw NoInternetException("Unknown host while performing hello handshake. Probably not connected to the internet.", ex)
        } catch (ex: Exception) {
            throw Exception( "Unexpected error getting hello handshake (server time). We may not be connected to the internet.", ex)
        }

        // read server time into our date type of choice
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd:H", Locale.US)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val formattedDateString = simpleDateFormat.format(serverTimestamp.getServerTime().time)

        Log.i(TAG, "Fetched server time: $formattedDateString}")
        return@withContext formattedDateString
    }

    /**
     * Hash a string using the MD5 algorithm
     *
     * @param [stringToHash]: The string to hash using the MD5 algorithm
     *
     * @return The MD5-hashed version of [stringToHash]
     *
     * @throws NoSuchAlgorithmException if the MD5 algorithm doesn't exist on this device
     */
    private fun getMd5(stringToHash: String): String {
        var ret = stringToHash

        ret = BigInteger(1, MessageDigest.getInstance("MD5").digest(ret.toByteArray())).toString(16)
        while (ret.length < 32) {
            val stringBuilder = java.lang.StringBuilder()
            stringBuilder.append("0")
            stringBuilder.append(ret)
            ret = stringBuilder.toString()
        }
        return ret
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

    //#endregion

    //#region Custom exceptions

    class NoInternetException : Exception {
        constructor() : super()
        constructor(message: String) : super(message)
        constructor(message: String, cause: Throwable) : super(message, cause)
        constructor(cause: Throwable) : super(cause)
    }

    class SearchException : Exception {
        constructor(message: String, cause: Throwable) : super(message, cause)
    }

    open class TabFetchException : Exception {
        constructor(message: String) : super(message)
        constructor(message: String, cause: Throwable) : super(message, cause)
    }

    class UnavailableForLegalReasonsException : NotFoundException {
        constructor(message: String) : super(message)
    }

    //#endregion
}

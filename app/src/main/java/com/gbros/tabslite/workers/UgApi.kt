package com.gbros.tabslite.workers

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.gbros.tabslite.data.*
import com.gbros.tabslite.utilities.ApiHelper
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import java.io.FileNotFoundException
import java.lang.IllegalStateException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class UgApi(
        val context: Context,
        val ugClientId: String = ApiHelper.getDeviceId(),
        val ugApiKey: String = "c16dc56e71da51deeb676b36aa321316"
) {
    private val gson = Gson()

    var lastSuggestionRequest = ""
    var lastResult = SearchSuggestionType(emptyList())
    suspend fun searchSuggest(q: String): List<String> {
        var query = q
        if(query.length > 5) { // ug api only allows a max of 5 chars for search suggestion requests.  rest of processing is done in app
            query = query.slice(0 until 5)
        }

        if(query == lastSuggestionRequest) {
            //processing past 5 chars is done in app
            return lastResult.suggestions.filter { s -> s.contains(q) }
        }


        coroutineScope {
            try {
                val connection = URL("https://api.ultimate-guitar.com/api/v1/tab/suggestion?q=$query").openConnection() as HttpURLConnection
                var inputStream = connection.inputStream
                val jsonReader = JsonReader(inputStream.reader())
                val searchSuggestionTypeToken = object : TypeToken<SearchSuggestionType>() {}.type
                lastResult = gson.fromJson(jsonReader, searchSuggestionTypeToken)
                inputStream.close()
            } catch (ex: FileNotFoundException) {
                // no suggestions for this query
                lastResult = SearchSuggestionType(emptyList())
            }
        }

        // suggestion caching
        lastSuggestionRequest = query

        // processing past 5 chars is done in app
        return lastResult.suggestions.filter { s -> s.contains(q) }
    }

    suspend fun search(q: String, pageNum: Int = 1): SearchRequestType = coroutineScope {
        try {
            val apiKey = ApiHelper.apiKey
            val deviceId = ApiHelper.getDeviceId()

            // type[]=300 means just chords (all instruments? use 300, 400, 700, and 800)
            var conn = URL("https://api.ultimate-guitar.com/api/v1/tab/search?title=$q&page=$pageNum&type[]=300&official[]=0").openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept-Charset", "utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
            conn.setRequestProperty("x-ug-client-id", deviceId)                   // stays constant over time; api key and client id are related to each other.
            conn.setRequestProperty("x-ug-api-key", apiKey)     // updates periodically.

            // handle when the api key is outdated
            if (conn.responseCode == 498) {
                conn.disconnect()
                ApiHelper.updateApiKey()
                conn = URL("https://api.ultimate-guitar.com/api/v1/tab/search?title=$q&page=$pageNum&type[]=300&official[]=0").openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
                conn.setRequestProperty("x-ug-client-id", deviceId)                   // stays constant over time; api key and client id are related to each other.
                conn.setRequestProperty("x-ug-api-key", apiKey)     // updates periodically.
                conn.setRequestProperty("Accept-Encoding", "identity")
            }

            val inputStream = conn.getInputStream()
            val jsonReader = JsonReader(inputStream.reader())
            var result = SearchRequestType()

            try {
                val searchResultTypeToken = object : TypeToken<SearchRequestType>() {}.type
                result = gson.fromJson(jsonReader, searchResultTypeToken)
            } catch (ex: JsonSyntaxException) {
                Log.v(javaClass.simpleName, "Search exception.  Probably just a 'did you mean' search suggestion at the end.", ex)
                try {
                    val stringTypeToken = object : TypeToken<String>() {}.type
                    val suggestedSearch : String = gson.fromJson(jsonReader, stringTypeToken)

                    this.cancel("search:$suggestedSearch")
                } catch (ex: IllegalStateException) {
                    inputStream.close()
                    Log.e(javaClass.simpleName, "Search illegal state exception!  Check SearchRequestType for consistency with data.", ex)
                    this.cancel("Illegal State.", ex)
                    throw ex
                }
            }

            inputStream.close()

            result
        } catch (ex: FileNotFoundException) {
            Log.v(javaClass.simpleName, "404 returned on search (End of results). '$q'.", ex)
            this.cancel("End of results (404 returned).", ex)
            SearchRequestType()
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "Error searching for '$q'.", ex)
            this.cancel("Error while searching.", ex)
            SearchRequestType()
        }
    }

    suspend fun updateChordVariations(chordIds: List<String>, force: Boolean = false, tuning: String = "E A D G B E",
                                   instrument: String = "guitar") = coroutineScope {
        val database = AppDatabase.getInstance(context).chordVariationDao()
        var chordParam = ""
        for (chord in chordIds) {
            if (force || !database.chordExists(chord)){ // if the chord already exists in the db at all, we can assume we have all variations of it.  Not often a new chord is created
                val uChord = URLEncoder.encode(chord, "utf-8")
                chordParam += "&chords[]=$uChord"
            }
        }

        if (chordParam.isNotEmpty()) {
            val apiKey = ApiHelper.apiKey
            val deviceId = ApiHelper.getDeviceId()

            val uTuning = URLEncoder.encode(tuning, "utf-8")
            val uInstrument = URLEncoder.encode(instrument, "utf-8")

            var conn = URL("https://api.ultimate-guitar.com/api/v1/tab/applicature?instrument=$uInstrument&tuning=$uTuning$chordParam").openConnection() as HttpURLConnection
            conn.setRequestProperty("Accept-Charset", "utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
            conn.setRequestProperty("x-ug-client-id", deviceId)             // stays constant over time; api key and client id are related to each other.
            conn.setRequestProperty("x-ug-api-key", apiKey)                 // updates periodically.

            // handle when the api key is outdated
            if(conn.responseCode == 498) {
                conn.disconnect()
                ApiHelper.updateApiKey()
                conn = URL("https://api.ultimate-guitar.com/api/v1/tab/applicature?instrument=$uInstrument&tuning=$uTuning$chordParam").openConnection() as HttpURLConnection
                conn.setRequestProperty("Accept-Charset", "utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("User-Agent", "UGT_ANDROID/5.10.11 (")  // actual value UGT_ANDROID/5.10.11 (ONEPLUS A3000; Android 10)
                conn.setRequestProperty("x-ug-client-id", deviceId)             // stays constant over time; api key and client id are related to each other.
                conn.setRequestProperty("x-ug-api-key", apiKey)                 // updates periodically.
            }


            val inputStream = conn.getInputStream()
            val jsonReader = JsonReader(inputStream.reader())
            val chordRequestTypeToken = object : TypeToken<List<TabRequestType.ChordInfo>>() {}.type
            val results: List<TabRequestType.ChordInfo> = gson.fromJson(jsonReader, chordRequestTypeToken)
            for(result in results) {
                database.insertAll(result.getChordVariations())
            }
            inputStream.close()
        }
    }

    suspend fun getChordVariations(chordId: String): List<ChordVariation> = coroutineScope {
        val database = AppDatabase.getInstance(context).chordVariationDao()
        database.getChordVariations(chordId)
    }
}
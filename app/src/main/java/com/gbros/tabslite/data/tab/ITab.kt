package com.gbros.tabslite.data.tab

import android.util.Log
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.chord.Chord
import com.gbros.tabslite.utilities.UgApi

private const val LOG_NAME = "tabslite.ITab          "

interface ITab {
    companion object {
        /**
         * Ensures that the full tab (not just the partial tab loaded in the search results) is stored
         * in the local database.  Checks if [content] is empty, and if so triggers an API call to download
         * the tab content from the internet and load it into the database.
         *
         * @param tabId: The tab ID of the tab to load from the API / database
         * @param database: The database to load the updated tab into
         * @param force: If true, load from the internet regardless of whether we already have the tab.  If false return the local copy from the database
         *
         * @return The resulting tab, either from the local database or from the internet
         */
        suspend fun fetchFullTab(tabId: Int, database: AppDatabase, force: Boolean = false): Tab {
            return if (force || !database.tabFullDao().existsWithContent(tabId)) {
                Log.d(LOG_NAME, "Fetching tab $tabId from internet (force = $force)")
                Tab(UgApi.fetchTabFromInternet(tabId = tabId, database = database))
            } else {
                // Cache hit for tab.  Not fetching from internet.
                Tab(database.tabFullDao().getTabInstance(tabId))
            }
        }
    }

    val tabId: Int
    val type: String
    val part: String
    val version: Int
    val votes: Int
    val rating: Double
    val date: Int
    val status: String
    val presetId: Int
    val tabAccessType: String
    val tpVersion: Int
    var tonalityName: String
    val versionDescription: String

    val songId: Int
    val songName: String
    val artistName: String
    val isVerified: Boolean
    val numVersions: Int

    // in JSON these are in a separate sublevel "recording"
    val recordingIsAcoustic: Boolean
    val recordingTonalityName: String
    val recordingPerformance: String
    val recordingArtists: ArrayList<String>

    var recommended: ArrayList<String>
    var userRating: Int
    var difficulty: String
    var tuning: String
    var capo: Int
    var urlWeb: String
    var strumming: ArrayList<String>
    var videosCount: Int
    var proBrother: Int
    var contributorUserId: Int
    var contributorUserName: String
    var content: String

    val transpose: Int

    /**
     * Get the human-readable capo number (ordinal numbers, i.e. 2nd Fret)
     */
    fun getCapoText(): String {
        return when {
            capo == 0 -> "None"
            capo in 11..13 -> "${capo}th Fret" // 11th, 12th, 13th are exceptions
            capo % 10 == 1 -> "${capo}st Fret"
            capo % 10 == 2 -> "${capo}nd Fret"
            capo % 10 == 3 -> "${capo}rd Fret"
            else -> "${capo}th Fret"
        }
    }

    /**
     * Get the TabsLite URL for this tab.  This is the URL that the app accepts as an intent to bring
     * the user directly to this tab.
     */
    fun getUrl(): String {
        // only allowed chars are alphanumeric and dash.
        return "https://tabslite.com/tab/$tabId"
    }

    /**
     * Transpose entire tab by the given number of half steps.  Updates [content] and [tonalityName]
     * but does not save changes to database.
     *
     * @param halfSteps: The number of half steps to transpose this tab
     */
    fun transpose(halfSteps: Int) {
        tonalityName = Chord.transposeChord(tonalityName, halfSteps)
        val chordPattern = Regex("\\[ch](.*?)\\[/ch]")
        val transposedContent = chordPattern.replace(this.content) {
            val chord = it.groupValues[1]
            "[ch]" + Chord.transposeChord(chord, halfSteps) + "[/ch]"
        }

        content = transposedContent
    }

    /**
     * Ensures that the full tab (not just the partial tab loaded in the search results) is stored
     * in the local database.  Checks if [content] is empty, and if so triggers an API call to download
     * the tab content from the internet and load it into the database.
     *
     * @param database: The database to load the updated tab into
     * @param force: If true, load from the internet regardless of whether we already have the tab.  If false, load only if [content] is empty
     *
     * @return The resulting tab, either from the local database or from the internet
     */
    suspend fun fetchFullTab(database: AppDatabase, force: Boolean = false) {
        // fetch new tab
        val newTab = Companion.fetchFullTab(tabId, database, force || content.isBlank())

        // update this tab
        tonalityName = newTab.tonalityName
        content = newTab.content

        recommended = newTab.recommended
        userRating = newTab.userRating
        difficulty = newTab.difficulty
        tuning = newTab.tuning
        capo = newTab.capo
        urlWeb = newTab.urlWeb
        strumming = newTab.strumming
        videosCount = newTab.videosCount
        proBrother = newTab.proBrother
        contributorUserId = newTab.contributorUserId
        contributorUserName = newTab.contributorUserName
    }
}
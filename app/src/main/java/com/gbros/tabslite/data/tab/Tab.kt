package com.gbros.tabslite.data.tab

import android.content.res.Resources.NotFoundException
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.UgApi

data class Tab(
    @PrimaryKey @ColumnInfo(name = "id") override var tabId: Int,
    @ColumnInfo(name = "song_id") override var songId: Int = -1,
    @ColumnInfo(name = "song_name") override var songName: String = "",
    @ColumnInfo(name = "artist_name") override var artistName: String = "",
    @ColumnInfo(name = "artist_id") override val artistId: Int = -1,
    @ColumnInfo(name = "type") override var type: String = "",
    @ColumnInfo(name = "part") override var part: String = "",
    @ColumnInfo(name = "version") override var version: Int = 0,
    @ColumnInfo(name = "votes") override var votes: Int = 0,
    @ColumnInfo(name = "rating") override var rating: Double = 0.0,
    @ColumnInfo(name = "date") override var date: Int = 0,
    @ColumnInfo(name = "status") override var status: String = "",
    @ColumnInfo(name = "preset_id") override var presetId: Int = 0,
    @ColumnInfo(name = "tab_access_type") override var tabAccessType: String = "public",
    @ColumnInfo(name = "tp_version") override var tpVersion: Int = 0,
    @ColumnInfo(name = "tonality_name") override var tonalityName: String = "",
    @ColumnInfo(name = "version_description") override var versionDescription: String = "",
    @ColumnInfo(name = "verified") override var isVerified: Boolean = false,

    @ColumnInfo(name = "recording_is_acoustic") override var recordingIsAcoustic: Boolean = false,
    @ColumnInfo(name = "recording_tonality_name") override var recordingTonalityName: String = "",
    @ColumnInfo(name = "recording_performance") override var recordingPerformance: String = "",
    @ColumnInfo(name = "recording_artists") override var recordingArtists: ArrayList<String> = ArrayList(),

    @ColumnInfo(name = "num_versions") override var numVersions: Int = 1,

    @ColumnInfo(name = "recommended") override var recommended: ArrayList<String> = ArrayList(0),
    @ColumnInfo(name = "user_rating") override var userRating: Int = 0,
    @ColumnInfo(name = "difficulty") override var difficulty: String = "novice",
    @ColumnInfo(name = "tuning") override var tuning: String = "E A D G B E",
    @ColumnInfo(name = "capo") override var capo: Int = 0,
    @ColumnInfo(name = "url_web") override var urlWeb: String = "",
    @ColumnInfo(name = "strumming") override var strumming: ArrayList<String> = ArrayList(),
    @ColumnInfo(name = "videos_count") override var videosCount: Int = 0,
    @ColumnInfo(name = "pro_brother") override var proBrother: Int = 0,
    @ColumnInfo(name = "contributor_user_id") override var contributorUserId: Int = -1,
    @ColumnInfo(name = "contributor_user_name") override var contributorUserName: String = "",
    @ColumnInfo(name = "content") override var content: String = "",
    @ColumnInfo(name = "transpose") override var transpose: Int? = null
): ITab {
    //#region "static" functions
    companion object {
        fun fromTabDataType(dataTabs: List<TabDataType>): List<Tab> {
            return dataTabs.map { Tab(it) }
        }

        suspend fun fetchAllEmptyPlaylistTabsFromInternet(dataAccess: DataAccess, playlistId: Int? = null, onProgressChange: (progress: Float) -> Unit = {}) {
            val emptyTabs: List<Int> = if (playlistId == null) dataAccess.getEmptyPlaylistTabIds() else dataAccess.getEmptyPlaylistTabIds(playlistId)
            Log.d(TAG, "Found ${emptyTabs.size} empty playlist tabs (filter by playlist id: $playlistId) to fetch")
            var numFetchedTabs = 0f
            emptyTabs.forEach { tabId ->
                try {
                    onProgressChange(++numFetchedTabs / emptyTabs.size.toFloat())
                    UgApi.fetchTabFromInternet(tabId, dataAccess)
                } catch (ex: UgApi.NoInternetException) {
                    Log.i(TAG, "Not connected to the internet during empty tab fetch for tab $tabId for playlist $playlistId: ${ex.message}. Skipping the rest of the tabs in this playlist.")
                    throw ex  // exit the fetch if we're not connected to the internet
                } catch (ex: UgApi.UnavailableForLegalReasonsException) { // must be before catch for NotFoundException since this is a type of NotFoundException
                    Log.i(TAG, "Tab $tabId unavailable for legal reasons.")
                } catch (ex: NotFoundException) {
                    Log.e(TAG, "Tab NOT FOUND during fetch of empty tab $tabId for playlist $playlistId")
                } catch (ex: Exception) {
                    Log.w(TAG, "Fetch of empty tab $tabId for playlist $playlistId failed: ${ex.message}", ex)
                }
            }
            onProgressChange(1f)
            Log.i(TAG, "Done fetching ${emptyTabs.size} empty tabs")
        }
    }
    //#endregion

    //#region constructors

    constructor(tabId: Int? = 0) : this(tabId = tabId ?: 0, songId = 0, songName = "", artistName = "", artistId = 0, isVerified = false, numVersions = 0,
        type = "", part = "", version = 0, votes = 0, rating = 0.0, date = 0, status = "", presetId = 0, tabAccessType = "",
        tpVersion = 0, tonalityName = "", versionDescription = "", recordingIsAcoustic = false, recordingTonalityName = "",
        recordingPerformance = "", recordingArtists = arrayListOf(), recommended = arrayListOf(), userRating = 0, difficulty = "", tuning = "",
        capo = 0, urlWeb = "", strumming = arrayListOf(), videosCount = 0, proBrother = 0, contributorUserId = 0, contributorUserName = "",
        content = "")

    constructor(tabFromDatabase: TabDataType) : this(tabId = tabFromDatabase.tabId, songId = tabFromDatabase.songId, songName = tabFromDatabase.songName, artistName = tabFromDatabase.artistName, artistId = tabFromDatabase.artistId, isVerified = tabFromDatabase.isVerified, numVersions = tabFromDatabase.numVersions,
        type = tabFromDatabase.type, part = tabFromDatabase.part, version = tabFromDatabase.version, votes = tabFromDatabase.votes, rating = tabFromDatabase.rating, date = tabFromDatabase.date, status = tabFromDatabase.status, presetId = tabFromDatabase.presetId, tabAccessType = tabFromDatabase.tabAccessType,
        tpVersion = tabFromDatabase.tpVersion, tonalityName = tabFromDatabase.tonalityName, versionDescription = tabFromDatabase.versionDescription, recordingIsAcoustic = tabFromDatabase.recordingIsAcoustic, recordingTonalityName = tabFromDatabase.recordingTonalityName,
        recordingPerformance = tabFromDatabase.recordingPerformance, recordingArtists = tabFromDatabase.recordingArtists, recommended = tabFromDatabase.recommended, userRating = tabFromDatabase.userRating, difficulty = tabFromDatabase.difficulty, tuning = tabFromDatabase.tuning,
        capo = tabFromDatabase.capo, urlWeb = tabFromDatabase.urlWeb, strumming = tabFromDatabase.strumming, videosCount = tabFromDatabase.videosCount, proBrother = tabFromDatabase.proBrother, contributorUserId = tabFromDatabase.contributorUserId, contributorUserName = tabFromDatabase.contributorUserName,
        content = tabFromDatabase.content)

    //#endregion

    override fun toString() = "$songName by $artistName"

    /**
     * Ensures that the full tab (not just the partial tab loaded in the search results) is stored
     * in the local database.  Checks if [Tab.content] is empty, and if so triggers an API call to download
     * the tab content from the internet and load it into the database.
     *
     * @param dataAccess: The database to load the updated tab into (or fetch the already downloaded tab from)
     * @param forceInternetFetch: If true, load from the internet regardless of whether we already have the tab.  If false, load only if [content] is empty
     */
    override suspend fun load(dataAccess: DataAccess, forceInternetFetch: Boolean): Tab {
        val loadedTab = if (forceInternetFetch || !dataAccess.existsWithContent(tabId)) {
            Log.d(TAG, "Fetching tab $tabId from internet (force = $forceInternetFetch)")
            Tab(UgApi.fetchTabFromInternet(tabId = tabId, dataAccess = dataAccess))
        } else {
            // Cache hit for tab.  Not fetching from internet.
            Tab(dataAccess.getTabInstance(tabId))
        }

        // set our content to match the freshly loaded tab
        set(loadedTab)
        return this
    }

    //#region private functions

    /**
     * Set all variables of this tab to match the provided tab
     */
    private fun set(tab: Tab) {
        // tab metadata
        tabId = tab.tabId
        songId = tab.songId
        songName = tab.songName
        artistName = tab.artistName
        isVerified = tab.isVerified
        numVersions = tab.numVersions
        type = tab.type
        part = tab.part
        version = tab.version
        versionDescription = tab.versionDescription
        votes = tab.votes
        rating = tab.rating
        date = tab.date
        status = tab.status
        presetId = tab.presetId
        tabAccessType = tab.tabAccessType
        tpVersion = tab.tpVersion
        urlWeb = tab.urlWeb
        userRating = tab.userRating
        difficulty = tab.difficulty
        contributorUserId = tab.contributorUserId
        contributorUserName = tab.contributorUserName

        // tab play data
        tonalityName = tab.tonalityName
        tuning = tab.tuning
        capo = tab.capo
        content = tab.content
        strumming = tab.strumming

        // tab recording data
        recommended = tab.recommended
        recordingIsAcoustic = tab.recordingIsAcoustic
        recordingTonalityName = tab.recordingTonalityName
        recordingPerformance = tab.recordingPerformance
        recordingArtists = tab.recordingArtists
        videosCount = tab.videosCount
        proBrother = tab.proBrother
    }

    //#endregion
}
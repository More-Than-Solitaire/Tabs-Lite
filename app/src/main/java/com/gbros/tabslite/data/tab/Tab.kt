package com.gbros.tabslite.data.tab

import android.content.res.Resources.NotFoundException
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.BackendConnection

data class Tab(
    @PrimaryKey @ColumnInfo(name = "id") override var tabId: String,
    @ColumnInfo(name = "song_id") override var songId: String = "",
    @ColumnInfo(name = "song_name") override var songName: String = "",
    @ColumnInfo(name = "song_genre") override var songGenre: String = "",
    @ColumnInfo(name = "artist_name") override var artistName: String = "",
    @ColumnInfo(name = "artist_id") override val artistId: String = "",
    @ColumnInfo(name = "type") override var type: String = "",
    @ColumnInfo(name = "part") override var part: String = "",
    @ColumnInfo(name = "version") override var version: Int = 0,
    @ColumnInfo(name = "votes") override var votes: Int = 0,
    @ColumnInfo(name = "rating") override var rating: Double = 0.0,
    @ColumnInfo(name = "date") override var date: Int = 0,
    @ColumnInfo(name = "status") override var status: String = "",
    @ColumnInfo(name = "tab_access_type") override var tabAccessType: String = "public",
    @ColumnInfo(name = "tonality_name") override var tonalityName: String = "",
    @ColumnInfo(name = "version_description") override var versionDescription: String = "",
    @ColumnInfo(name = "verified") override var isVerified: Boolean = false,
    @ColumnInfo(name = "is_tab_ml") override var isTabMl: Boolean = false,

    @ColumnInfo(name = "versions_count") override var versionsCount: Int = 1,

    @ColumnInfo(name = "recommended") override var recommended: ArrayList<String> = ArrayList(0),
    @ColumnInfo(name = "difficulty") override var difficulty: String = "novice",
    @ColumnInfo(name = "tuning") override var tuning: String = "E A D G B E",
    @ColumnInfo(name = "capo") override var capo: Int = 0,
    @ColumnInfo(name = "contributor_user_id") override var contributorUserId: String = "0",
    @ColumnInfo(name = "contributor_user_name") override var contributorUserName: String = "Unregistered",
    @ColumnInfo(name = "content") override var content: String = "",
    @ColumnInfo(name = "transpose") override var transpose: Int? = null
): ITab {
    //#region "static" functions
    companion object {
        fun fromTabDataType(dataTabs: List<TabDataType>): List<Tab> {
            return dataTabs.map { Tab(it) }
        }

        suspend fun fetchAllEmptyPlaylistTabsFromInternet(dataAccess: DataAccess, playlistId: Int? = null, onProgressChange: (progress: Float) -> Unit = {}) {
            val emptyTabs: List<String> = if (playlistId == null) dataAccess.getEmptyPlaylistTabIds() else dataAccess.getEmptyPlaylistTabIds(playlistId)
            Log.d(TAG, "Found ${emptyTabs.size} empty playlist tabs (filter by playlist id: $playlistId) to fetch")
            var numFetchedTabs = 0f
            emptyTabs.forEach { tabId ->
                try {
                    onProgressChange(++numFetchedTabs / emptyTabs.size.toFloat())
                    BackendConnection.fetchTabFromInternet(tabId, dataAccess)
                } catch (ex: BackendConnection.NoInternetException) {
                    Log.i(TAG, "Not connected to the internet during empty tab fetch for tab $tabId for playlist $playlistId: ${ex.message}. Skipping the rest of the tabs in this playlist.")
                    throw ex  // exit the fetch if we're not connected to the internet
                } catch (ex: BackendConnection.UnavailableForLegalReasonsException) { // must be before catch for NotFoundException since this is a type of NotFoundException
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

    constructor(tabId: String = "") : this(tabId = tabId, songId = "", songName = "", songGenre = "", artistName = "", artistId = "", isVerified = false, versionsCount = 0,
        type = "", part = "", version = 0, votes = 0, rating = 0.0, date = 0, status = "", tabAccessType = "public",
        tonalityName = "", versionDescription = "", recommended = arrayListOf(), difficulty = "", tuning = "",
        capo = 0, contributorUserId = "0", contributorUserName = "Unregistered", content = "")

    constructor(tabFromDatabase: TabDataType) : this(tabId = tabFromDatabase.tabId, songId = tabFromDatabase.songId, songName = tabFromDatabase.songName, songGenre = tabFromDatabase.songGenre,
        artistName = tabFromDatabase.artistName, artistId = tabFromDatabase.artistId, isVerified = tabFromDatabase.isVerified, versionsCount = tabFromDatabase.versionsCount,
        type = tabFromDatabase.type, part = tabFromDatabase.part, version = tabFromDatabase.version, votes = tabFromDatabase.votes, rating = tabFromDatabase.rating, date = tabFromDatabase.date, status = tabFromDatabase.status, tabAccessType = tabFromDatabase.tabAccessType,
        tonalityName = tabFromDatabase.tonalityName, versionDescription = tabFromDatabase.versionDescription, recommended = tabFromDatabase.recommended, difficulty = tabFromDatabase.difficulty, tuning = tabFromDatabase.tuning,
        capo = tabFromDatabase.capo, contributorUserId = tabFromDatabase.contributorUserId, contributorUserName = tabFromDatabase.contributorUserName, content = tabFromDatabase.content)

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
            Tab(BackendConnection.fetchTabFromInternet(tabId = tabId, dataAccess = dataAccess))
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
        songGenre = tab.songGenre
        artistName = tab.artistName
        isVerified = tab.isVerified
        versionsCount = tab.versionsCount
        type = tab.type
        part = tab.part
        version = tab.version
        versionDescription = tab.versionDescription
        votes = tab.votes
        rating = tab.rating
        date = tab.date
        status = tab.status
        tabAccessType = tab.tabAccessType
        difficulty = tab.difficulty
        contributorUserId = tab.contributorUserId
        contributorUserName = tab.contributorUserName

        // tab play data
        tonalityName = tab.tonalityName
        tuning = tab.tuning
        capo = tab.capo
        content = tab.content

        recommended = tab.recommended
    }

    //#endregion
}
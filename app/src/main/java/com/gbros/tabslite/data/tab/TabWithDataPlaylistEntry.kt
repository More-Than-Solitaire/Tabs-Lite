package com.gbros.tabslite.data.tab

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.playlist.DataPlaylistEntry
import com.gbros.tabslite.data.playlist.IDataPlaylistEntry
import com.gbros.tabslite.data.playlist.Playlist
import kotlinx.parcelize.Parcelize

@Parcelize  // used for playlist reordering
data class TabWithDataPlaylistEntry(
    /**
     * The ID of the playlist entry that represents this tab/playlist combo
     */
    @ColumnInfo(name = "entry_id") override var entryId: Int,

    /**
     * The ID of the playlist that this tab/playlist combo belongs to
     */
    @ColumnInfo(name = "playlist_id") override var playlistId: Int = 0,

    /**
     * The ID of the tab in this tab/playlist combo
     */
    @ColumnInfo(name = "tab_id") override var tabId: Int = 0,

    /**
     * The next entry in this playlist (if one exists, else null)
     */
    @ColumnInfo(name = "next_entry_id") override var nextEntryId: Int? = null,

    /**
     * The previous entry in this playlist (if one exists, else null)
     */
    @ColumnInfo(name = "prev_entry_id") override var prevEntryId: Int? = null,
    @ColumnInfo(name = "date_added") override var dateAdded: Long = 0,
    @ColumnInfo(name = "song_id") override var songId: Int = 0,
    @ColumnInfo(name = "song_name") override var songName: String = "",
    @ColumnInfo(name = "artist_name") override var artistName: String = "",
    @ColumnInfo(name = "verified") override var isVerified: Boolean = false,
    @ColumnInfo(name = "num_versions") override var numVersions: Int = 0,
    @ColumnInfo(name = "type") override var type: String = "",
    @ColumnInfo(name = "part") override var part: String = "",
    @ColumnInfo(name = "version") override var version: Int = 0,
    @ColumnInfo(name = "votes") override var votes: Int = 0,
    @ColumnInfo(name = "rating") override var rating: Double = 0.0,
    @ColumnInfo(name = "date") override var date: Int = 0,
    @ColumnInfo(name = "status") override var status: String = "",
    @ColumnInfo(name = "preset_id") override var presetId: Int = 0,
    @ColumnInfo(name = "tab_access_type") override var tabAccessType: String = "",
    @ColumnInfo(name = "tp_version") override var tpVersion: Int = 0,
    @ColumnInfo(name = "tonality_name") override var tonalityName: String = "",
    @ColumnInfo(name = "version_description") override var versionDescription: String = "",
    @ColumnInfo(name = "recording_is_acoustic") override var recordingIsAcoustic: Boolean = false,
    @ColumnInfo(name = "recording_tonality_name") override var recordingTonalityName: String = "",
    @ColumnInfo(name = "recording_performance") override var recordingPerformance: String = "",
    @ColumnInfo(name = "recording_artists") override var recordingArtists: ArrayList<String> = arrayListOf(),

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

    // columns from Playlist
    @ColumnInfo(name = "user_created") var playlistUserCreated: Boolean? = true,
    @ColumnInfo(name = "title") var playlistTitle: String? = "",
    @ColumnInfo(name = "date_created") var playlistDateCreated: Long? = 0,
    @ColumnInfo(name = "date_modified") var playlistDateModified: Long? = 0,
    @ColumnInfo(name = "description") var playlistDescription: String? = "",
    @ColumnInfo(name = "transpose") override var transpose: Int = 0
) : ITab, IDataPlaylistEntry(tabId = tabId, transpose = 0, entryId = entryId, playlistId = playlistId, nextEntryId = nextEntryId, prevEntryId = prevEntryId, dateAdded = dateAdded), Parcelable {

    /**
     * Ensures that the full [TabWithDataPlaylistEntry] (not just the partial tab loaded in the search results) is stored
     * in the local database.  Checks if [content] is empty, and if so triggers an API call to download
     * the tab content from the internet and load it into the database.
     *
     * @param dataAccess: The database to load the updated tab into
     * @param forceInternetFetch: If true, load from the internet regardless of whether we already have the tab.  If false, load only if [content] is empty
     *
     * @return this object, for joining calls together
     */
    override suspend fun load(dataAccess: DataAccess, forceInternetFetch: Boolean): TabWithDataPlaylistEntry {
        // fetch playlist entry
        val loadedPlaylistEntry = dataAccess.getEntryById(entryId)
        if (loadedPlaylistEntry == null) {
            throw NoSuchElementException("Attempted to load a playlist entry that could not be found in the database.")
        } else {
            set(loadedPlaylistEntry)
        }

        // fetch playlist
        val loadedPlaylistDetail = dataAccess.getPlaylist(playlistId)
        set(loadedPlaylistDetail)

        // fetch tab
        val loadedTab = Tab(tabId).load(dataAccess, forceInternetFetch)
        set(loadedTab)
        return this
    }

    //#region private methods

    /**
     * Set all variables of this playlist entry to match the provided [playlistEntry]
     */
    private fun set(playlistEntry: DataPlaylistEntry) {
        playlistId = playlistEntry.playlistId
        entryId = playlistEntry.entryId
        nextEntryId = playlistEntry.nextEntryId
        prevEntryId = playlistEntry.prevEntryId
        tabId = playlistEntry.tabId
        dateAdded = playlistEntry.dateAdded
        transpose = playlistEntry.transpose
    }

    /**
     * Set all variables of this playlist to match the provided [playlistDetail]
     */
    private fun set(playlistDetail: Playlist) {
        playlistId = playlistDetail.playlistId
        playlistTitle = playlistDetail.title
        playlistDateCreated = playlistDetail.dateCreated
        playlistDateModified = playlistDetail.dateModified
        playlistDescription = playlistDetail.description
        playlistUserCreated = playlistDetail.userCreated
    }

    /**
     * Set all variables of this tab to match the provided [tab]
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
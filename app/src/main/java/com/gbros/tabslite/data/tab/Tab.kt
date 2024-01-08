package com.gbros.tabslite.data.tab

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class Tab(
    @PrimaryKey @ColumnInfo(name = "id") override var tabId: Int,
    @ColumnInfo(name = "song_id") override var songId: Int = -1,
    @ColumnInfo(name = "song_name") override var songName: String = "",
    @ColumnInfo(name = "artist_name") override var artistName: String = "",
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
): ITab {
    @ColumnInfo(name = "transpose") override var transpose: Int = 0
        private set;

    companion object {
        fun fromTabDataType(dataTabs: List<TabDataType>): List<Tab> {
            return dataTabs.map { Tab(it) }
        }
    }

    constructor(tabId: Int? = 0) : this(tabId = tabId ?: 0, songId = 0, songName = "", artistName = "", isVerified = false, numVersions = 0,
        type = "", part = "", version = 0, votes = 0, rating = 0.0, date = 0, status = "", presetId = 0, tabAccessType = "",
        tpVersion = 0, tonalityName = "", versionDescription = "", recordingIsAcoustic = false, recordingTonalityName = "",
        recordingPerformance = "", recordingArtists = arrayListOf(), recommended = arrayListOf(), userRating = 0, difficulty = "", tuning = "",
        capo = 0, urlWeb = "", strumming = arrayListOf(), videosCount = 0, proBrother = 0, contributorUserId = 0, contributorUserName = "",
        content = "")

    constructor(tabFromDatabase: TabDataType) : this(tabId = tabFromDatabase.tabId, songId = tabFromDatabase.songId, songName = tabFromDatabase.songName, artistName = tabFromDatabase.artistName, isVerified = tabFromDatabase.isVerified, numVersions = tabFromDatabase.numVersions,
        type = tabFromDatabase.type, part = tabFromDatabase.part, version = tabFromDatabase.version, votes = tabFromDatabase.votes, rating = tabFromDatabase.rating, date = tabFromDatabase.date, status = tabFromDatabase.status, presetId = tabFromDatabase.presetId, tabAccessType = tabFromDatabase.tabAccessType,
        tpVersion = tabFromDatabase.tpVersion, tonalityName = tabFromDatabase.tonalityName, versionDescription = tabFromDatabase.versionDescription, recordingIsAcoustic = tabFromDatabase.recordingIsAcoustic, recordingTonalityName = tabFromDatabase.recordingTonalityName,
        recordingPerformance = tabFromDatabase.recordingPerformance, recordingArtists = tabFromDatabase.recordingArtists, recommended = tabFromDatabase.recommended, userRating = tabFromDatabase.userRating, difficulty = tabFromDatabase.difficulty, tuning = tabFromDatabase.tuning,
        capo = tabFromDatabase.capo, urlWeb = tabFromDatabase.urlWeb, strumming = tabFromDatabase.strumming, videosCount = tabFromDatabase.videosCount, proBrother = tabFromDatabase.proBrother, contributorUserId = tabFromDatabase.contributorUserId, contributorUserName = tabFromDatabase.contributorUserName,
        content = tabFromDatabase.content)

    override fun transpose(halfSteps: Int) {
        super.transpose(halfSteps)
        transpose += halfSteps
    }

    override fun toString() = "$songName by $artistName"
}
package com.gbros.tabslite.data.tab

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// todo: implement bpm or switch entirely over to TabRequestType
@Entity(
        tableName = "tabs"
)

data class TabDataType(
    @PrimaryKey @ColumnInfo(name = "id") var tabId: Int,
    @ColumnInfo(name = "song_id") var songId: Int = -1,
    @ColumnInfo(name = "song_name") var songName: String = "",
    @ColumnInfo(name = "artist_name") var artistName: String = "",
    @ColumnInfo(name = "artist_id") var artistId: Int = -1,
    @ColumnInfo(name = "type") var type: String = "",
    @ColumnInfo(name = "part") var part: String = "",
    @ColumnInfo(name = "version") var version: Int = 0,
    @ColumnInfo(name = "votes") var votes: Int = 0,
    @ColumnInfo(name = "rating") var rating: Double = 0.0,
    @ColumnInfo(name = "date") var date: Int = 0,
    @ColumnInfo(name = "status") var status: String = "",
    @ColumnInfo(name = "preset_id") var presetId: Int = 0,
    @ColumnInfo(name = "tab_access_type") var tabAccessType: String = "public",
    @ColumnInfo(name = "tp_version") var tpVersion: Int = 0,
    @ColumnInfo(name = "tonality_name") var tonalityName: String = "",
    @ColumnInfo(name = "version_description") var versionDescription: String = "",
    @ColumnInfo(name = "verified") var isVerified: Boolean = false,

    @ColumnInfo(name = "recording_is_acoustic") var recordingIsAcoustic: Boolean = false,
    @ColumnInfo(name = "recording_tonality_name") var recordingTonalityName: String = "",
    @ColumnInfo(name = "recording_performance") var recordingPerformance: String = "",
    @ColumnInfo(name = "recording_artists") var recordingArtists: ArrayList<String> = ArrayList(),

    @ColumnInfo(name = "num_versions") var numVersions: Int = 1,

    @ColumnInfo(name = "recommended") var recommended: ArrayList<String> = ArrayList(0),
    @ColumnInfo(name = "user_rating") var userRating: Int = 0,
    @ColumnInfo(name = "difficulty") var difficulty: String = "novice",
    @ColumnInfo(name = "tuning") var tuning: String = "E A D G B E",
    @ColumnInfo(name = "capo") var capo: Int = 0,
    @ColumnInfo(name = "url_web") var urlWeb: String = "",
    @ColumnInfo(name = "strumming") var strumming: ArrayList<String> = ArrayList(),
    @ColumnInfo(name = "videos_count") var videosCount: Int = 0,
    @ColumnInfo(name = "pro_brother") var proBrother: Int = 0,
    @ColumnInfo(name = "contributor_user_id") var contributorUserId: Int = -1,
    @ColumnInfo(name = "contributor_user_name") var contributorUserName: String = "",
    @ColumnInfo(name = "content") var content: String = "",
    ) {
    override fun toString() = "$songName by $artistName"
}
package com.gbros.tabslite.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// todo: implement bpm or switch entirely over to TabRequestType
@Entity(
        tableName = "tabs"
)

@Parcelize
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

    @ColumnInfo(name = "favorite") var unused_fav: Int = 0,             // kept so we don't have to upgrade the database
    @ColumnInfo(name = "favorite_time") var unused_fav_time: Int? = 0,  // kept so we don't have to upgrade the database
    @ColumnInfo(name = "transposed") var unused_transposed: Int = 0     // kept so we don't have to upgrade the database
) : IntTabFull {

    override fun toString() = "$songName by $artistName"
}
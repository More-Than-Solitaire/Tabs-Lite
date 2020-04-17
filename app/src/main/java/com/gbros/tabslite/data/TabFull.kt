package com.gbros.tabslite.data

import androidx.annotation.Nullable
import androidx.room.*
import java.util.*
import kotlin.collections.ArrayList

// todo: implement bpm or switch entirely over to TabRequestType
@Entity(
        tableName = "tabs"
)
data class TabFull(
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
        @ColumnInfo(name = "favorite") var favorite: Boolean = false,

        @ColumnInfo(name = "transposed") var transposed: Int = 0,

        @ColumnInfo(name = "favorite_time") @Nullable override var favoriteTime: Long? = null

) : IntTabBasic {
    override fun toString() = "$songName by $artistName"

    @Ignore private var tb : TabBasic = TabBasic(songId = songId, songName = songName,
            artistName = artistName, isVerified = isVerified, tabId = tabId, type = type,
            version = version )

    fun getTabBasic() : TabBasic {
        return tb
    }

    fun getCapoText(): String {
        return when (capo) {
            0 -> "None"
            1 -> "1st Fret"
            2 -> "2nd Fret"
            3 -> "3rd Fret"
            else -> capo.toString() + "th Fret"
        }
    }
}
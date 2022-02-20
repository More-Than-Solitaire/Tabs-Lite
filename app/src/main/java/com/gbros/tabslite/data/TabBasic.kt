package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class TabBasic(
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

        @ColumnInfo(name = "favorite_time") @Nullable override var favoriteTime: Long? = null
) : IntTabBasic, Parcelable {
    override fun toString() = "$songName by $artistName"
}
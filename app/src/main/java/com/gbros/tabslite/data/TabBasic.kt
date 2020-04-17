package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

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


/*
data class TabBasic : IntTabBasic{
    override var songId: Int = -1
        get() = field
        set(value) {field = value}
    override var songName: String = ""
        get() = field
        set(value) {field = value}

    override var artistName: String = ""
        get() = field
        set(value) {field = value}

    override var isVerified: Boolean = false
        get() = field
        set(value) {field = value}

    override var numVersions: Int = 1
        get() = field
        set(value) {field = value}




    override var tabId: Int = -1
        get() = field
        set(value) {field = value}

    override var type: String = ""
        get() = field
        set(value) {field = value}

    override var part: String = ""
        get() = field
        set(value) {field = value}

    override var version: Int = 0
        get() = field
        set(value) {field = value}

    override var votes: Int = 0
        get() = field
        set(value) {field = value}

    override var rating: Double = 0.0
        get() = field
        set(value) {field = value}

    override var date: Int = 0
        get() = field
        set(value) {field = value}

    override var status: String = ""
        get() = field
        set(value) {field = value}

    override var presetId: Int = 0
        get() = field
        set(value) {field = value}

    override var tabAccessType: String = ""
        get() = field
        set(value) {field = value}

    override var tpVersion: Int = 0
        get() = field
        set(value) {field = value}

    override var tonalityName: String = ""
        get() = field
        set(value) {field = value}

    override var versionDescription: String = ""
        get() = field
        set(value) {field = value}

    // in JSON these are in a separate sublevel "recording"
    override var recordingIsAcoustic: Boolean = false
        get() = field
        set(value) {field = value}

    override var recordingTonalityName: String = ""
        get() = field
        set(value) {field = value}

    override var recordingArtists: ArrayList<String> = ArrayList()
        get() = field
        set(value) {field = value}

    override var recordingPerformance: String = ""
        get() = field
        set(value) {field = value}

    override fun toString() = songName

    constructor(songId: Int = 0, songName: String = "", artistName: String = "",
                isVerified: Boolean = false, numVersions: Int = 1, tabId: Int = 0, type: String = "",
                part: String = "", version: Int = 0, votes: Int = 0, rating: Double = 0.0,
                date: Int = 0, status: String = "", presetId: Int = 0, tabAccessType: String = "public",
                tpVersion: Int = 0, tonalityName: String = "", versionDescription: String = "",
                recordingIsAcoustic: Boolean = false, recordingTonalityName: String = "",
                recordingArtists: ArrayList<String> = ArrayList(), recordingPerformance: String = ""){
        this.songId                 = songId
        this.songName               = songName
        this.artistName             = artistName
        this.isVerified             = isVerified
        this.numVersions            = numVersions
        this.tabId                  = tabId
        this.type                   = type
        this.part                   = part
        this.version                = version
        this.votes                  = votes
        this.rating                 = rating
        this.date                   = date
        this.status                 = status
        this.presetId               = presetId
        this.tabAccessType          = tabAccessType
        this.tpVersion              = tpVersion
        this.tonalityName           = tonalityName
        this.versionDescription     = versionDescription
        this.recordingIsAcoustic    = recordingIsAcoustic
        this.recordingTonalityName  = recordingTonalityName
        this.recordingArtists       = recordingArtists
        this.recordingPerformance   = recordingPerformance
    }

    /*
    constructor(songId: Int, songName: String, artistName: String, isVerified: Boolean,
                tabId: Int, type: String, version: Int) {
        this.songId                 = songId
        this.songName               = songName
        this.artistName             = artistName
        this.isVerified             = isVerified
        this.tabId                  = tabId
        this.type                   = type
        this.version                = version
    }
      */
}
*/


package com.gbros.tabslite.data.tab

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
        tableName = "tabs"
)

data class TabDataType(
    @PrimaryKey @ColumnInfo(name = "id") var tabId: String,
    @ColumnInfo(name = "song_id") var songId: String = "",
    @ColumnInfo(name = "song_name") var songName: String = "",
    @ColumnInfo(name = "song_genre") var songGenre: String = "",
    @ColumnInfo(name = "artist_name") var artistName: String = "",
    @ColumnInfo(name = "artist_id") var artistId: String = "",
    @ColumnInfo(name = "type") var type: String = "",
    @ColumnInfo(name = "part") var part: String = "",
    @ColumnInfo(name = "version") var version: Int = 0,
    @ColumnInfo(name = "votes") var votes: Int = 0,
    @ColumnInfo(name = "rating") var rating: Double = 0.0,
    @ColumnInfo(name = "date") var date: Int = 0,
    @ColumnInfo(name = "status") var status: String = "",
    @ColumnInfo(name = "tab_access_type") var tabAccessType: String = "public",
    @ColumnInfo(name = "tonality_name") var tonalityName: String = "",
    @ColumnInfo(name = "version_description") var versionDescription: String = "",
    @ColumnInfo(name = "verified") var isVerified: Boolean = false,
    @ColumnInfo(name = "is_tab_ml") var isTabMl: Boolean = false,

    @ColumnInfo(name = "versions_count") var versionsCount: Int = 1,

    @ColumnInfo(name = "recommended") var recommended: ArrayList<String> = ArrayList(0),
    @ColumnInfo(name = "difficulty") var difficulty: String = "novice",
    @ColumnInfo(name = "tuning") var tuning: String = "E A D G B E",
    @ColumnInfo(name = "capo") var capo: Int = 0,
    @ColumnInfo(name = "contributor_user_id") var contributorUserId: String = "0",
    @ColumnInfo(name = "contributor_user_name") var contributorUserName: String = "Unregistered",
    @ColumnInfo(name = "content") var content: String = "",
    ) {
    override fun toString() = "$songName by $artistName"
}
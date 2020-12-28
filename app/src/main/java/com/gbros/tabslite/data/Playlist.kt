package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize

import java.util.*

/**
 * [Playlist] represents any playlists the user may have on the device.
 */
@Entity(tableName = "playlist")

@Parcelize
data class Playlist(
        @PrimaryKey @ColumnInfo(name = "id") val playlistId: Int,
        @ColumnInfo(name = "user_created") val userCreated: Boolean,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "date_created") val dateCreated: Date,
        @ColumnInfo(name = "date_modified") val dateModified: Date,
        @ColumnInfo(name = "description") val description: String
) : Parcelable {
    override fun toString() = title
}
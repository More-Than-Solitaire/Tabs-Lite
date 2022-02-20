package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * [Playlist] represents any playlists the user may have on the device.  Playlist ID -1 is a special
 * playlist for favorite tabs.  That playlist doesn't have an entry here so that it doesn't show up
 * in the playlist tab, however entries are still found by ID in the playlist_entry database.
 */
@Entity(tableName = "playlist")

@Parcelize
data class Playlist(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val playlistId: Int,
        @ColumnInfo(name = "user_created") val userCreated: Boolean,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "date_created") val dateCreated: Long,
        @ColumnInfo(name = "date_modified") val dateModified: Long,
        @ColumnInfo(name = "description") val description: String
) : Parcelable {
    override fun toString() = title
}
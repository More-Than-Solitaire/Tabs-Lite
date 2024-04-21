package com.gbros.tabslite.data.playlist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * [Playlist] represents any playlists the user may have on the device.  Playlist ID -1 is a special
 * playlist for favorite tabs.  That playlist doesn't have an entry here so that it doesn't show up
 * in the playlist tab, however entries are still found by ID in the playlist_entry database.
 */
@Serializable
@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val playlistId: Int = 0,
    @ColumnInfo(name = "user_created") override val userCreated: Boolean,
    @ColumnInfo(name = "title") override val title: String,
    @ColumnInfo(name = "date_created") val dateCreated: Long,
    @ColumnInfo(name = "date_modified") val dateModified: Long,
    @ColumnInfo(name = "description") override val description: String
): IPlaylist {
    override fun toString() = title

    companion object {
        const val FAVORITES_PLAYLIST_ID = -1
        const val TOP_TABS_PLAYLIST_ID = -2
    }
}
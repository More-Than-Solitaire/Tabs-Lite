package com.gbros.tabslite.data.playlist

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gbros.tabslite.data.playlist.Playlist.Companion.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.data.playlist.Playlist.Companion.TOP_TABS_PLAYLIST_ID
import kotlinx.serialization.Serializable

/**
 * [Playlist] represents any playlists the user may have on the device.  Playlist ID -1
 * ([FAVORITES_PLAYLIST_ID]) and -2 ([TOP_TABS_PLAYLIST_ID]) are reserved special playlists
 * for favorite/popular tabs.  That playlist doesn't have an entry in the playlist table so that it
 * doesn't show up in the playlists view, however entries are still found by ID in the
 * playlist_entry database.
 */
@Serializable
@Entity(tableName = "playlist")
data class Playlist(
    /**
     * The identifier for this playlist
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val playlistId: Int = 0,

    /**
     * Whether this playlist was system generated (false, e.g. the Favorites playlist) or user created (true).
     */
    @ColumnInfo(name = "user_created") override val userCreated: Boolean,

    /**
     * The human-readable title of this playlist
     */
    @ColumnInfo(name = "title") override val title: String,

    /**
     * The date/time this playlist was created in milliseconds ([System.currentTimeMillis])
     */
    @ColumnInfo(name = "date_created") val dateCreated: Long,

    /**
     * The date/time this playlist was last modified in milliseconds ([System.currentTimeMillis]). Can be used for sorting
     */
    @ColumnInfo(name = "date_modified") val dateModified: Long,

    /**
     * The human-readable description of this playlist
     */
    @ColumnInfo(name = "description") override val description: String
): IPlaylist {
    override fun toString() = title

    companion object {
        /**
         * The reserved playlist ID for the Favorites system playlist
         */
        const val FAVORITES_PLAYLIST_ID = -1

        /**
         * The reserved playlist ID for the Popular/Top Tabs system playlist
         */
        const val TOP_TABS_PLAYLIST_ID = -2
    }
}
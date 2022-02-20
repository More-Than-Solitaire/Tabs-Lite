package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * [PlaylistEntry] represents a song in a playlist (or more than once in a playlist). Playlist ID -1
 * is a special playlist for Favorites.
 */
@Entity(
        tableName = "playlist_entry"
)

@Parcelize
data class PlaylistEntry(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val entryId: Int,
        @ColumnInfo(name = "playlist_id") val playlistId: Int,          // what playlist this entry is in
        @ColumnInfo(name = "tab_id") val tabId: Int,                    // which tab we added to the playlist (references TabFull tabId)
        @ColumnInfo(name = "next_entry_id") val nextEntryId: Int?,    // the id of the next entry in this playlist
        @ColumnInfo(name = "prev_entry_id") val prevEntryId: Int?,    // the id of the previous entry in this playlist
        @ColumnInfo(name = "date_added") val dateAdded: Long,           // when this entry was added to the playlist
        @ColumnInfo(name = "transpose") var transpose: Int              // each entry gets its own saved transpose number so changing the number from the favorites menu won't change every entry in every playlist.
) : Parcelable {
    constructor(playlistId: Int, tabId: Int, next_entry_id: Int?, prev_entry_id: Int?, dateAdded: Long, transpose: Int) : this(0, playlistId, tabId, next_entry_id, prev_entry_id, dateAdded, transpose)
}
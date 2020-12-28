package com.gbros.tabslite.data

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * [PlaylistEntry] represents a song in a playlist (or more than once in a playlist)
 */
@Entity(
        tableName = "playlist_entry"
)

@Parcelize
data class PlaylistEntry(
        @PrimaryKey @ColumnInfo(name = "id") val entryId: Int,
        @ColumnInfo(name = "playlist_id") val playlistId: Boolean,      // what playlist this entry is in
        @ColumnInfo(name = "tab_id") val tabId: Int,                    // which tab we added to the playlist (references TabFull tabId)
        @ColumnInfo(name = "ordering") val ordering: Int,               // an integer value to control playlist order
        @ColumnInfo(name = "date_added") val dateAdded: Date,           // when this entry was added to the playlist
        @ColumnInfo(name = "transpose") val transpose: Int              // each entry gets its own saved transpose number so changing the number from the favorites menu won't change every entry in every playlist.
) : Parcelable {}
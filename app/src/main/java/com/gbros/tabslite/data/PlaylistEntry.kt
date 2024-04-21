package com.gbros.tabslite.data

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

private const val LOG_NAME = "tabslite.PlaylistEntry "

/**
 * [PlaylistEntry] represents a song in a playlist (or more than once in a playlist). Playlist ID -1
 * is a special playlist for Favorites.
 */
@Serializable
@Entity(tableName = "playlist_entry")
data class PlaylistEntry(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "entry_id") override val entryId: Int = 0,
        @ColumnInfo(name = "playlist_id") override val playlistId: Int,          // what playlist this entry is in
        @ColumnInfo(name = "tab_id") override val tabId: Int,                    // which tab we added to the playlist (references TabFull tabId)
        @ColumnInfo(name = "next_entry_id") override val nextEntryId: Int?,    // the id of the next entry in this playlist
        @ColumnInfo(name = "prev_entry_id") override val prevEntryId: Int?,    // the id of the previous entry in this playlist
        @ColumnInfo(name = "date_added") override val dateAdded: Long,           // when this entry was added to the playlist
        @ColumnInfo(name = "transpose") override var transpose: Int              // each entry gets its own saved transpose number so changing the number from the favorites menu won't change every entry in every playlist.
) : IPlaylistEntry {
    constructor(playlistId: Int, tabId: Int, next_entry_id: Int?, prev_entry_id: Int?, dateAdded: Long, transpose: Int) : this(0, playlistId, tabId, next_entry_id, prev_entry_id, dateAdded, transpose)

    constructor(playlistEntry: IPlaylistEntry) : this(playlistEntry.entryId, playlistEntry.playlistId, playlistEntry.tabId, playlistEntry.nextEntryId, playlistEntry.prevEntryId, playlistEntry.dateAdded, playlistEntry.transpose)

    companion object {
        fun <T:IPlaylistEntry> sortLinkedList(entries: List<T>): List<T> {
            val entryMap = entries.associateBy { it.entryId }
            val sortedEntries = mutableListOf<T>()

            var currentEntry = entries.firstOrNull { it.prevEntryId == null }

            try {
                while (currentEntry != null) {
                    sortedEntries.add(currentEntry)
                    currentEntry = if (currentEntry.nextEntryId != currentEntry.entryId) {
                        entryMap[currentEntry.nextEntryId]
                    } else {
                        Log.e(LOG_NAME, "Error!  Playlist linked list is broken: circular reference")
                        null // stop list traversal
                    }
                }
            } catch (ex: OutOfMemoryError) {
                Log.e(LOG_NAME, "Error!  Playlist linked list is likely broken: circular reference", ex)
            }

            return sortedEntries
        }

    }
}
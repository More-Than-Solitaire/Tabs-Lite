package com.gbros.tabslite.data.playlist

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gbros.tabslite.utilities.TAG

/**
 * [DataPlaylistEntry] represents a song in a playlist (or more than once in a playlist). Playlist ID -1
 * is a special playlist for Favorites.
 */
@Entity(tableName = "playlist_entry")
data class DataPlaylistEntry(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "entry_id") override val entryId: Int = 0,
        @ColumnInfo(name = "playlist_id") override val playlistId: Int,        // what playlist this entry is in
        @ColumnInfo(name = "tab_id") override val tabId: String,               // which tab we added to the playlist (references TabFull tabId)
        @ColumnInfo(name = "next_entry_id") override val nextEntryId: Int?,    // the id of the next entry in this playlist
        @ColumnInfo(name = "prev_entry_id") override val prevEntryId: Int?,    // the id of the previous entry in this playlist
        @ColumnInfo(name = "date_added") override val dateAdded: Long,         // when this entry was added to the playlist
        @ColumnInfo(name = "transpose") override var transpose: Int            // each entry gets its own saved transpose number so changing the number from the favorites menu won't change every entry in every playlist.
) : IDataPlaylistEntry(tabId, transpose, entryId, playlistId, nextEntryId, prevEntryId, dateAdded) {
    constructor(playlistId: Int, tabId: String, next_entry_id: Int?, prev_entry_id: Int?, dateAdded: Long, transpose: Int) : this(0, playlistId, tabId, next_entry_id, prev_entry_id, dateAdded, transpose)

    constructor(playlistEntry: IDataPlaylistEntry) : this(playlistEntry.entryId, playlistEntry.playlistId, playlistEntry.tabId, playlistEntry.nextEntryId, playlistEntry.prevEntryId, playlistEntry.dateAdded, playlistEntry.transpose)

    companion object {
        fun <T : IDataPlaylistEntry> sortLinkedList(entries: List<T>): List<T> {
            val entryMap = entries.associateBy { it.entryId }
            val sortedEntries = mutableListOf<T>()

            var currentEntry = entries.firstOrNull { it.prevEntryId == null }
            try {
                while (currentEntry != null) {
                    sortedEntries.add(currentEntry)

                    if (sortedEntries.all { usedEntry -> usedEntry.entryId != currentEntry!!.nextEntryId }) { // next entry hasn't been used yet; no circular reference
                        // set up for next iteration
                        currentEntry = entryMap[currentEntry.nextEntryId]
                    } else {
                        val errorMessage = "Error!  Playlist ${currentEntry.playlistId} linked list is broken: circular reference"
                        Log.e(TAG, errorMessage)
                        throw BrokenLinkedListException(errorMessage, entries)
                    }
                }
            } catch (ex: OutOfMemoryError) {
                val errorMessage = "Error!  Playlist linked list is likely broken: circular reference"
                Log.e(TAG, errorMessage, ex)
                throw BrokenLinkedListException(errorMessage, ex, entries)
            }

            // add any remaining elements
            if (sortedEntries.size < entries.size) {
                val remainingEntries =
                    entries.filter { entry -> sortedEntries.all { usedEntry -> usedEntry.entryId != entry.entryId } }
                var errorString =
                    "Error! Playlist ${entries[0].playlistId} linked list is broken. Elements remaining after list traversal:\n"
                for (e in remainingEntries) {
                    errorString += "{playlistId: ${e.playlistId}, entryId: ${e.entryId}, nextEntryId: ${e.nextEntryId}, prevEntryId: ${e.prevEntryId}},\n"
                }
                Log.e(TAG, errorString)
                sortedEntries.addAll(remainingEntries)
                throw BrokenLinkedListException(errorString, sortedEntries)
            }

            return sortedEntries
        }
    }
}

/**
 * Exception thrown when Linked List traversal fails. This could be due to circular references, a lack
 * of a starting point, or the list being in an invalid state
 */
class BrokenLinkedListException : Exception {
    /**
     * The broken linked list in question
     */
    val list: List<IDataPlaylistEntry>

    constructor(message: String, list: List<IDataPlaylistEntry>) : super(message) {
        this.list = list
    }
    constructor(message: String, cause: Throwable, list: List<IDataPlaylistEntry>) : super(message, cause) {
        this.list = list
    }
}
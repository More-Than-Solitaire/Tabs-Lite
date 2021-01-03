package com.gbros.tabslite.utilities

import android.content.Context
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

object PlaylistHelper {
    /**
     * Helper function to add this class's [tabId] to the [playlistId] given.  Inserts a new entry
     * at the end, updates the previous last entry to keep the linked list going, and updates the
     * master [Playlist] modifiedDate.
     */
    fun addToPlaylist(playlistId: Int, tabId: Int, transpose: Int = 0, context: Context) {
        // find existing last entry in this playlist
        val getDataJob = GlobalScope.async { AppDatabase.getInstance(context).playlistEntryDao().getLastEntryInPlaylist(playlistId) }
        getDataJob.invokeOnCompletion {
            val prevEntry = getDataJob.getCompleted()

            // insert new entry into the database
            val entry = PlaylistEntry(playlistId, tabId, null, prevEntry?.entryId, System.currentTimeMillis(), transpose)
            val insertJob = GlobalScope.async { AppDatabase.getInstance(context).playlistEntryDao().insert(entry) }
            insertJob.invokeOnCompletion {

                // update existing entry's next_entry_id
                val newEntryId = insertJob.getCompleted()
                prevEntry?.entryId?.let { it1 -> AppDatabase.getInstance(context).playlistEntryDao().setNextEntryId(newEntryId.toInt(), it1) }
            }

            // update playlist modifiedTime
            AppDatabase.getInstance(context).playlistDao().updateTimestamp(playlistId, System.currentTimeMillis())
        }
    }
}
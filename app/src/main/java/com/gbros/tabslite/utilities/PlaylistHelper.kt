package com.gbros.tabslite.utilities

import android.content.Context
import android.util.Log
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

private const val LOG_NAME = "tabslite.PlaylistHelper"


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
            val previouslyLastEntry = getDataJob.getCompleted()  // used to be the last entry in the playlist, until we came along

            Log.v(LOG_NAME, "Adding new playlist entry (tabid $tabId) to playlist $playlistId.  previous entry id: ${previouslyLastEntry?.entryId}")
            // insert new entry into the database
            val entry = PlaylistEntry(playlistId, tabId, null, previouslyLastEntry?.entryId, System.currentTimeMillis(), transpose)
            val insertJob = GlobalScope.async { AppDatabase.getInstance(context).playlistEntryDao().insert(entry) }
            insertJob.invokeOnCompletion {
                val newEntryId = insertJob.getCompleted().toInt()  // the entry we just added

                // update the previously last entry's next_entry_id
                previouslyLastEntry?.entryId?.let { prev -> AppDatabase.getInstance(context).playlistEntryDao().setNextEntryId(prev, newEntryId) }
            }

            // update playlist modifiedTime
            AppDatabase.getInstance(context).playlistDao().updateTimestamp(playlistId, System.currentTimeMillis())
        }
    }
}
package com.gbros.tabslite.data.playlist

import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.tab.Tab
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable

@Serializable
data class SelfContainedPlaylist(
    override val playlistId: Int,
    override val title: String,
    override val description: String,
    override val userCreated: Boolean,
    val entries: List<IPlaylistEntry>
): IPlaylist {
    constructor(playlist: IPlaylist, entries: List<IPlaylistEntry>): this(playlistId = playlist.playlistId, title = playlist.title, description = playlist.description, userCreated = playlist.userCreated, entries = entries)

    /**
     * Imports this instance of [SelfContainedPlaylist] to the database. If this [playlistId] is equal to [Playlist.FAVORITES_PLAYLIST_ID], skips any duplicate entries
     */
    suspend fun importToDatabase(dataAccess: DataAccess, onProgressChange: (progress: Float) -> Unit = {}) {
        var currentlyImportedEntries = 0f
        if (playlistId == Playlist.FAVORITES_PLAYLIST_ID) {
            // get current favorite tabs (to not reimport tabs that are already favorite tabs)
            val currentFavorites = dataAccess.getAllEntriesInPlaylist(Playlist.FAVORITES_PLAYLIST_ID)
            val entriesToImport = entries.filter { e -> currentFavorites.all { currentFav -> e.tabId != currentFav.tabId } }
            for (entry in entriesToImport) {  // don't double-import favorites
                currentlyImportedEntries++
                onProgressChange((currentlyImportedEntries / entriesToImport.size.toFloat()) * 0.4f) // the 0.4f constant makes the import from file part take 40% of the progress, leaving 60% for the fetch from internet below
                dataAccess.appendToPlaylist(playlistId, entry.tabId, entry.transpose)
            }
        } else {
            val newPlaylistID = dataAccess.upsert(
                Playlist(userCreated = userCreated, title = title, dateCreated = System.currentTimeMillis(), dateModified = System.currentTimeMillis(), description = description))
            for (entry in entries) {
                currentlyImportedEntries++
                onProgressChange((currentlyImportedEntries / entries.size.toFloat()) * 0.4f) // the 0.4f constant makes the import from file part take 40% of the progress, leaving 60% for the fetch from internet below
                dataAccess.appendToPlaylist(newPlaylistID.toInt(), entry.tabId, entry.transpose)
            }
        }

        // ensure all entries are downloaded locally
        Tab.fetchAllEmptyPlaylistTabsFromInternet(dataAccess, playlistId) { progress -> onProgressChange(0.4f + (progress * 0.6f)) } // 0.4f is the progress already taken above, 0.6f makes this step take 60% of the progress
    }
}
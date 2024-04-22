package com.gbros.tabslite.data.playlist

import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.Tab
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
    suspend fun importToDatabase(db: AppDatabase) {
        if (playlistId == Playlist.FAVORITES_PLAYLIST_ID) {
            // get current favorite tabs (to not reimport tabs that are already favorite tabs)
            val currentFavorites = db.playlistEntryDao().getAllEntriesInPlaylist(Playlist.FAVORITES_PLAYLIST_ID)
            for (entry in entries.filter { e -> currentFavorites.all { currentFav -> e.tabId != currentFav.tabId } }) {  // don't double-import favorites
                db.playlistEntryDao().addToPlaylist(playlistId, entry.tabId, entry.transpose)
            }
        } else {
            val newPlaylistID = db.playlistDao().savePlaylist(
                Playlist(userCreated = userCreated, title = title, dateCreated = System.currentTimeMillis(), dateModified = System.currentTimeMillis(), description = description))
            for (entry in entries) {
                db.playlistEntryDao().addToPlaylist(newPlaylistID.toInt(), entry.tabId, entry.transpose)
            }
        }

        Tab.fetchAllEmptyPlaylistTabsFromInternet(db)  // ensure all entries are downloaded locally
    }
}
package com.gbros.tabslite.compose.playlists

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.IPlaylistEntry
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry

private const val LOG_NAME = "tabslite.PlaylistScreen"

@Composable
fun PlaylistScreen(playlistId: Int, navigateToTabByPlaylistEntryId: (Int) -> Unit, navigateBack: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val playlist = remember { db.playlistDao().getLivePlaylist(playlistId = playlistId) }
    val liveSongs = remember { db.tabFullDao().getTabsFromPlaylistEntryId(playlistId = playlistId) }
    val songs by liveSongs.observeAsState(listOf())
    var updatedDescription: String? by remember { mutableStateOf(null) }
    var updatedTitle: String? by remember { mutableStateOf(null) }

    // sort songs by internal linked list
    val orderedSongs: List<TabWithPlaylistEntry> = remember(songs) { sortLinkedList(songs) }

    // handle entry rearrangement
    var entryMovedSrc: IPlaylistEntry? by remember { mutableStateOf(null) }
    var entryMovedDest: IPlaylistEntry? by remember { mutableStateOf(null) }
    var entryMovedMoveAfter: Boolean? by remember { mutableStateOf(null) }

    // handle entry deletion
    var playlistEntryToRemove: IPlaylistEntry? by remember { mutableStateOf(null) }

    // handle playlist deletion
    var deletePlaylist by remember { mutableStateOf(false) }

    PlaylistView(
        livePlaylist = playlist,
        songs = orderedSongs,
        navigateToTabByPlaylistEntryId = navigateToTabByPlaylistEntryId,
        titleChanged = { updatedTitle = it },
        descriptionChanged = { updatedDescription = it },
        entryMoved = { src, dest, moveAfter -> entryMovedSrc = src; entryMovedDest = dest; entryMovedMoveAfter = moveAfter },
        entryRemoved = { entry -> playlistEntryToRemove = entry },
        navigateBack = navigateBack,
        deletePlaylist = { deletePlaylist = true }
    )

    BackHandler {
        navigateBack()
    }

    // delete playlist
    LaunchedEffect(key1 = deletePlaylist) {
        if (deletePlaylist) {
            db.playlistDao().deletePlaylist(playlistId)
        }
    }

    // remove playlist entry
    LaunchedEffect(key1 = playlistEntryToRemove) {
        val entryToRemove = playlistEntryToRemove
        if (entryToRemove != null) {
            db.playlistEntryDao().removeEntryFromPlaylist(entryToRemove)
        }
    }

    // rearrange playlist entries
    LaunchedEffect(key1 = entryMovedSrc, key2 = entryMovedDest, key3 = entryMovedMoveAfter) {
        val src = entryMovedSrc
        val dest = entryMovedDest
        val moveAfter = entryMovedMoveAfter
        if (src != null && dest != null && moveAfter != null) {
            if (moveAfter)
                db.playlistEntryDao().moveEntryAfter(src, dest)
            else
                db.playlistEntryDao().moveEntryBefore(src, dest)
        }
    }

    // update playlist description
    LaunchedEffect(key1 = updatedDescription) {
        val copyOfUpdatedDescription = updatedDescription
        if (copyOfUpdatedDescription != null) {
            db.playlistDao().updateDescription(playlistId, copyOfUpdatedDescription)
        }
    }

    // update playlist title
    LaunchedEffect(key1 = updatedTitle) {
        val copyOfUpdatedTitle = updatedTitle
        if (copyOfUpdatedTitle != null) {
            db.playlistDao().updateTitle(playlistId, copyOfUpdatedTitle)
        }
    }
}


private fun sortLinkedList(entries: List<TabWithPlaylistEntry>): List<TabWithPlaylistEntry> {
    val entryMap = entries.associateBy { it.entryId }
    val sortedEntries = mutableListOf<TabWithPlaylistEntry>()

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

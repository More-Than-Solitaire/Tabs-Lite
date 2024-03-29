package com.gbros.tabslite.compose.playlists

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.IPlaylistEntry
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

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

@Composable
private fun PlaylistView(
    livePlaylist: LiveData<Playlist>,
    songs: List<TabWithPlaylistEntry>,
    navigateToTabByPlaylistEntryId: (Int) -> Unit,
    titleChanged: (title: String) -> Unit,
    descriptionChanged: (description: String) -> Unit,
    entryMoved: (src: IPlaylistEntry, dest: IPlaylistEntry, moveAfter: Boolean) -> Unit,
    entryRemoved: (entry: IPlaylistEntry) -> Unit,
    deletePlaylist: () -> Unit,
    navigateBack: () -> Unit
) {
    val playlist by livePlaylist.observeAsState(Playlist(0, true, "", 0, 0, ""))
    var description by remember(playlist) { mutableStateOf(playlist.description) }
    var title by remember(playlist) { mutableStateOf(playlist.title) }
    var deletePlaylistConfirmationDialogShowing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        PlaylistHeader(
            title = title,
            description = description,
            titleChanged = { title = it },
            descriptionChanged = { description = it },
            titleFinalize = { titleChanged(title) },
            descriptionFinalize = { descriptionChanged(description) },
            navigateBack = navigateBack,
            deletePlaylist = {
                deletePlaylistConfirmationDialogShowing = true
            }
        )

        PlaylistSongList(
            songs = songs,
            navigateToTabByPlaylistEntryId = {entryId ->
                // save title and description updates
                if (title != playlist.title) {
                    titleChanged(title)
                }
                if (description != playlist.description) {
                    descriptionChanged(description)
                }

                navigateToTabByPlaylistEntryId(entryId)
            },
            onReorder = entryMoved,
            onRemove = entryRemoved
        )
    }

    if (deletePlaylistConfirmationDialogShowing) {
        DeletePlaylistConfirmationDialog(
            onConfirm = { deletePlaylistConfirmationDialogShowing = false; deletePlaylist(); navigateBack() },
            onDismiss = { deletePlaylistConfirmationDialogShowing = false }
        )
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

@Composable @Preview
private fun PlaylistViewPreview() {
    AppTheme {
        val playlistForTest = MutableLiveData(Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text."))

        PlaylistView(
            livePlaylist = playlistForTest,
            songs = createListOfTabWithPlaylistEntry(50),
            navigateToTabByPlaylistEntryId = {},
            titleChanged = {},
            descriptionChanged = {},
            entryMoved = {_, _, _ -> },
            entryRemoved = {},
            navigateBack = {},
            deletePlaylist = {}
        )
    }
}

private fun createListOfTabWithPlaylistEntry(size: Int): List<TabWithPlaylistEntry> {
    val listOfEntries = mutableListOf<TabWithPlaylistEntry>()
    for (id in 0..size) {
        listOfEntries.add(TabWithPlaylistEntry(entryId = id, playlistId = 1, tabId = id * 20, nextEntryId = if(id<size) id+1 else null,
            prevEntryId = if(id>0) id-1 else null, dateAdded = 0, songId = 12, songName = "Song $id", artistName ="Artist name",
            isVerified = false, numVersions = 4, type = "Chords", part = "part", version = 2, votes = 0,
            rating = 0.0, date = 0, status = "", presetId = 0, tabAccessType = "public", tpVersion = 0,
            tonalityName = "D", versionDescription = "version desc", recordingIsAcoustic = false, recordingTonalityName = "",
            recordingPerformance = "", recordingArtists = arrayListOf(), recommended = arrayListOf(), userRating = 0,
            playlistUserCreated = false, playlistTitle = "playlist title", playlistDateCreated = 0, playlistDescription = "playlist desc",
            playlistDateModified = 0))
    }

    return listOfEntries
}

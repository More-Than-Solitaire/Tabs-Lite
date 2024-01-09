package com.gbros.tabslite.compose.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
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

@Composable
fun PlaylistScreen(playlistId: Int, navigateToTabByPlaylistEntryId: (Int) -> Unit, navigateBack: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val playlist = remember { db.playlistDao().getPlaylist(playlistId = playlistId) }
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

    PlaylistView(
        livePlaylist = playlist,
        songs = orderedSongs,
        navigateToTabByPlaylistEntryId = navigateToTabByPlaylistEntryId,
        titleChanged = { updatedTitle = it },
        descriptionChanged = { updatedDescription = it },
        entryMoved = { src, dest, moveAfter -> entryMovedSrc = src; entryMovedDest = dest; entryMovedMoveAfter = moveAfter },
        entryRemoved = { entry -> playlistEntryToRemove = entry },
        navigateBack = navigateBack
    )

    BackHandler {
        navigateBack()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistView(
    livePlaylist: LiveData<Playlist>,
    songs: List<TabWithPlaylistEntry>,
    navigateToTabByPlaylistEntryId: (Int) -> Unit,
    titleChanged: (title: String) -> Unit,
    descriptionChanged: (description: String) -> Unit,
    entryMoved: (src: IPlaylistEntry, dest: IPlaylistEntry, moveAfter: Boolean) -> Unit,
    entryRemoved: (entry: IPlaylistEntry) -> Unit,
    navigateBack: () -> Unit
) {
    val playlist by livePlaylist.observeAsState(Playlist(0, true, "", 0, 0, ""))
    var description by remember(playlist) { mutableStateOf(playlist.description) }
    var title by remember(playlist) { mutableStateOf(playlist.title) }

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
            navigateBack = navigateBack
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
            onReorder = entryMoved
        )

    }
}

private fun sortLinkedList(entries: List<TabWithPlaylistEntry>): List<TabWithPlaylistEntry> {
    val entryMap = entries.associateBy { it.entryId }
    val sortedEntries = mutableListOf<TabWithPlaylistEntry>()

    var currentEntry = entries.firstOrNull { it.prevEntryId == null }
    while (currentEntry != null) {
        sortedEntries.add(currentEntry)
        currentEntry = entryMap[currentEntry.nextEntryId]
    }

    return sortedEntries
}

@Composable @Preview
private fun PlaylistViewPreview() {
    AppTheme {
        val playlistForTest = MutableLiveData(Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text."))
        val tabForTest1 = TabWithPlaylistEntry(1, 1, 1, 2, null, 1234, 0, "Long Time Ago 1", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
        val tabForTest2 = TabWithPlaylistEntry(2, 1, 1, 3, 1, 1234, 0, "Long Time Ago 2", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
        val tabForTest3 = TabWithPlaylistEntry(3, 1, 1, null, 2, 1234, 0, "Long Time Ago 3", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
        val tabListForTest1 = listOf(tabForTest1, tabForTest2, tabForTest3)

        PlaylistView(
            livePlaylist = playlistForTest,
            songs = createListOfTabWithPlaylistEntry(50),
            navigateToTabByPlaylistEntryId = {},
            titleChanged = {},
            descriptionChanged = {},
            entryMoved = {_, _, _ -> },
            entryRemoved = {},
            navigateBack = {}
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

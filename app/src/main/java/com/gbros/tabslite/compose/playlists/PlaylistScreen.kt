package com.gbros.tabslite.compose.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.compose.dragdropcolumn.DragDropColumn
import com.gbros.tabslite.compose.songlist.SongListItem
import com.gbros.tabslite.compose.swipetodismiss.MaterialSwipeToDismiss
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.IPlaylistEntry
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun PlaylistScreen(playlistId: Int, navigateToTabByPlaylistEntryId: (Int) -> Unit, navigateBack: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val playlist = db.playlistDao().getPlaylist(playlistId = playlistId)
    val songs by db.tabFullDao().getTabsFromPlaylistEntryId(playlistId = playlistId).observeAsState(listOf())
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
    var titleWasFocused: Boolean? by remember { mutableStateOf(null)}
    var descriptionWasFocused: Boolean? by remember { mutableStateOf(null)}

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    placeholder = { Text("Playlist Name") },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (titleWasFocused == true && !it.isFocused && title != playlist.title) {
                                titleChanged(title)
                            }
                            titleWasFocused = it.isFocused
                        }
                )
            },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        TextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Playlist Description") },
            colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (descriptionWasFocused == true && !it.isFocused && description != playlist.description) {
                        descriptionChanged(description)
                    }
                    descriptionWasFocused = it.isFocused
                }
        )

        DragDropColumn(
            items = songs,
            onSwap = { src: Int, dest: Int ->
                if (src != dest && src < songs.size && dest < songs.size) {
                    entryMoved(songs[src], songs[dest], src < dest)
                }
            },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { isDragging, song ->
            MaterialSwipeToDismiss(
                onRemove = { entryRemoved(song) },
                content = {
                    SongListItem(
                        song = song,
                        border = if (isDragging) BorderStroke(
                            1f.dp,
                            MaterialTheme.colorScheme.onSecondaryContainer
                        ) else null,
                        onClick = {
                            // save title and description updates
                            if (title != playlist.title) {
                                titleChanged(title)
                            }
                            if (description != playlist.description) {
                                descriptionChanged(description)
                            }

                            navigateToTabByPlaylistEntryId(song.entryId)
                        }
                    )
                }
            )
        }
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
        val tabForTest = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
        val tabListForTest1 = listOf(tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest)

        PlaylistView(
            livePlaylist = playlistForTest,
            songs = tabListForTest1,
            navigateToTabByPlaylistEntryId = {},
            titleChanged = {},
            descriptionChanged = {},
            entryMoved = {_, _, _ -> },
            entryRemoved = {},
            navigateBack = {}
        )
    }
}
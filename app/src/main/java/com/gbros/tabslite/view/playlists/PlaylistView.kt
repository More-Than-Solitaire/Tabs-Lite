package com.gbros.tabslite.view.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.playlist.IDataPlaylistEntry
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun PlaylistView(
    livePlaylist: LiveData<Playlist>,
    songs: List<TabWithDataPlaylistEntry>,
    navigateToTabByPlaylistEntryId: (Int) -> Unit,
    titleChanged: (title: String) -> Unit,
    descriptionChanged: (description: String) -> Unit,
    entryMoved: (src: IDataPlaylistEntry, dest: IDataPlaylistEntry, moveAfter: Boolean) -> Unit,
    entryRemoved: (entry: IDataPlaylistEntry) -> Unit,
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

private fun createListOfTabWithPlaylistEntry(size: Int): List<TabWithDataPlaylistEntry> {
    val listOfEntries = mutableListOf<TabWithDataPlaylistEntry>()
    for (id in 0..size) {
        listOfEntries.add(TabWithDataPlaylistEntry(entryId = id, playlistId = 1, tabId = id * 20, nextEntryId = if(id<size) id+1 else null,
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


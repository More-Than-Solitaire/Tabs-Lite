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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    val songs = db.tabFullDao().getPlaylistTabs(playlistId = playlistId)

    PlaylistView(
        livePlaylist = playlist,
        liveSongs = songs,
        navigateToTabByPlaylistEntryId = navigateToTabByPlaylistEntryId,
        titleChanged = { newTitle -> db.playlistDao().updateTitle(playlistId, newTitle) },
        descriptionChanged = { newDescription -> db.playlistDao().updateDescription(playlistId, newDescription)},
        entryMoved = { src, dest, moveAfter -> if (moveAfter) db.playlistEntryDao().moveEntryAfter(src, dest) else db.playlistEntryDao().moveEntryBefore(src, dest) },
        entryRemoved = { entry -> db.playlistEntryDao().removeEntryFromPlaylist(entry) },
        navigateBack = navigateBack
    )

    BackHandler {
        navigateBack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistView(
    livePlaylist: LiveData<Playlist>,
    liveSongs: LiveData<List<TabWithPlaylistEntry>>,
    navigateToTabByPlaylistEntryId: (Int) -> Unit,
    titleChanged: (title: String) -> Unit,
    descriptionChanged: (description: String) -> Unit,
    entryMoved: (src: IPlaylistEntry, dest: IPlaylistEntry, moveAfter: Boolean) -> Unit,
    entryRemoved: (entry: IPlaylistEntry) -> Unit,
    navigateBack: () -> Unit
) {
    val playlist by livePlaylist.observeAsState(Playlist(0, true, "", 0, 0, ""))
    val songs by liveSongs.observeAsState(listOf())

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                TextField(
                    value = playlist.title,
                    onValueChange = { titleChanged(it) },
                    singleLine = true,
                    placeholder = { Text("Playlist Name") },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        TextField(
            value = playlist.description,
            onValueChange = { descriptionChanged(it) },
            placeholder = { Text("Playlist Description") },
            colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
        )

        DragDropColumn(
            items = songs,
            onSwap = { src: Int, dest: Int ->
                entryMoved(songs[src], songs[dest], src > dest)
            },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { isDragging, song ->
            MaterialSwipeToDismiss(
                onRemove = { entryRemoved(song) },
                content = {
                    SongListItem(
                        song = song,
                        border = if (isDragging) BorderStroke(
                            0.5f.dp,
                            MaterialTheme.colorScheme.onSecondaryContainer
                        ) else null,
                        onClick = { navigateToTabByPlaylistEntryId(song.entryId) })
                }
            )
        }
    }

}

@Composable @Preview
private fun PlaylistViewPreview() {
    AppTheme {
        val playlistForTest = MutableLiveData(Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text."))
        val tabForTest = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
        val tabListForTest1 = MutableLiveData(listOf(tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest))

        PlaylistView(
            livePlaylist = playlistForTest,
            liveSongs = tabListForTest1,
            navigateToTabByPlaylistEntryId = {},
            titleChanged = {},
            descriptionChanged = {},
            entryMoved = {_, _, _ -> },
            entryRemoved = {},
            navigateBack = {}
        )
    }
}
package com.gbros.tabslite.compose.playlists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
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
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.TabFullWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistView(playlist: Playlist, livePlaylistEntries: LiveData<List<TabFullWithPlaylistEntry>>, navigateToTabById: (Int) -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }

    Column {
        TopAppBar(
            title = {
                TextField(
                    value = playlist.title,
                    onValueChange = { newTitle ->  db.playlistDao().updateTitle(playlist.playlistId, newTitle) },
                    singleLine = true,
                    placeholder = { Text("Playlist Name") },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        TextField(
            value = playlist.description,
            onValueChange = { newDescription ->  db.playlistDao().updateDescription(playlist.playlistId, newDescription) },
            placeholder = { Text("Playlist Description") },
            colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
        )

        val songs by livePlaylistEntries.observeAsState(listOf())
        DragDropColumn (
            items = songs,
            onSwap = { src: Int, dest: Int ->
                if (src > dest) {
                    db.playlistEntryDao().moveEntryBefore(songs[src], songs[dest])
                } else if (src < dest) {
                    db.playlistEntryDao().moveEntryAfter(songs[src], songs[dest])
                }
            },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) { isDragging, song ->
            MaterialSwipeToDismiss(
                onRemove = {
                    //todo: remove song from playlist
                           db.playlistEntryDao().removeEntryFromPlaylist(song)
                },
                content =  {
                    SongListItem(song = song, border = if (isDragging) BorderStroke(0.5f.dp, MaterialTheme.colorScheme.onSecondaryContainer) else null, onClick = { navigateToTabById(song.tabId) })
                }
            )
        }
    }
}

@Composable
fun PlaylistEntry(song: TabFullWithPlaylistEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .focusable()
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .absolutePadding(5.dp, 5.dp, 5.dp, 5.dp)
                .fillMaxWidth()
        ) {
            Column (
                modifier = Modifier
                    .weight(1f)
            ){
                Text(
                    text = song.songName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column {
                Text(text = song.type)
                Text(text = "ver. " + song.version)
            }
        }
    }
}

@Composable @Preview
private fun PlaylistViewPreview() {
    AppTheme {
        val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
        val tabForTest = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
        val tabListForTest1 = MutableLiveData(listOf(tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest))

        PlaylistView(playlist = playlistForTest, livePlaylistEntries = tabListForTest1) {}
    }
}
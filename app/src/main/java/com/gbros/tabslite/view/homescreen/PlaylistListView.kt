package com.gbros.tabslite.view.homescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.addtoplaylistdialog.CreatePlaylistDialog
import com.gbros.tabslite.view.playlists.PlaylistList

@Composable
fun PlaylistListView(
    livePlaylists: LiveData<List<Playlist>>,
    onCreatePlaylist: (title: String, description: String) -> Unit,
    navigateToPlaylistById: (id: Int) -> Unit
) {
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        PlaylistList(livePlaylists = livePlaylists, navigateToPlaylistById = navigateToPlaylistById)

        FloatingActionButton(
            onClick = {
                showCreatePlaylistDialog = true
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Playlist")
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { newPlaylistTitle, newPlaylistDescription ->
                onCreatePlaylist(newPlaylistTitle, newPlaylistDescription)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }
}

@Composable @Preview
private fun PlaylistPagePreview() {
    val playlistForTest = Playlist(0, true, "Playlist Title", 0, 0, "Playlist description")
    AppTheme {
        PlaylistListView(
            livePlaylists = MutableLiveData(
                listOf(
                    playlistForTest,
                    playlistForTest,
                    playlistForTest,
                    playlistForTest
                )
            ),
            onCreatePlaylist = { _, _ -> },
            navigateToPlaylistById = {}
        )
    }
}
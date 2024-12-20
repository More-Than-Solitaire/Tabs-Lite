package com.gbros.tabslite.view.addtoplaylistdialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun AddToPlaylistDialog(tabId: Int, transpose: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val playlists by db.playlistDao().getLivePlaylists().observeAsState(initial = listOf())
    var selectedPlaylist: Playlist? by remember { mutableStateOf(null) }
    var confirmedPlaylist: Playlist? by remember { mutableStateOf(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    if (selectedPlaylist == null && playlists.isNotEmpty()) {
        selectedPlaylist = playlists[0]
    }

    AlertDialog(
        icon = {
            Icon(ImageVector.vectorResource(R.drawable.ic_playlist_add), contentDescription = stringResource(id = R.string.title_add_to_playlist_dialog))
        },
        title = {
            Text(text = stringResource(id = R.string.title_add_to_playlist_dialog))
        },
        text = {
            Row {
                Column(
                    Modifier.weight(1f)
                ) {
                    PlaylistDropdown(playlists = playlists, selectedPlaylist = selectedPlaylist, onSelectionChange = { selectedPlaylist = it })
                }
                Column(

                ) {
                    Button(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                        onClick = {
                            showCreatePlaylistDialog = true
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.app_action_description_create_playlist))
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    confirmedPlaylist = selectedPlaylist
                },
                enabled = selectedPlaylist != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Dismiss")
            }
        }
    )

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(onConfirm = { newPlaylist -> selectedPlaylist = newPlaylist; showCreatePlaylistDialog = false }, onDismiss = { showCreatePlaylistDialog = false })
    }

    LaunchedEffect(key1 = confirmedPlaylist) {
        val copyOfConfirmedlaylist = confirmedPlaylist
        if (copyOfConfirmedlaylist != null) {
            db.playlistEntryDao().addToPlaylist(playlistId = copyOfConfirmedlaylist.playlistId, tabId = tabId, transpose = transpose)
            onConfirm()
        }
    }

}

@Composable @Preview
private fun AddToPlaylistDialogPreview() {
    AppTheme {
        AddToPlaylistDialog(1, 0, {}, {})
    }
}
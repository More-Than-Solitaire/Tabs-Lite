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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>, 
    selectedPlaylistDropdownText: String?,
    onSelectionChange: (Playlist) -> Unit, 
    confirmButtonEnabled: Boolean, 
    onCreatePlaylist: (title: String, description: String) -> Unit, 
    onConfirm: () -> Unit, 
    onDismiss: () -> Unit
) {
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

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
                    PlaylistDropdown(playlists = playlists, title = selectedPlaylistDropdownText ?: stringResource(R.string.select_playlist_dialog_no_selection), onSelectionChange = onSelectionChange)
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
                onClick = onConfirm,
                enabled = confirmButtonEnabled
            ) {
                Text(stringResource(R.string.generic_action_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.generic_action_dismiss))
            }
        }
    )

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { title, description ->
                onCreatePlaylist(title, description)
                showCreatePlaylistDialog = false
            },
            onDismiss = { showCreatePlaylistDialog = false })
    }
}

@Composable @Preview
private fun AddToPlaylistDialogPreview() {
    val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
    val list = listOf(playlistForTest, playlistForTest, playlistForTest ,playlistForTest, playlistForTest)
    AppTheme {
        AddToPlaylistDialog(
            playlists = list,
            selectedPlaylistDropdownText = "Select a playlist...",
            confirmButtonEnabled = false,
            onSelectionChange = { },
            onCreatePlaylist = { _, _ -> },
            onConfirm = { },
            onDismiss = { },
        )
    }
}
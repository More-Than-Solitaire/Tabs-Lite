package com.gbros.tabslite.compose.addtoplaylistdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun CreatePlaylistDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var savedPlaylist: Playlist? by remember { mutableStateOf(null) }

    AlertDialog(
        icon = {
            Icon(Icons.Default.Create, contentDescription = "Create playlist")
        },
        title = {
            Text(text = "Create playlist")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = {title = it },
                    placeholder = { Text("Playlist Title") },
                    singleLine = true
                )
                TextField(
                    value = description,
                    onValueChange = {description = it},
                    placeholder = { Text("Playlist Description") },
                    modifier = Modifier
                )
            }
        },
        onDismissRequest = { },
        confirmButton = {
            TextButton(
                onClick = {
                    savedPlaylist = Playlist(playlistId = 0, userCreated = true, title = title, description = description, dateCreated = System.currentTimeMillis(), dateModified = System.currentTimeMillis())
                },
                enabled = title.isNotBlank()
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

    LaunchedEffect(key1 = savedPlaylist) {
        val copyOfSavedPlaylist = savedPlaylist
        if (copyOfSavedPlaylist != null) {
            db.playlistDao().savePlaylist(copyOfSavedPlaylist)
            onConfirm()
        }
    }
}

@Composable
@Preview
private fun CreatePlaylistDialogPreview() {
    AppTheme {
        CreatePlaylistDialog(onConfirm = { }, onDismiss = { })
    }
}
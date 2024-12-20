package com.gbros.tabslite.view.playlists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun DeletePlaylistConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Delete, contentDescription = null)
        },
        title = {
            Text(text = "Delete playlist?")
        },
        text = {
            Text(text = "Deleting a playlist cannot be undone.")
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable @Preview
private fun DeletePlaylistConfirmationDialogPreview() {
    AppTheme {
        RemovePlaylistEntryConfirmationDialog({}, {})
    }
}
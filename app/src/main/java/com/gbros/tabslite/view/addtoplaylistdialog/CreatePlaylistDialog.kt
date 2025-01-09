package com.gbros.tabslite.view.addtoplaylistdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun CreatePlaylistDialog(onConfirm: (newPlaylistTitle: String, newPlaylistDescription: String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        icon = {
            Icon(Icons.Default.Create, contentDescription = null)
        },
        title = {
            Text(text = stringResource(id = R.string.title_create_playlist_dialog))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = {title = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_playlist_title)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                TextField(
                    value = description,
                    onValueChange = {description = it},
                    placeholder = { Text(stringResource(id = R.string.placeholder_playlist_description)) },
                    modifier = Modifier
                )
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, description)
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(id = R.string.generic_action_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(id = R.string.generic_action_dismiss))
            }
        }
    )
}

@Composable
@Preview
private fun CreatePlaylistDialogPreview() {
    AppTheme {
        CreatePlaylistDialog(onConfirm = {_, _ -> }, onDismiss = { })
    }
}
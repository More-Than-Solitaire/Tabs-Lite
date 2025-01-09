package com.gbros.tabslite.view.addtoplaylistdialog

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDropdown(playlists: List<Playlist>, title: String, onSelectionChange: (selectedPlaylist: Playlist) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { nowExpanded -> expanded = nowExpanded }
    ) {
        TextField(
            value = title,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, playlists.isNotEmpty()),
            enabled = playlists.isNotEmpty()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            playlists.forEach { playlist: Playlist ->
                DropdownMenuItem(
                    text = {
                        Text(text = playlist.title)
                    },
                    onClick = {
                        expanded = false
                        onSelectionChange(playlist)
                    }
                )
            }
        }
    }
}

@Composable @Preview
private fun PlaylistDropdownPreview() {
    val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
    val list = listOf(playlistForTest, playlistForTest, playlistForTest ,playlistForTest, playlistForTest)

    AppTheme {
        PlaylistDropdown(
            list,
            "Select a playlist...",
        ) {}
    }
}
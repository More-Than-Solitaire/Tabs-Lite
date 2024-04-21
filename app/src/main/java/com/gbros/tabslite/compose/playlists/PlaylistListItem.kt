package com.gbros.tabslite.compose.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

/**
 * Single list item representing one playlist
 */
@Composable
fun PlaylistListItem(playlist: Playlist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .focusable()
            .fillMaxWidth()
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
                    text = playlist.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
@Preview
fun PlaylistListItemPreview(){
    val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
    AppTheme {
        PlaylistListItem(playlist = playlistForTest) {}
    }
}

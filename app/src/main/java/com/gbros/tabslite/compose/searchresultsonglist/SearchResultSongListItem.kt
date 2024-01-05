package com.gbros.tabslite.compose.searchresultsonglist

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.data.TabFullWithPlaylistEntry

@Composable
fun SearchResultSongListItem(song: IntTabFull, onClick: () -> Unit){
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
                    text = song.songName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = pluralStringResource(id = R.plurals.num_song_versions, song.numVersions, song.numVersions)
            )
        }
    }
}

@Composable @Preview
private fun SearchResultSongListItemPreview() {
    val tabForTest = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    SearchResultSongListItem(song = tabForTest) {}
}
package com.gbros.tabslite.compose.songversionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.compose.ratingicon.RatingIcon
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry

@Composable
fun SongVersionListItem(song: ITab, onClick: () -> Unit){
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .focusable()
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(all = 5.dp)
        ){
            Text(
                text = stringResource(R.string.tab_version_number, song.version),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
            )
            RatingIcon(rating = song.rating)
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .width(48.dp)
            ) {
                Text(
                    text = song.votes.toString(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 1.dp)
                )
            }
        }
    }
}

@Composable @Preview
private fun SongVersionListItemPreview() {
    val tabForTest = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    SongVersionListItem(song = tabForTest) {}
}
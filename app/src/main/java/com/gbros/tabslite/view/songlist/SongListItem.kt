package com.gbros.tabslite.view.songlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

/**
 * Single list item representing one song
 */
@Composable
fun SongListItem(
    modifier: Modifier = Modifier,
    song: ITab,
) {
    Card {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .absolutePadding(5.dp, 5.dp, 5.dp, 5.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
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
            Column {
                Text(text = song.type)
                Text(
                    text = String.format(
                        stringResource(id = R.string.tab_version_abbreviation),
                        song.version
                    )
                )
            }
        }
    }
}

@Composable @Preview
fun SongListItemPreview(){
    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", 1, false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    AppTheme {
        SongListItem(song = tabForTest)
    }
}

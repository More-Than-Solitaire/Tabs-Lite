package com.gbros.tabslite.compose.tabview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry

@Composable
fun TabSummary(tab: ITab) {
    Row {
        Column {
            Text(
                text = stringResource(id = R.string.tab_difficulty, tab.difficulty),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.tab_tuning, tab.tuning),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(
                    id = R.string.tab_capo,
                    tab.getCapoText()
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(text = stringResource(id = R.string.tab_key, tab.tonalityName),
                color = MaterialTheme.colorScheme.onBackground)
            Text(text = stringResource(id = R.string.tab_author, tab.contributorUserName),
                color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable @Preview
private fun TabSummaryPreview() {
    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = "[tab]     [ch]C[/ch]                   [ch]Am[/ch] \nThat David played and it pleased the Lord[/tab]")
    TabSummary(tab = tabForTest)
}
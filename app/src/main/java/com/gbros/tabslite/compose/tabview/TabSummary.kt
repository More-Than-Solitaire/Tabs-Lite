package com.gbros.tabslite.compose.tabview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.R
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.data.TabFullWithPlaylistEntry

@Composable
fun TabSummary(tab: IntTabFull) {
    Row {
        Column {
            Text(
                text = stringResource(id = R.string.tab_difficulty, tab.difficulty),
            )
            Text(
                text = stringResource(id = R.string.tab_tuning, tab.tuning)
            )
            Text(
                text = stringResource(
                    id = R.string.tab_capo,
                    tab.getCapoText()
                )
            )
            Text(text = stringResource(id = R.string.tab_key, tab.tonalityName))
            Text(text = stringResource(id = R.string.tab_author, tab.contributorUserName))
        }
    }
}

@Composable @Preview
private fun TabSummaryPreview() {
    val tabForTest = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = "[tab]     [ch]C[/ch]                   [ch]Am[/ch] \nThat David played and it pleased the Lord[/tab]")
    TabSummary(tab = tabForTest)
}
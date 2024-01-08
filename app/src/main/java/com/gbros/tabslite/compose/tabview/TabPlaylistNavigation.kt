package com.gbros.tabslite.compose.tabview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun TabPlaylistNavigation(tab: TabWithPlaylistEntry, navigateToTabByPlaylistEntryId: (id: Int) -> Unit) {
    Row {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row {
                Text(
                    text = tab.playlistTitle?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(all = 6.dp)
                        .weight(1f)
                )
                IconButton(enabled = tab.prevEntryId != null, onClick = {
                    tab.prevEntryId?.let { navigateToTabByPlaylistEntryId(it) }
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_skip_back) ,
                        contentDescription = "Previous"
                    )
                }
                IconButton(enabled = tab.nextEntryId != null, onClick = {
                    tab.nextEntryId?.let { navigateToTabByPlaylistEntryId(it) }
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_skip_forward),
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable @Preview
private fun TabPlaylistNavigationPreview() {
    val tabForTest = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = "[tab]     [ch]C[/ch]                   [ch]Am[/ch] \nThat David played and it pleased the Lord[/tab]")
    AppTheme {
        TabPlaylistNavigation(tabForTest, {})
    }
}
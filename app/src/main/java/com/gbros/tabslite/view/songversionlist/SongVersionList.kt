package com.gbros.tabslite.view.songversionlist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry

@Composable
fun SongVersionList(songVersions: List<ITab>, navigateToTabByTabId: (id: String) -> Unit){
    LazyColumn{
        items(songVersions) { version ->
            SongVersionListItem(song = version) {
                navigateToTabByTabId(version.tabId)
            }
        }
    }
}

@Composable @Preview
private fun SongVersionListPreview() {
    val tabForTest1 = TabWithDataPlaylistEntry(1, 1, "1", 1, 1, 1234, "0", "Long Time Ago", "rock","CoolGuyz", "1", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , "public", "C", "E A D G B E", false, ArrayList(), "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabForTest2 = TabWithDataPlaylistEntry(1, 1, "1", 1, 1, 1234, "0", "Long Time Ago", "rock","CoolGuyz", "1", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , "public", "C", "E A D G B E", false, ArrayList(), "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest = listOf(tabForTest1, tabForTest2)

    SongVersionList(tabListForTest, {})
}
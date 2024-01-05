package com.gbros.tabslite.compose.songversionlist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.data.TabFullWithPlaylistEntry

@Composable
fun SongVersionList(songVersions: List<TabFullWithPlaylistEntry>){
    val sortedVersions = remember { songVersions.sortedByDescending { v -> v.votes } }

    LazyColumn{
        items(sortedVersions) { version ->
            SongVersionListItem(song = version) {
                //todo: navigate to tab view for this version
            }
        }
    }
}

@Composable @Preview
private fun SongVersionListPreview() {
    val tabForTest1 = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabForTest2 = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 2, 8, 4.1, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest = listOf(tabForTest1, tabForTest2)

    SongVersionList(tabListForTest)
}
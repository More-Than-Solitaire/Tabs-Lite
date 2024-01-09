package com.gbros.tabslite.compose.songlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongList(modifier: Modifier = Modifier, liveSongs: LiveData<List<TabWithPlaylistEntry>>, navigateByPlaylistEntryId: Boolean = false, navigateToTabById: (id: Int) -> Unit, verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp), initialSortBy: SortBy, sorter: (SortBy, List<TabWithPlaylistEntry>) -> List<TabWithPlaylistEntry>){
    var expanded by remember { mutableStateOf(false) }
    val songs by liveSongs.observeAsState(listOf())
    var sortBy: SortBy by remember { mutableStateOf(initialSortBy) }
    val sortedSongs by remember(key1 = songs, key2 = sortBy) {
        mutableStateOf(sorter(sortBy, songs))
    }

    Column {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {  }, modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)) {
            Button(
                onClick = { expanded = !expanded},
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Sort by: " + SortBy.getString(sortBy))
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                for (sortOption in SortBy.entries) {
                    DropdownMenuItem(
                        text = { Text(text = SortBy.getString(sortOption)) },
                        onClick = { expanded = false; sortBy = sortOption }
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = verticalArrangement,
            modifier = modifier
        ) {
            items(sortedSongs) { song ->
                SongListItem(
                    song = song,
                    onClick = { navigateToTabById(if (navigateByPlaylistEntryId) song.entryId else song.tabId) })
            }
            item {
                Spacer(modifier = Modifier.height(height = 24.dp))
            }
        }
    }
}

enum class SortBy {
    DateAdded,
    Name,
    ArtistName,
    Popularity;

    companion object {
        fun getString(entry: SortBy): String {
            return when(entry) {
                DateAdded -> "Date Added"
                Popularity -> "Popularity"
                ArtistName -> "Artist Name"
                Name -> "Title"
            }
        }
    }
}

@Composable @Preview
fun SongListPreview(){
    val tabForTest1 = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabForTest2 = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest = MutableLiveData(listOf(tabForTest1, tabForTest2))

    AppTheme {
        SongList(liveSongs = tabListForTest, navigateToTabById = {}, initialSortBy = SortBy.DateAdded, sorter = {sortBy, songs -> songs})
    }
}
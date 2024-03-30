package com.gbros.tabslite.compose.songlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

/**
 * The view including both the list of songs and the dropdown for sorting them
 */
@Composable
fun SongListView(
    modifier: Modifier = Modifier,
    liveSongs: LiveData<List<TabWithPlaylistEntry>>,
    navigateByPlaylistEntryId: Boolean = false,
    navigateToTabById: (id: Int) -> Unit,
    initialSortBy: SortBy,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
    sorter: (SortBy, List<TabWithPlaylistEntry>) -> List<TabWithPlaylistEntry>,
    emptyListText: String = "Nothing here!",
){
    var expanded by remember { mutableStateOf(false) }
    val songs by liveSongs.observeAsState(listOf())
    var sortBy: SortBy by remember { mutableStateOf(initialSortBy) }
    val sortedSongs by remember(key1 = songs, key2 = sortBy) {
        mutableStateOf(sorter(sortBy, songs))
    }

    Column {
        SortByDropdown(selectedSort = sortBy, onOptionSelected = { newSort -> sortBy = newSort })

        SongList(modifier = modifier, songs = sortedSongs, navigateToTabById = navigateToTabById, navigateByPlaylistEntryId = navigateByPlaylistEntryId, verticalArrangement = verticalArrangement, emptyListText = emptyListText)
    }
}

@Composable @Preview
fun SongListViewPreview(){
    val tabForTest1 = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabForTest2 = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest = MutableLiveData(listOf(tabForTest1, tabForTest2))

    AppTheme {
        SongListView(liveSongs = tabListForTest, navigateToTabById = {}, initialSortBy = SortBy.DateAdded, sorter = {sortBy, songs -> songs})
    }
}
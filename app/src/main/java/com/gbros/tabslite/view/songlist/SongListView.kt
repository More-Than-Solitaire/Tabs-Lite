package com.gbros.tabslite.view.songlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.InfoCard

private const val LOG_NAME = "tabslite.SongListView  "

/**
 * The view including both the list of songs and the dropdown for sorting them
 */
@Composable
fun SongListView(
    modifier: Modifier = Modifier,
    viewState: ISongListViewState,
    navigateByPlaylistEntryId: Boolean = false,
    navigateToTabById: (id: Int) -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
    emptyListText: String = stringResource(id = R.string.message_empty_list),
    onSortSelectionChange: (SortBy) -> Unit
){
    Column {
        SortByDropdown(selectedSort = viewState.sortBy.observeAsState().value, onOptionSelected = onSortSelectionChange)
        val songs = viewState.songs.observeAsState(listOf())
        if (songs.value.isEmpty()) {
            // no songs
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp)
            ) {
                InfoCard(text = emptyListText)
            }
        } else {
            LazyColumn(
                verticalArrangement = verticalArrangement,
                modifier = modifier
            ) {
                items(songs.value) { song ->
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
}

@Composable @Preview
private fun SongListViewPreview(){
    val tabForTest1 = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabForTest2 = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest = MutableLiveData(listOf(tabForTest1, tabForTest2))

    val viewState = SongListViewStateForTest(
        songs = tabListForTest,
        sortBy = MutableLiveData(SortBy.Name)
    )

    AppTheme {
        SongListView(viewState = viewState, navigateToTabById = {}, onSortSelectionChange = { })
    }
}

private class SongListViewStateForTest(
    override val songs: LiveData<List<TabWithDataPlaylistEntry>>,
    override val sortBy: LiveData<SortBy>
) : ISongListViewState
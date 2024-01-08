package com.gbros.tabslite.compose.searchresultsonglist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.compose.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.ISearch
import com.gbros.tabslite.data.Search
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun SearchScreen(
    query: String,
    navigateToSongVersionsBySongId: (songId: Int) -> Unit,
    navigateBack: () -> Unit,
    onSearch: (query: String) -> Unit
) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val searchSession = Search(query, db)

    SearchView(searchSession = searchSession, navigateToSongVersionsBySongId = navigateToSongVersionsBySongId, navigateBack = navigateBack, onSearch = onSearch)

    BackHandler {
        navigateBack()
    }
}

@Composable
private fun SearchView(
    searchSession: ISearch,
    navigateToSongVersionsBySongId: (songId: Int) -> Unit,
    navigateBack: () -> Unit,
    onSearch: (query: String) -> Unit
) {
    val searchResults by searchSession.result.observeAsState()
    val lazyColumnState = rememberLazyListState()
    val endOfSearchResults by searchSession.allPagesLoaded.observeAsState(false)
    var needMoreSearchResults by remember { mutableStateOf(false) }
    needMoreSearchResults = needMoreSearchResults || !lazyColumnState.canScrollForward  // remember that we bumped into the end until we get more results

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        TabsSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            initialQueryText = searchSession.query,
            leadingIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            onSearch = onSearch
        )

        searchResults?.let { songs ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), state = lazyColumnState) {
                items(items = songs) { song ->
                    SearchResultCard(song) {
                        navigateToSongVersionsBySongId(
                            song.songId
                        )
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        if (!endOfSearchResults) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = searchResults) {
        needMoreSearchResults = false
    }

    if (needMoreSearchResults && !searchSession.searchInProgress && !endOfSearchResults) {
        searchSession.searchNextPage()
    }
}

@Composable @Preview
private fun SearchViewPreview() {

    AppTheme {
        SearchView(SearchForTest(false, true), {}, {}) {}
    }
}

private class SearchForTest(allPagesLoaded: Boolean, override var searchInProgress: Boolean) : ISearch {
    private val tabForTest1 = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    private val tabForTest2 = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    private val resultListForTest = MutableLiveData(listOf<ITab>(tabForTest1, tabForTest2, tabForTest1, tabForTest2))

    override var allPagesLoaded: LiveData<Boolean> = MutableLiveData(allPagesLoaded)

    override val query = "Search Query text"

    override val result = resultListForTest
    override fun searchNextPage() {

    }


}
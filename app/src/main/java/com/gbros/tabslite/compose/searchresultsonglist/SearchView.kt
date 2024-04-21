package com.gbros.tabslite.compose.searchresultsonglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.compose.card.InfoCard
import com.gbros.tabslite.compose.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.data.ISearch
import com.gbros.tabslite.data.SearchDidYouMeanException
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SearchView(
    query: String,
    searchSession: ISearch,
    navigateToSongVersionsBySongId: (songId: Int) -> Unit,
    navigateBack: () -> Unit,
    onSearch: (query: String) -> Unit
) {
    var searchResults: List<ITab> by remember { mutableStateOf(listOf()) }
    val lazyColumnState = rememberLazyListState()
    var needMoreSearchResults by remember { mutableStateOf(true) }
    var showProgressIndicator by remember { mutableStateOf(false) }

    var currentSearchPage by remember { mutableIntStateOf(0) }
    var currentSearchQuery by remember { mutableStateOf(query) }
    var endOfSearchResults by remember { mutableStateOf(false) }

    // remember that we bumped into the end until we get more results
    needMoreSearchResults = needMoreSearchResults || !lazyColumnState.canScrollForward

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        TabsSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            initialQueryText = query,
            leadingIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            onSearch = onSearch
        )

        if (endOfSearchResults && searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp), contentAlignment = Alignment.Center
            ) {
                InfoCard(text = "No search results. Revise your query or check your internet connection.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), state = lazyColumnState) {
                items(items = searchResults) { song ->
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
                        if (showProgressIndicator) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

    }

    LaunchedEffect(key1 = needMoreSearchResults, key2 = lazyColumnState.canScrollForward) {
        showProgressIndicator = true
        var retriesLeft = 3
        needMoreSearchResults = needMoreSearchResults || !lazyColumnState.canScrollForward
        while (needMoreSearchResults && !endOfSearchResults && retriesLeft > 0) {
            try {
                val newSearchResults = searchSession.getSearchResults(currentSearchPage+1, currentSearchQuery)
                withContext(Dispatchers.Main.immediate) {
                    currentSearchPage += 1
                    searchResults = searchResults.toMutableList().apply { addAll(newSearchResults) }
                    endOfSearchResults = endOfSearchResults || newSearchResults.isEmpty()
                    needMoreSearchResults = !lazyColumnState.canScrollForward
                }

            } catch (ex: SearchDidYouMeanException) {
                withContext(Dispatchers.Main.immediate) {
                    endOfSearchResults = false
                    currentSearchPage = 0
                    currentSearchQuery = ex.didYouMean
                    retriesLeft--
                }
            }
        }

        showProgressIndicator = false
    }
}

@Composable
@Preview
private fun SearchViewPreview() {

    AppTheme {
        SearchView("test query", SearchForTest(), {}, {}, {})
    }
}

private class SearchForTest() :
    ISearch {
    private val tabForTest1 = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    private val tabForTest2 = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    private val resultListForTest = listOf<ITab>(tabForTest1, tabForTest2, tabForTest1, tabForTest2)

    override suspend fun getSearchResults(
        page: Int,
        query: String
    ): List<ITab> {
        return resultListForTest
    }
}
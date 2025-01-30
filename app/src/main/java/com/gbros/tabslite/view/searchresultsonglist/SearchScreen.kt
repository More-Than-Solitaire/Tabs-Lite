package com.gbros.tabslite.view.searchresultsonglist

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.ErrorCard
import com.gbros.tabslite.view.card.InfoCard
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.SearchViewModel

private const val SEARCH_NAV_ARG = "query"
const val SEARCH_ROUTE_TEMPLATE = "search/%s"

fun NavController.navigateToSearch(query: String) {
    navigate(SEARCH_ROUTE_TEMPLATE.format(query))
}

fun NavGraphBuilder.searchScreen(
    onNavigateToSongId: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(
        SEARCH_ROUTE_TEMPLATE.format("{$SEARCH_NAV_ARG}")
    ) { navBackStackEntry ->
        val query = navBackStackEntry.arguments!!.getString(SEARCH_NAV_ARG, "")
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: SearchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.SearchViewModelFactory> { factory -> factory.create(query, db.dataAccess()) }
        SearchScreen(
            viewState = viewModel,
            tabSearchBarViewState = viewModel.tabSearchBarViewModel,
            onMoreSearchResultsNeeded = viewModel::onMoreSearchResultsNeeded,
            onTabSearchBarQueryChange = viewModel.tabSearchBarViewModel::onQueryChange,
            onNavigateToSongVersionsBySongId = onNavigateToSongId,
            onNavigateBack = onNavigateBack,
            onNavigateToSearch = onNavigateToSearch
        )
    }
}

@Composable
fun SearchScreen(
    viewState: ISearchViewState,
    tabSearchBarViewState: ITabSearchBarViewState,
    onMoreSearchResultsNeeded: suspend () -> Unit,
    onTabSearchBarQueryChange: (newQuery: String) -> Unit,
    onNavigateToSongVersionsBySongId: (songId: Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (query: String) -> Unit
) {
    val lazyColumnState = rememberLazyListState()
    var needMoreSearchResults by remember { mutableStateOf(true) }
    val searchResults = viewState.results.observeAsState(listOf())
    val searchState = viewState.searchState.observeAsState(LoadingState.Loading)

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
            leadingIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.generic_action_back))
                }
            },
            viewState = tabSearchBarViewState,
            onSearch = onNavigateToSearch,
            onQueryChange = onTabSearchBarQueryChange
        )

        if (searchState.value is LoadingState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp), contentAlignment = Alignment.Center
            ) {
                ErrorCard(text = (searchState.value as LoadingState.Error).message)
            }
        } else if (searchState.value is LoadingState.Success && searchResults.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp), contentAlignment = Alignment.Center
            ) {
                InfoCard(text = stringResource(id = R.string.message_no_search_results))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), state = lazyColumnState) {
                items(items = searchResults.value) { song ->
                    SearchResultCard(song) {
                        onNavigateToSongVersionsBySongId(song.songId)
                    }
                }

                // extra item at the end to display the circular progress indicator if we're still loading
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        if (searchState.value is LoadingState.Loading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

    }

    LaunchedEffect(key1 = lazyColumnState.canScrollForward, key2 = searchState.value, key3 = searchResults.value) {
        if (!lazyColumnState.canScrollForward && (viewState.allResultsLoaded.value != true)){
            onMoreSearchResultsNeeded()
        }
    }

    BackHandler {
        onNavigateBack()
    }
}


//#region test/preview

private class SearchViewStateForTest(
    override val query: String,
    override val results: LiveData<List<ITab>>,
    override val searchState: LiveData<LoadingState>,
    override val allResultsLoaded: LiveData<Boolean>
) : ISearchViewState

private class TabSearchBarViewStateForTest(
    override val query: LiveData<String>,
    override val searchSuggestions: LiveData<List<String>>
): ITabSearchBarViewState

@Composable
@Preview
private fun SearchScreenPreview() {
    val hallelujahTabForTest = """
        [Intro]
        [ch]C[/ch] [ch]Em[/ch] [ch]C[/ch] [ch]Em[/ch]
         
        [Verse]
        [tab][ch]C[/ch]                [ch]Em[/ch]
          Hey there Delilah, What’s it like in New York City?[/tab]
        [tab]      [ch]C[/ch]                                      [ch]Em[/ch]                                  [ch]Am[/ch]   [ch]G[/ch]
        I’m a thousand miles away, But girl tonight you look so pretty, Yes you do, [/tab]
        
        [tab]F                   [ch]G[/ch]                  [ch]Am[/ch]
          Time Square can’t shine as bright as you, [/tab]
        [tab]             [ch]G[/ch]
        I swear it’s true. [/tab]
        [tab][ch]C[/ch]
          Hey there Delilah, [/tab]
        [tab]          [ch]Em[/ch]
        Don’t you worry about the distance, [/tab]
        [tab]          [ch]C[/ch]
        I’m right there if you get lonely, [/tab]
        [tab]          [ch]Em[/ch]
        [ch]G[/ch]ive this song another listen, [/tab]
        [tab]           [ch]Am[/ch]     [ch]G[/ch]
        Close your eyes, [/tab]
        [tab]F              [ch]G[/ch]                [ch]Am[/ch]
          Listen to my voice it’s my disguise, [/tab]
        [tab]            [ch]G[/ch]
        I’m by your side.[/tab]    """.trimIndent()
    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = hallelujahTabForTest)
    val state = SearchViewStateForTest("my song", MutableLiveData(listOf(tabForTest, tabForTest, tabForTest)), MutableLiveData(LoadingState.Loading), MutableLiveData(false))
    val tabSearchBarViewState = TabSearchBarViewStateForTest(
        query = MutableLiveData("my song"),
        searchSuggestions = MutableLiveData(listOf()),
    )

    AppTheme {
        SearchScreen(
            viewState = state,
            tabSearchBarViewState = tabSearchBarViewState,
            onMoreSearchResultsNeeded = {},
            onNavigateToSongVersionsBySongId = {},
            onNavigateBack = {},
            onNavigateToSearch = {},
            onTabSearchBarQueryChange = {}
        )
    }
}

@Composable
@Preview
private fun SearchScreenPreviewError() {
    val hallelujahTabForTest = """
        [Intro]
        [ch]C[/ch] [ch]Em[/ch] [ch]C[/ch] [ch]Em[/ch]
         
        [Verse]
        [tab][ch]C[/ch]                [ch]Em[/ch]
          Hey there Delilah, What’s it like in New York City?[/tab]
        [tab]      [ch]C[/ch]                                      [ch]Em[/ch]                                  [ch]Am[/ch]   [ch]G[/ch]
        I’m a thousand miles away, But girl tonight you look so pretty, Yes you do, [/tab]
        
        [tab]F                   [ch]G[/ch]                  [ch]Am[/ch]
          Time Square can’t shine as bright as you, [/tab]
        [tab]             [ch]G[/ch]
        I swear it’s true. [/tab]
        [tab][ch]C[/ch]
          Hey there Delilah, [/tab]
        [tab]          [ch]Em[/ch]
        Don’t you worry about the distance, [/tab]
        [tab]          [ch]C[/ch]
        I’m right there if you get lonely, [/tab]
        [tab]          [ch]Em[/ch]
        [ch]G[/ch]ive this song another listen, [/tab]
        [tab]           [ch]Am[/ch]     [ch]G[/ch]
        Close your eyes, [/tab]
        [tab]F              [ch]G[/ch]                [ch]Am[/ch]
          Listen to my voice it’s my disguise, [/tab]
        [tab]            [ch]G[/ch]
        I’m by your side.[/tab]    """.trimIndent()
    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = hallelujahTabForTest)
    val state = SearchViewStateForTest("my song", MutableLiveData(listOf(tabForTest, tabForTest, tabForTest)), MutableLiveData(LoadingState.Error("Unexpected error: test error")), MutableLiveData(false))

    val tabSearchBarViewState = TabSearchBarViewStateForTest(
        query = MutableLiveData("my song"),
        searchSuggestions = MutableLiveData(listOf()),
    )

    AppTheme {
        SearchScreen(
            viewState = state,
            tabSearchBarViewState = tabSearchBarViewState,
            onMoreSearchResultsNeeded = {},
            onNavigateToSongVersionsBySongId = {},
            onNavigateBack = {},
            onNavigateToSearch = {},
            onTabSearchBarQueryChange = {}
        )
    }
}


//#endregion

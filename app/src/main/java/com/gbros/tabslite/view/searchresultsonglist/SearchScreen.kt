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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.ErrorCard
import com.gbros.tabslite.view.card.InfoCard
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.SearchViewModel

private const val TITLE_SEARCH_NAV_ARG = "query"
private const val TITLE_SEARCH_ROUTE_TEMPLATE = "search/%s"
private const val ARTIST_SONG_LIST_TEMPLATE = "artist/%s"
private const val ARTIST_SONG_LIST_NAV_ARG = "artistId"

/**
 * NavController extension to allow navigation to the search screen based on a query
 */
fun NavController.navigateToSearch(query: String) {
    navigate(TITLE_SEARCH_ROUTE_TEMPLATE.format(query))
}

/**
 * NavController extension to allow navigation to a list of songs by a specified artist ID
 */
fun NavController.navigateToArtistIdSongList(artistId: Int) {
    navigate(ARTIST_SONG_LIST_TEMPLATE.format(artistId.toString()))
}

/**
 * NavGraphBuilder extension to build the search by title screen for when a user searches using text
 * (normal search)
 */
fun NavGraphBuilder.searchByTitleScreen(
    onNavigateToSongId: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToTabByTabId: (tabId: Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(
        route = TITLE_SEARCH_ROUTE_TEMPLATE.format("{$TITLE_SEARCH_NAV_ARG}")
    ) { navBackStackEntry ->
        val query = navBackStackEntry.arguments!!.getString(TITLE_SEARCH_NAV_ARG, "")
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: SearchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.SearchViewModelFactory> { factory -> factory.create(query, null, db.dataAccess()) }
        SearchScreen(
            viewState = viewModel,
            tabSearchBarViewState = viewModel.tabSearchBarViewModel,
            onMoreSearchResultsNeeded = viewModel::onMoreSearchResultsNeeded,
            onTabSearchBarQueryChange = viewModel.tabSearchBarViewModel::onQueryChange,
            onNavigateToSongVersionsBySongId = onNavigateToSongId,
            onNavigateBack = onNavigateBack,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToTabByTabId = onNavigateToTabByTabId
        )
    }
}

/**
 * NavGraphBuilder extension to build the search by artist ID screen for when a user clicks an 
 * artist name to see all songs by that artist
 */
fun NavGraphBuilder.listSongsByArtistIdScreen(
    onNavigateToSongId: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateToTabByTabId: (tabId: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        route = ARTIST_SONG_LIST_TEMPLATE.format("{$ARTIST_SONG_LIST_NAV_ARG}"),
        arguments = listOf(navArgument(ARTIST_SONG_LIST_NAV_ARG) { type = NavType.IntType } )
    ) { navBackStackEntry ->
        val artistId = navBackStackEntry.arguments!!.getInt(ARTIST_SONG_LIST_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: SearchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.SearchViewModelFactory> { factory -> factory.create("", artistId, db.dataAccess()) }
        SearchScreen(
            viewState = viewModel,
            tabSearchBarViewState = viewModel.tabSearchBarViewModel,
            onMoreSearchResultsNeeded = viewModel::onMoreSearchResultsNeeded,
            onTabSearchBarQueryChange = viewModel.tabSearchBarViewModel::onQueryChange,
            onNavigateToSongVersionsBySongId = onNavigateToSongId,
            onNavigateBack = onNavigateBack,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToTabByTabId = onNavigateToTabByTabId
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
    onNavigateToSearch: (query: String) -> Unit,
    onNavigateToTabByTabId: (tabId: Int) -> Unit
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
            onQueryChange = onTabSearchBarQueryChange,
            onNavigateToTabById = onNavigateToTabByTabId
        )

        if (searchState.value is LoadingState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp), contentAlignment = Alignment.Center
            ) {
                ErrorCard(text = stringResource((searchState.value as LoadingState.Error).messageStringRef))
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
    override val searchSuggestions: LiveData<List<String>>,
    override val tabSuggestions: LiveData<List<ITab>>,
    override val loadingState: LiveData<LoadingState>
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
    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", 1, false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = hallelujahTabForTest)
    val state = SearchViewStateForTest("my song", MutableLiveData(listOf(tabForTest, tabForTest, tabForTest)), MutableLiveData(LoadingState.Loading), MutableLiveData(false))
    val tabSearchBarViewState = TabSearchBarViewStateForTest(
        query = MutableLiveData("my song"),
        searchSuggestions = MutableLiveData(listOf()),
        tabSuggestions = MutableLiveData(listOf(Tab(0))),
        loadingState = MutableLiveData()
    )

    AppTheme {
        SearchScreen(
            viewState = state,
            tabSearchBarViewState = tabSearchBarViewState,
            onMoreSearchResultsNeeded = {},
            onNavigateToSongVersionsBySongId = {},
            onNavigateBack = {},
            onNavigateToSearch = {},
            onTabSearchBarQueryChange = {},
            onNavigateToTabByTabId = {}
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
    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", 1, false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = hallelujahTabForTest)
    val state = SearchViewStateForTest("my song", MutableLiveData(listOf(tabForTest, tabForTest, tabForTest)), MutableLiveData(LoadingState.Error(R.string.error)), MutableLiveData(false))

    val tabSearchBarViewState = TabSearchBarViewStateForTest(
        query = MutableLiveData("my song"),
        searchSuggestions = MutableLiveData(listOf()),
        tabSuggestions = MutableLiveData(listOf(Tab(0))),
        loadingState = MutableLiveData()
    )

    AppTheme {
        SearchScreen(
            viewState = state,
            tabSearchBarViewState = tabSearchBarViewState,
            onMoreSearchResultsNeeded = {},
            onNavigateToSongVersionsBySongId = {},
            onNavigateBack = {},
            onNavigateToSearch = {},
            onTabSearchBarQueryChange = {},
            onNavigateToTabByTabId = {}
        )
    }
}


//#endregion

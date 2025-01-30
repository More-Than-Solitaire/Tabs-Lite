package com.gbros.tabslite.view.homescreen

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.songlist.ISongListViewState
import com.gbros.tabslite.view.songlist.SongListView
import com.gbros.tabslite.view.songlist.SortBy
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.HomeViewModel

private const val LOG_NAME = "tabslite.HomeScreen    "

const val HOME_ROUTE = "home"

fun NavController.popUpToHome() {
    if (!popBackStack(route = HOME_ROUTE, inclusive = false)) {
        // fallback if HOME_ROUTE wasn't on the back stack
        navigate(HOME_ROUTE)
    }
}

fun NavGraphBuilder.homeScreen(
    onNavigateToSearch: (String) -> Unit,
    onNavigateToPlaylistEntry: (Int) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onNavigateToPlaylist: (Int) -> Unit
) {
    composable(HOME_ROUTE) {
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: HomeViewModel = hiltViewModel<HomeViewModel, HomeViewModel.HomeViewModelFactory> { factory -> factory.create(dataAccess = db.dataAccess()) }
        HomeScreen(
            viewState = viewModel,
            favoriteSongListViewState = viewModel.favoriteSongListViewModel,
            onFavoriteSongListSortByChange = viewModel.favoriteSongListViewModel::onSortSelectionChange,
            popularSongListViewState = viewModel.popularSongListViewModel,
            onPopularSongListSortByChange = viewModel.popularSongListViewModel::onSortSelectionChange,
            tabSearchBarViewState = viewModel.tabSearchBarViewModel,
            onTabSearchBarQueryChange = viewModel.tabSearchBarViewModel::onQueryChange,
            onNavigateToSearch = onNavigateToSearch,
            onExportPlaylists = viewModel::exportPlaylists,
            onImportPlaylists = viewModel::importPlaylists,
            onCreatePlaylist = viewModel::createPlaylist,
            navigateToTabByPlaylistEntryId = onNavigateToPlaylistEntry,
            navigateToPlaylistById = onNavigateToPlaylist,
            navigateToTabByTabId = onNavigateToTab
        )
    }
}

@Composable
fun HomeScreen(
    viewState: IHomeViewState,
    favoriteSongListViewState: ISongListViewState,
    onFavoriteSongListSortByChange: (SortBy) -> Unit,
    popularSongListViewState: ISongListViewState,
    onPopularSongListSortByChange: (SortBy) -> Unit,
    tabSearchBarViewState: ITabSearchBarViewState,
    onTabSearchBarQueryChange: (query: String) -> Unit,
    onNavigateToSearch: (query: String) -> Unit,
    onExportPlaylists: (destinationFile: Uri, contentResolver: ContentResolver) -> Unit,
    onImportPlaylists: (sourceFile: Uri, contentResolver: ContentResolver) -> Unit,
    onCreatePlaylist: (title: String, description: String) -> Unit,
    navigateToTabByPlaylistEntryId: (id: Int) -> Unit,
    navigateToTabByTabId: (id: Int) -> Unit,
    navigateToPlaylistById: (id: Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var pagerNav by remember { mutableIntStateOf(-1) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val contentResolver = LocalContext.current.contentResolver

    // handle playlist data export
    val exportDataFilePickerActivityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            onExportPlaylists(result.data!!.data!!, contentResolver)
        } else {
            Log.w(LOG_NAME, "Export playlists clicked, but no file chosen.")
        }
    }

    // handle playlist data import
    val importPlaylistsPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { fileToImport ->
        if (fileToImport != null) {
            onImportPlaylists(fileToImport, contentResolver)
        } else {
            Log.w(LOG_NAME, "Import Playlists clicked, but fileToImport is null")
        }
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismissRequest = { showAboutDialog = false },
            onExportPlaylistsClicked = {
                showAboutDialog = false

                // launch a file picker to find where to export the playlist data to
                val filePickerEvent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "tabslite_backup.json")
                }
                exportDataFilePickerActivityLauncher.launch(filePickerEvent)
            },
            onImportPlaylistsClicked = {
                showAboutDialog = false

                // launch a file picker to choose the file to import
                importPlaylistsPickerLauncher.launch("application/json")
            }
        )
    }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            TabsSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                leadingIcon = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Box(modifier = Modifier) {
                            val importProgress = viewState.playlistImportProgress.observeAsState(0f)
                            CircularProgressIndicator(progress = { importProgress.value })
                        }
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                viewState = tabSearchBarViewState,
                onSearch = onNavigateToSearch,
                onQueryChange = onTabSearchBarQueryChange
            )
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Unspecified
            ) {
                TabRowItem(
                    selected = pagerState.currentPage == 0,
                    inactiveIcon = Icons.Default.FavoriteBorder,
                    activeIcon = Icons.Filled.Favorite,
                    title = stringResource(id = R.string.title_favorites_playlist)
                ) {
                    pagerNav = if (pagerNav != 0) 0 else -1
                }
                TabRowItem(
                    selected = pagerState.currentPage == 1,
                    inactiveIcon = Icons.Outlined.Person,
                    activeIcon = Icons.Filled.Person,
                    title = stringResource(id = R.string.title_popular_playlist)
                ) {
                    pagerNav = if (pagerNav != 1) 1 else -1
                }
                TabRowItem(
                    selected = pagerState.currentPage == 2,
                    inactiveIcon = ImageVector.vectorResource(R.drawable.ic_playlist_play_light),
                    activeIcon = ImageVector.vectorResource(R.drawable.ic_playlist_play),
                    title = stringResource(id = R.string.title_playlists_page)
                ) {
                    pagerNav = if (pagerNav != 2) 2 else -1
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            beyondViewportPageCount = 3,
            contentPadding = PaddingValues(top = 8.dp, bottom = 0.dp, start = 8.dp, end = 8.dp),
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxHeight()
        ) { page ->
            when (page) {
                // Favorites page
                0 -> SongListView(
                    viewState = favoriteSongListViewState,
                    emptyListText = stringResource(R.string.empty_favorites),
                    onSortSelectionChange = onFavoriteSongListSortByChange,
                    navigateToTabById = navigateToTabByTabId,
                    navigateByPlaylistEntryId = false,
                )

                // Popular page
                1 -> SongListView(
                    viewState = popularSongListViewState,
                    emptyListText = stringResource(R.string.empty_popular),
                    onSortSelectionChange = onPopularSongListSortByChange,
                    navigateToTabById = navigateToTabByPlaylistEntryId,
                    navigateByPlaylistEntryId = true,
                )

                // Playlists page
                2 -> PlaylistListView(
                    livePlaylists = viewState.playlists,
                    onCreatePlaylist = onCreatePlaylist,
                    navigateToPlaylistById = navigateToPlaylistById
                )
            }
        }
    }

    // scroll to page when that page's tab is clicked
    LaunchedEffect(pagerNav) {
        if (pagerNav >= 0 && pagerNav != pagerState.currentPage) {
            pagerState.animateScrollToPage(pagerNav)
        }
        pagerNav = -1
    }
}

@Composable
fun TabRowItem(selected: Boolean, inactiveIcon: ImageVector, activeIcon: ImageVector, title: String, onClick: () -> Unit) {
    Tab(
        icon = { Icon(imageVector =  if(selected) activeIcon else inactiveIcon, null) },
        text = { Text(title) },
        selected = selected,
        onClick = onClick
    )
}

//#region preview / classes for test

@Composable @Preview
private fun HomeScreenPreview() {
    val viewState = HomeViewStateForTest(
        playlistImportState = MutableLiveData(LoadingState.Loading),
        playlistImportProgress = MutableLiveData(0.6f),
        playlists = MutableLiveData(listOf())
    )

    val songListState = SongListViewStateForTest(
        songs = MutableLiveData(listOf()),
        sortBy = MutableLiveData(SortBy.DateAdded)
    )

    val tabSearchBarViewState = TabSearchBarViewStateForTest(
        query = MutableLiveData(""),
        searchSuggestions = MutableLiveData(listOf())
    )

    AppTheme {
        HomeScreen(
            viewState = viewState,
            favoriteSongListViewState = songListState,
            onFavoriteSongListSortByChange = {},
            popularSongListViewState = songListState,
            onPopularSongListSortByChange = {},
            tabSearchBarViewState = tabSearchBarViewState,
            onTabSearchBarQueryChange = {},
            onNavigateToSearch = {},
            onExportPlaylists = {_,_->},
            onImportPlaylists = {_,_->},
            onCreatePlaylist = {_,_->},
            navigateToTabByPlaylistEntryId = {},
            navigateToTabByTabId = {},
            navigateToPlaylistById = {}
        )
    }
}

private class HomeViewStateForTest(
    override val playlistImportProgress: LiveData<Float>,
    override val playlistImportState: LiveData<LoadingState>,
    override val playlists: LiveData<List<Playlist>>
) : IHomeViewState

private class SongListViewStateForTest(
    override val songs: LiveData<List<TabWithDataPlaylistEntry>>,
    override val sortBy: LiveData<SortBy>
) : ISongListViewState

private class TabSearchBarViewStateForTest(
    override val query: LiveData<String>,
    override val searchSuggestions: LiveData<List<String>>
) : ITabSearchBarViewState

//#endregion
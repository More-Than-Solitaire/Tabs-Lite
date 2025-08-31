package com.gbros.tabslite.view.homescreen

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.playlists.PlaylistsSortBy
import com.gbros.tabslite.view.songlist.ISongListViewState
import com.gbros.tabslite.view.songlist.SongListView
import com.gbros.tabslite.view.songlist.SortBy
import com.gbros.tabslite.view.songlist.SortByDropdown
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.HomeViewModel


const val HOME_ROUTE = "home"

fun NavController.popUpToHome() {
    if (!popBackStack(route = HOME_ROUTE, inclusive = false)) {
        // fallback if HOME_ROUTE wasn't on the back stack
        navigate(HOME_ROUTE)
    }
}

fun NavGraphBuilder.homeScreen(
    onNavigateToSearch: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onNavigateToPlaylist: (Int) -> Unit
) {
    composable(HOME_ROUTE) {
        val context = LocalContext.current.applicationContext
        val db = AppDatabase.getInstance(context)
        val viewModel: HomeViewModel = hiltViewModel<HomeViewModel, HomeViewModel.HomeViewModelFactory> { factory -> factory.create(dataAccess = db.dataAccess(), context = context) }
        HomeScreen(
            viewState = viewModel,
            favoriteSongListViewState = viewModel.favoriteSongListViewModel,
            onFavoriteSongListSortByChange = viewModel.favoriteSongListViewModel::onSortSelectionChange,
            popularSongListViewState = viewModel.popularSongListViewModel,
            onPopularSongListSortByChange = viewModel.popularSongListViewModel::onSortSelectionChange,
            onPlaylistsSortByChange = viewModel::sortPlaylists,
            tabSearchBarViewState = viewModel.tabSearchBarViewModel,
            onTabSearchBarQueryChange = viewModel.tabSearchBarViewModel::onQueryChange,
            onNavigateToSearch = onNavigateToSearch,
            onExportPlaylists = viewModel::exportPlaylists,
            onImportPlaylists = viewModel::importPlaylists,
            onCreatePlaylist = viewModel::createPlaylist,
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
    onPlaylistsSortByChange: (PlaylistsSortBy) -> Unit,
    tabSearchBarViewState: ITabSearchBarViewState,
    onTabSearchBarQueryChange: (query: String) -> Unit,
    onNavigateToSearch: (query: String) -> Unit,
    onExportPlaylists: (destinationFile: Uri, contentResolver: ContentResolver) -> Unit,
    onImportPlaylists: (sourceFile: Uri, contentResolver: ContentResolver) -> Unit,
    onCreatePlaylist: (title: String, description: String) -> Unit,
    navigateToTabByTabId: (id: Int) -> Unit,
    navigateToPlaylistById: (id: Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val secondaryPagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scrollingFollowingPair by remember {  // handle the sort by dropdown being in a separate pager
        derivedStateOf {
            if (pagerState.isScrollInProgress) {
                pagerState to secondaryPagerState
            } else if (secondaryPagerState.isScrollInProgress) {
                secondaryPagerState to pagerState
            } else null
        }
    }
    var pagerNav by remember { mutableIntStateOf(-1) }
    
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // handle playlist data export
    val exportDataFilePickerActivityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data?.data != null) {
            // Save export details to SharedPreferences
            val sharedPreferences = context.getSharedPreferences("export_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString("last_export_file_name", "tabslite_backup.json")
                putString("last_export_uri", result.data!!.data!!.toString())
                putLong("last_export_time", System.currentTimeMillis())
                apply()
            }
            onExportPlaylists(result.data!!.data!!, contentResolver)
        } // else: user cancelled the action
    }

    // handle playlist data import
    val importPlaylistsPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { fileToImport ->
        if (fileToImport != null) {
            onImportPlaylists(fileToImport, contentResolver)
        } // else: user cancelled the action
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismissRequest = { showAboutDialog = false },
            onExportPlaylistsClicked = {
                showAboutDialog = false

                // https://developer.android.com/training/data-storage/shared/documents-files
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


    val content = @Composable {
        val columnModifier = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Modifier
                .fillMaxWidth(0.4f)
        } else {
            Modifier
        }

        Column(
            modifier = columnModifier
        ) {
            TabsSearchBar(
                modifier = Modifier
                    .fillMaxWidth(),
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
                onQueryChange = onTabSearchBarQueryChange,
                onNavigateToTabById = navigateToTabByTabId
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

            // Sort By dropdowns
            HorizontalPager(
                state = secondaryPagerState,
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 3,
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp),
                pageSpacing = 8.dp,
                modifier = Modifier
            ) { page ->
                when (page) {
                    // Favorites page
                    0 -> SortByDropdown(
                        selectedSort = favoriteSongListViewState.sortBy.observeAsState().value,
                        onOptionSelected = onFavoriteSongListSortByChange
                    )

                    // Popular page
                    1 -> SortByDropdown(
                        selectedSort = popularSongListViewState.sortBy.observeAsState().value,
                        onOptionSelected = onPopularSongListSortByChange
                    )

                    // Playlists page
                    2 -> SortByDropdown(
                        selectedSort = viewState.playlistsSortBy.observeAsState().value,
                        onOptionSelected = onPlaylistsSortByChange
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            beyondViewportPageCount = 3,
            contentPadding = PaddingValues(horizontal = 8.dp),
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxHeight()
        ) { page ->
            when (page) {
                // Favorites page
                0 -> SongListView(
                    viewState = favoriteSongListViewState,
                    emptyListText = stringResource(R.string.empty_favorites),
                    navigateToTabById = navigateToTabByTabId,
                    navigateByPlaylistEntryId = false,
                )

                // Popular page
                1 -> SongListView(
                    viewState = popularSongListViewState,
                    emptyListText = stringResource(R.string.empty_popular),
                    navigateToTabById = navigateToTabByTabId,
                    navigateByPlaylistEntryId = false,  // can't navigate by playlisty entry because the playlist entries get cleared and refreshed each time the activity starts (e.g. when device is rotated or dark mode is enabled)
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

    // adjust view based on device orientation
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        Row (
            modifier = Modifier
                .windowInsetsPadding(WindowInsets(
                    left = WindowInsets.safeDrawing.getLeft(LocalDensity.current, LocalLayoutDirection.current),
                    right = WindowInsets.safeDrawing.getRight(LocalDensity.current, LocalLayoutDirection.current)
                )),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                content()
            }
        )
    } else {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets(
                    left = WindowInsets.safeDrawing.getLeft(LocalDensity.current, LocalLayoutDirection.current),
                    right = WindowInsets.safeDrawing.getRight(LocalDensity.current, LocalLayoutDirection.current),
                    top = WindowInsets.safeDrawing.getTop(LocalDensity.current)
                )),
            content = {
                content()
            }
        )
    }

    // scroll to page when that page's tab is clicked
    LaunchedEffect(pagerNav) {
        if (pagerNav >= 0 && pagerNav != pagerState.currentPage) {
            pagerState.animateScrollToPage(pagerNav)
        }
        pagerNav = -1
    }

    // sync secondary horizontal pager for sort by dropdown to primary (and vice versa)
    LaunchedEffect(scrollingFollowingPair) {
        val (scrollingState, followingState) = scrollingFollowingPair ?: return@LaunchedEffect
        snapshotFlow { Pair(scrollingState.currentPage, scrollingState.currentPageOffsetFraction) }
            .collect { (currentPage, currentPageOffsetFraction) ->
                followingState.scrollToPage(
                    page = currentPage,
                    pageOffsetFraction = currentPageOffsetFraction
                )
            }
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

@Preview(
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Preview
@Composable
private fun HomeScreenPreview() {
    val viewState = HomeViewStateForTest(
        playlistImportState = MutableLiveData(LoadingState.Loading),
        playlistImportProgress = MutableLiveData(0.6f),
        playlists = MutableLiveData(listOf()),
        playlistsSortBy = MutableLiveData(PlaylistsSortBy.Name)
    )

    val songListState = SongListViewStateForTest(
        songs = MutableLiveData(listOf()),
        sortBy = MutableLiveData(SortBy.DateAdded)
    )

    val tabSearchBarViewState = TabSearchBarViewStateForTest(
        query = MutableLiveData(""),
        searchSuggestions = MutableLiveData(listOf()),
        tabSuggestions = MutableLiveData(listOf()),
        loadingState = MutableLiveData(LoadingState.Loading)
    )

    AppTheme {
        HomeScreen(
            viewState = viewState,
            favoriteSongListViewState = songListState,
            onFavoriteSongListSortByChange = {},
            popularSongListViewState = songListState,
            onPopularSongListSortByChange = {},
            onPlaylistsSortByChange = {},
            tabSearchBarViewState = tabSearchBarViewState,
            onTabSearchBarQueryChange = {},
            onNavigateToSearch = {},
            onExportPlaylists = {_,_->},
            onImportPlaylists = {_,_->},
            onCreatePlaylist = {_,_->},
            navigateToTabByTabId = {},
            navigateToPlaylistById = {}
        )
    }
}

private class HomeViewStateForTest(
    override val playlistImportProgress: LiveData<Float>,
    override val playlistImportState: LiveData<LoadingState>,
    override val playlists: LiveData<List<Playlist>>,
    override val playlistsSortBy: LiveData<PlaylistsSortBy>
) : IHomeViewState

private class SongListViewStateForTest(
    override val songs: LiveData<List<TabWithDataPlaylistEntry>>,
    override val sortBy: LiveData<SortBy>
) : ISongListViewState

private class TabSearchBarViewStateForTest(
    override val query: LiveData<String>,
    override val searchSuggestions: LiveData<List<String>>,
    override val tabSuggestions: LiveData<List<ITab>>,
    override val loadingState: LiveData<LoadingState>
) : ITabSearchBarViewState

//#endregion
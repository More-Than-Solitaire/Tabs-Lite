package com.gbros.tabslite.view.homescreen

import android.app.Activity.RESULT_OK
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.playlist.PlaylistFileExportType
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.songlist.SongListView
import com.gbros.tabslite.view.songlist.SortBy
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.TabSearchBarViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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
        HomeScreen(
            onSearch = onNavigateToSearch,
            navigateToTabByPlaylistEntryId = onNavigateToPlaylistEntry,
            navigateToPlaylistById = onNavigateToPlaylist,
            navigateToTabByTabId = onNavigateToTab
        )
    }
}

@Composable
fun HomeScreen(
    onSearch: (query: String) -> Unit,
    navigateToTabByPlaylistEntryId: (id: Int) -> Unit,
    navigateToTabByTabId: (id: Int) -> Unit,
    navigateToPlaylistById: (id: Int) -> Unit
) {
    val currentContext = LocalContext.current
    val dataAccess = AppDatabase.getInstance(LocalContext.current).dataAccess()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var pagerNav by remember { mutableIntStateOf(-1) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val contentResolver = currentContext.contentResolver

    var playlistImportExportProgress by remember { mutableFloatStateOf(0f) }
    // handle playlist data export
    var destinationForPlaylistExport: Uri? by remember { mutableStateOf(null) }
    val exportDataFilePickerActivityLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            destinationForPlaylistExport = result.data!!.data
        } else {
            Log.w(LOG_NAME, "Import playlists clicked, but no file chosen.")
        }
    }

    // handle playlist data import
    var fileToImportPlaylistData: Uri? by remember { mutableStateOf(null) }
    val importPlaylistsPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {fileToImport ->
        fileToImportPlaylistData = fileToImport
    }
    val tabSearchBarViewModel = TabSearchBarViewModel(
        initialQuery = "",
        leadingIcon = {
            IconButton(onClick = { showAboutDialog = true }) {
                Box(modifier = Modifier) {
                    CircularProgressIndicator(progress = { playlistImportExportProgress })
                }
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dataAccess = dataAccess
    )

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
                onSearch = onSearch,
                viewState = tabSearchBarViewModel,
                onQueryChange = tabSearchBarViewModel::onQueryChange
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
                0 -> SongListView(liveSongs = dataAccess.getFavoriteTabs(), navigateToTabById = navigateToTabByTabId, navigateByPlaylistEntryId = false, defaultSortValue = SortBy.DateAdded,
                    liveSortByPreference = dataAccess.getLivePreference(Preference.FAVORITES_SORT),
                    onSortPreferenceChange = { launch { dataAccess.upsert(it) } },
                    emptyListText = stringResource(R.string.empty_favorites))

                // Popular page
                1 -> SongListView(liveSongs = dataAccess.getPopularTabs(), navigateToTabById = navigateToTabByPlaylistEntryId, navigateByPlaylistEntryId = true, defaultSortValue = SortBy.Popularity,
                    liveSortByPreference = dataAccess.getLivePreference(Preference.POPULAR_SORT),
                    onSortPreferenceChange = { launch { dataAccess.upsert(it) } },
                    emptyListText = stringResource(R.string.empty_popular))

                // Playlists page
                2 -> PlaylistPage(livePlaylists = dataAccess.getLivePlaylists(), navigateToPlaylistById = navigateToPlaylistById)
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

    // export playlists if a filename is chosen to export playlists to
    LaunchedEffect(key1 = destinationForPlaylistExport) {
        if (destinationForPlaylistExport != null) {
            playlistImportExportProgress = 0.2f
            val allUserPlaylists = dataAccess.getPlaylists().filter { playlist -> playlist.playlistId != Playlist.TOP_TABS_PLAYLIST_ID }
            val allPlaylists = mutableListOf(Playlist(-1, false, currentContext.getString(R.string.title_favorites_playlist), 0, 0, "")) // add the Favorites playlist
            allPlaylists.addAll(allUserPlaylists)
            playlistImportExportProgress = 0.6f
            val allSelfContainedPlaylists = dataAccess.getSelfContainedPlaylists(allPlaylists)
            val playlistsAndEntries = Json.encodeToString(PlaylistFileExportType(playlists = allSelfContainedPlaylists))
            playlistImportExportProgress = 0.8f

            contentResolver.openOutputStream(destinationForPlaylistExport!!).use { outputStream ->
                outputStream?.write(playlistsAndEntries.toByteArray())
                outputStream?.flush()
            }

            playlistImportExportProgress = 1f
            delay(700)
        }

        // reset for next export
        playlistImportExportProgress = 0f
        destinationForPlaylistExport = null
    }

    // import playlists when a file is chosen to import playlists from
    LaunchedEffect(key1 = fileToImportPlaylistData) {
        val fileToImport = fileToImportPlaylistData
        var dataToImport: String? = null
        if (fileToImport != null) {
            playlistImportExportProgress = .05f
            // read file
            contentResolver.openInputStream(fileToImport).use {
                dataToImport = it?.reader()?.readText()
            }
        }

        if (!dataToImport.isNullOrBlank()) {
            val importedData = Json.decodeFromString<PlaylistFileExportType>(dataToImport!!)
            // import all playlists (except Favorites and Top Tabs)
            val totalEntriesToImport = importedData.playlists.sumOf { pl -> pl.entries.size }.toFloat()
            var progressFromPreviouslyImportedPlaylists = 0f  // track the amount of progress used by previous playlists, used to add current progress to
            for (playlist in importedData.playlists.filter { pl -> pl.playlistId != Playlist.TOP_TABS_PLAYLIST_ID }) {
                val progressForThisPlaylist = playlist.entries.size.toFloat() / totalEntriesToImport  // available portion of 100% to use for this playlist
                playlist.importToDatabase(dataAccess = dataAccess, onProgressChange = { progress ->
                    playlistImportExportProgress = progressFromPreviouslyImportedPlaylists + (progress * progressForThisPlaylist)
                })
                progressFromPreviouslyImportedPlaylists += progressForThisPlaylist
            }

            // pause at 100% progress for a second before setting progress to 0
            playlistImportExportProgress = 1f
            delay(700)
        }

        // reset for next import
        playlistImportExportProgress = 0f
        fileToImportPlaylistData = null
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

@Composable @Preview
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen({}, {}, {}, {})
    }
}


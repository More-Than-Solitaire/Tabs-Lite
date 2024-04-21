package com.gbros.tabslite.compose.homescreen

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.compose.songlist.SongListView
import com.gbros.tabslite.compose.songlist.SortBy
import com.gbros.tabslite.compose.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.PlaylistEntry
import com.gbros.tabslite.data.PlaylistFileExportType
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.ui.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val LOG_NAME = "tabslite.HomeScreen    "

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onSearch: (query: String) -> Unit,
    navigateToTabByPlaylistEntryId: (id: Int) -> Unit,
    navigateToTabByTabId: (id: Int) -> Unit,
    navigateToPlaylistById: (id: Int) -> Unit
) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    var pagerNav by remember { mutableIntStateOf(-1) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val contentResolver = currentContext.contentResolver

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
                leadingIcon = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Unspecified
            ) {
                TabRowItem(
                    selected = pagerState.currentPage == 0,
                    inactiveIcon = Icons.Default.FavoriteBorder,
                    activeIcon = Icons.Filled.Favorite,
                    title = "Favorites"
                ) {
                    pagerNav = if (pagerNav != 0) 0 else -1
                }
                TabRowItem(
                    selected = pagerState.currentPage == 1,
                    inactiveIcon = Icons.Outlined.Person,
                    activeIcon = Icons.Filled.Person,
                    title = "Popular"
                ) {
                    pagerNav = if (pagerNav != 1) 1 else -1
                }
                TabRowItem(
                    selected = pagerState.currentPage == 2,
                    inactiveIcon = ImageVector.vectorResource(R.drawable.ic_playlist_play_light),
                    activeIcon = ImageVector.vectorResource(R.drawable.ic_playlist_play),
                    title = "Playlists"
                ) {
                    pagerNav = if (pagerNav != 2) 2 else -1
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            beyondBoundsPageCount = 3,
            contentPadding = PaddingValues(top = 8.dp, bottom = 0.dp, start = 8.dp, end = 8.dp),
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxHeight()
        ) { page ->
            when (page) {
                // Favorites page
                0 -> SongListView(liveSongs = db.tabFullDao().getFavoriteTabs(), navigateToTabById = navigateToTabByTabId, navigateByPlaylistEntryId = false, defaultSortValue = SortBy.DateAdded,
                    liveSortByPreference = db.preferenceDao().getLivePreference(Preference.FAVORITES_SORT),
                    onSortPreferenceChange = { launch { db.preferenceDao().upsertPreference(it) } },
                    emptyListText = "Select the heart icon on any song to save it offline in this list.")

                // Popular page
                1 -> SongListView(liveSongs = db.tabFullDao().getPopularTabs(), navigateToTabById = navigateToTabByPlaylistEntryId, navigateByPlaylistEntryId = true, defaultSortValue = SortBy.Popularity,
                    liveSortByPreference = db.preferenceDao().getLivePreference(Preference.POPULAR_SORT),
                    onSortPreferenceChange = { launch { db.preferenceDao().upsertPreference(it) } },
                    emptyListText = "Today's popular songs will load when you're connected to the internet.")

                // Playlists page
                2 -> PlaylistPage(livePlaylists = db.playlistDao().getLivePlaylists(), navigateToPlaylistById = navigateToPlaylistById)
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
        if (destinationForPlaylistExport?.path != null) {
            val allPlaylists = db.playlistDao().getPlaylists().filter { playlist -> playlist.playlistId != Playlist.TOP_TABS_PLAYLIST_ID }
            val allPlaylistEntries = db.playlistEntryDao().getAllPlaylistEntries().filter { entry -> entry.playlistId != Playlist.TOP_TABS_PLAYLIST_ID }
            val playlistsAndEntries = Json.encodeToString(PlaylistFileExportType(playlists = allPlaylists, entries = allPlaylistEntries))

            contentResolver.openOutputStream(destinationForPlaylistExport!!).use { outputStream ->
                outputStream?.write(playlistsAndEntries.toByteArray())
                outputStream?.flush()
            }
        }

        // reset for next export
        destinationForPlaylistExport = null
    }

    // import playlists when a file is chosen to import playlists from
    LaunchedEffect(key1 = fileToImportPlaylistData) {
        val fileToImport = fileToImportPlaylistData
        var dataToImport: String? = null
        if (fileToImport != null && fileToImport.path != null) {
            // read file
            contentResolver.openInputStream(fileToImport).use {
                dataToImport = it?.reader()?.readText()
            }
        }

        if (!dataToImport.isNullOrBlank()) {
            val importedData = Json.decodeFromString<PlaylistFileExportType>(dataToImport!!)

            // import all playlists (except Favorites and Top Tabs)
            val playlistIdMap = HashMap<Int, Int>(importedData.playlists.size)  // importing the playlists will give them new IDs; track these new IDs so they can be mapped for playlist entries
            playlistIdMap[Playlist.FAVORITES_PLAYLIST_ID] = Playlist.FAVORITES_PLAYLIST_ID  // include the favorites playlist so that favorite tabs are imported
            for (playlist in importedData.playlists) {
                if (playlist.playlistId == Playlist.FAVORITES_PLAYLIST_ID || playlist.playlistId == Playlist.TOP_TABS_PLAYLIST_ID)
                    continue

                val newPlaylistID = db.playlistDao().savePlaylist(
                    Playlist(userCreated = playlist.userCreated, title = playlist.title, dateCreated = playlist.dateCreated, dateModified = playlist.dateModified, description = playlist.description)
                )
                playlistIdMap[playlist.playlistId] = newPlaylistID.toInt()
            }

            // get current favorite tabs (to not reimport tabs that are already favorite tabs)
            val currentFavorites = db.playlistEntryDao().getAllEntriesInPlaylist(Playlist.FAVORITES_PLAYLIST_ID)

            // update playlist IDs to the new playlist IDs mapped above
            val playlistEntriesWithUpdatedPlaylistIds = importedData.entries.mapNotNull { entry ->
                if (playlistIdMap.containsKey(entry.playlistId) && (entry.playlistId != Playlist.FAVORITES_PLAYLIST_ID || currentFavorites.all { fav -> fav.tabId != entry.tabId }))  // don't reimport existing favorites or orphaned playlist entries
                    playlistIdMap[entry.playlistId]?.let { PlaylistEntry(playlistId = it, tabId = entry.tabId, next_entry_id = entry.nextEntryId, prev_entry_id = entry.prevEntryId, dateAdded = entry.dateAdded, transpose = entry.transpose) }
                else
                    null
            }

            // import all playlist entries
            val entryIdMap = HashMap<Int, Int>(importedData.entries.size)  // importing the playlist entries will give them new IDs; track these to be able to update the linked list ordering for playlists
            for (entry in playlistEntriesWithUpdatedPlaylistIds) {
                entryIdMap[entry.entryId] = db.playlistEntryDao().insert(entry = entry).toInt()  // insert and save the new entry ID
            }

            // now that we have all our updated entry IDs, loop through and fix all the prev and next mappings
            for (entry in playlistEntriesWithUpdatedPlaylistIds) {
                if (entry.nextEntryId != null) {
                    if (!entryIdMap.containsKey(entry.nextEntryId)) {
                        Log.e(LOG_NAME, "Playlist entry ${entryIdMap[entry.entryId]}'s next entry ID couldn't be found. There will be unreferenced playlist entries in this playlist. Playlist ${entry.playlistId} linked list is now broken.")
                    }
                    db.playlistEntryDao().setNextEntryId(entryIdMap[entry.entryId], entryIdMap[entry.nextEntryId])
                }

                if (entry.prevEntryId != null) {
                    if (!entryIdMap.containsKey(entry.prevEntryId)) {
                        Log.e(LOG_NAME, "Playlist entry ${entryIdMap[entry.entryId]}'s previous entry ID couldn't be found. There will be unreferenced playlist entries in this playlist. Playlist ${entry.playlistId} linked list is now broken.")
                    }
                    db.playlistEntryDao().setPrevEntryId(entryIdMap[entry.entryId], entryIdMap[entry.prevEntryId])
                }
            }
        }

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


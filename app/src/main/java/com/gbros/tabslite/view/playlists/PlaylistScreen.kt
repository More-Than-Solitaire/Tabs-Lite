package com.gbros.tabslite.view.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.playlist.IDataPlaylistEntry
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.viewmodel.PlaylistViewModel

private const val PLAYLIST_NAV_ARG = "playlistId"
private const val PLAYLIST_DETAIL_ROUTE_TEMPLATE = "playlist/%s"

fun NavController.navigateToPlaylistDetail(playlistId: Int) {
    navigate(PLAYLIST_DETAIL_ROUTE_TEMPLATE.format(playlistId.toString()))
}

fun NavGraphBuilder.playlistDetailScreen(
    onNavigateToTabByPlaylistEntryId: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        route = PLAYLIST_DETAIL_ROUTE_TEMPLATE.format("{$PLAYLIST_NAV_ARG}"),
        arguments = listOf(navArgument(PLAYLIST_NAV_ARG) { type = NavType.IntType })
    ) { navBackStackEntry ->
        val playlistId = navBackStackEntry.arguments!!.getInt(PLAYLIST_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: PlaylistViewModel = hiltViewModel<PlaylistViewModel, PlaylistViewModel.PlaylistViewModelFactory> { factory -> factory.create(playlistId, db.dataAccess()) }

        PlaylistScreen(
            viewState = viewModel,
            titleChanged = viewModel::titleChanged,
            descriptionChanged = viewModel::descriptionChanged,
            entryMoved = viewModel::reorderPlaylistEntry,
            entryRemoved = viewModel::entryRemoved,
            playlistDeleted = viewModel::playlistDeleted,
            navigateToTabByPlaylistEntryId = onNavigateToTabByPlaylistEntryId,
            navigateBack = onNavigateBack
        )
    }
}

@Composable
fun PlaylistScreen(
    viewState: IPlaylistViewState,
    titleChanged: (newTitle: String) -> Unit,
    descriptionChanged: (newDescription: String) -> Unit,
    entryMoved: (fromIndex: Int, toIndex: Int) -> Unit,
    entryRemoved: (entry: IDataPlaylistEntry) -> Unit,
    playlistDeleted: () -> Unit,
    navigateToTabByPlaylistEntryId: (Int) -> Unit,
    navigateBack: () -> Unit
) {
    var deletePlaylistConfirmationDialogShowing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
    ) {
        PlaylistHeader(
            title = viewState.title,
            description = viewState.description,
            titleChanged = titleChanged,
            descriptionChanged = descriptionChanged,
            navigateBack = navigateBack,
            deletePlaylist = {
                deletePlaylistConfirmationDialogShowing = true
            }
        )

        PlaylistSongList(
            songs = viewState.songs.observeAsState(listOf()).value,
            navigateToTabByPlaylistEntryId = {entryId ->
                focusManager.clearFocus()  // this will trigger saving the playlist title and description if changed
                navigateToTabByPlaylistEntryId(entryId)
            },
            onReorder = entryMoved,
            onRemove = entryRemoved
        )
    }

    if (deletePlaylistConfirmationDialogShowing) {
        DeletePlaylistConfirmationDialog(
            onConfirm = { deletePlaylistConfirmationDialogShowing = false; playlistDeleted(); navigateBack() },
            onDismiss = { deletePlaylistConfirmationDialogShowing = false }
        )
    }

    BackHandler {
        navigateBack()
    }
}

@Composable @Preview
private fun PlaylistViewPreview() {
    AppTheme {
        val playlistForTest = MutableLiveData(Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text."))

        val playlistState = PlaylistViewStateForTest(
            title = MutableLiveData("Playlist title"),
            description = MutableLiveData("Playlist description"),
            songs = MutableLiveData(createListOfTabWithPlaylistEntry(3))
        )

        PlaylistScreen(
            viewState = playlistState,
            navigateToTabByPlaylistEntryId = {},
            titleChanged = {},
            descriptionChanged = {},
            entryMoved = {_, _ -> },
            entryRemoved = {},
            navigateBack = {},
            playlistDeleted = {}
        )
    }
}

private class PlaylistViewStateForTest(
    override val title: LiveData<String>,
    override val description: LiveData<String>,
    override val songs: LiveData<List<TabWithDataPlaylistEntry>>
) : IPlaylistViewState

private fun createListOfTabWithPlaylistEntry(size: Int): List<TabWithDataPlaylistEntry> {
    val listOfEntries = mutableListOf<TabWithDataPlaylistEntry>()
    for (id in 0..size) {
        listOfEntries.add(
            TabWithDataPlaylistEntry(entryId = id, playlistId = 1, tabId = (id * 20).toString(), nextEntryId = if(id<size) id+1 else null,
            prevEntryId = if(id>0) id-1 else null, dateAdded = 0, songId = "12", songName = "Song $id", artistName ="Artist name",
            isVerified = false, versionsCount = 4, type = "Chords", part = "part", version = 2, votes = 0,
            rating = 0.0, date = 0, status = "", tabAccessType = "public",
            tonalityName = "D", versionDescription = "version desc", recommended = arrayListOf(),
            playlistUserCreated = false, playlistTitle = "playlist title", playlistDateCreated = 0, playlistDescription = "playlist desc",
            playlistDateModified = 0)
        )
    }

    return listOfEntries
}

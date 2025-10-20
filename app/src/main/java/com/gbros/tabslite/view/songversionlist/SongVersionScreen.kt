package com.gbros.tabslite.view.songversionlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.view.tabsearchbar.ITabSearchBarViewState
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.viewmodel.SongVersionViewModel

private const val SONG_VERSION_NAV_ARG = "songId"
const val SONG_VERSION_ROUTE_TEMPLATE = "song/%s"

fun NavController.navigateToSongVersion(songId: Int) {
    navigate(SONG_VERSION_ROUTE_TEMPLATE.format(songId.toString()))
}

fun NavGraphBuilder.songVersionScreen(
    onNavigateToTabByTabId: (Int) -> Unit,
    onNavigateToTabByPlaylistEntryId: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        SONG_VERSION_ROUTE_TEMPLATE.format("{$SONG_VERSION_NAV_ARG}"),
        arguments = listOf(navArgument(SONG_VERSION_NAV_ARG) { type = NavType.IntType })
    ) { navBackStackEntry ->
        val songId = navBackStackEntry.arguments!!.getInt(SONG_VERSION_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: SongVersionViewModel = hiltViewModel<SongVersionViewModel, SongVersionViewModel.SongVersionViewModelFactory> { factory -> factory.create(songId, db.dataAccess()) }

        SongVersionScreen(
            viewState = viewModel,
            tabSearchBarViewState = viewModel.tabSearchBarViewModel,
            onTabSearchBarQueryChange = viewModel.tabSearchBarViewModel::onQueryChange,
            onNavigateToTabByTabId = onNavigateToTabByTabId,
            onNavigateBack = onNavigateBack,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToTabByPlaylistEntryId = onNavigateToTabByPlaylistEntryId
        )
    }
}

@Composable
fun SongVersionScreen(
    viewState: ISongVersionViewState,
    tabSearchBarViewState: ITabSearchBarViewState,
    onTabSearchBarQueryChange: (newQuery: String) -> Unit,
    onNavigateToTabByTabId: (id: Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (query: String) -> Unit,
    onNavigateToTabByPlaylistEntryId: (id: Int) -> Unit
) {
    val songVersions = viewState.songVersions.observeAsState(listOf()).value.sortedByDescending { song -> song.votes }

    Column(
        modifier = Modifier
            .fillMaxHeight()
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
            onNavigateToTabById = onNavigateToTabByTabId,
            onNavigateToPlaylistEntryById = onNavigateToTabByPlaylistEntryId
        )

        SongVersionList(songVersions = songVersions, navigateToTabByTabId = onNavigateToTabByTabId)
    }
    
    BackHandler {
        onNavigateBack()
    }
}
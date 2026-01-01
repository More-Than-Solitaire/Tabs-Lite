package com.gbros.tabslite

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.gbros.tabslite.view.createtab.createTabContentScreen
import com.gbros.tabslite.view.createtab.createTabScreen
import com.gbros.tabslite.view.createtab.navigateToCreateTabContent
import com.gbros.tabslite.view.createtab.navigateToCreateTab
import com.gbros.tabslite.view.homescreen.HOME_ROUTE
import com.gbros.tabslite.view.homescreen.homeScreen
import com.gbros.tabslite.view.homescreen.popUpToHome
import com.gbros.tabslite.view.playlists.navigateToPlaylistDetail
import com.gbros.tabslite.view.playlists.playlistDetailScreen
import com.gbros.tabslite.view.searchresultsonglist.listSongsByArtistIdScreen
import com.gbros.tabslite.view.searchresultsonglist.navigateToArtistIdSongList
import com.gbros.tabslite.view.searchresultsonglist.navigateToSearch
import com.gbros.tabslite.view.searchresultsonglist.navigateToSongSelection
import com.gbros.tabslite.view.searchresultsonglist.searchByTitleScreen
import com.gbros.tabslite.view.searchresultsonglist.selectSongByTitleScreen
import com.gbros.tabslite.view.songversionlist.navigateToSongVersion
import com.gbros.tabslite.view.songversionlist.songVersionScreen
import com.gbros.tabslite.view.tabview.navigateToPlaylistEntry
import com.gbros.tabslite.view.tabview.navigateToTab
import com.gbros.tabslite.view.tabview.playlistEntryScreen
import com.gbros.tabslite.view.tabview.swapToTab
import com.gbros.tabslite.view.tabview.tabScreen

/**
 * This nav graph is a collection of all pages in the app, and has the responsibility of passing nav
 * args between screens to keep each screen definition modular and decoupled
 */
@Composable
fun TabsLiteNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HOME_ROUTE) {
        homeScreen(
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateToTab = navController::navigateToTab,
            onNavigateToPlaylist = navController::navigateToPlaylistDetail,
            onNavigateToPlaylistEntry = navController::navigateToPlaylistEntry,
            onNavigateToCreateTab = navController::navigateToCreateTab
        )

        tabScreen (
            onNavigateBack = navController::popBackStack,
            onNavigateToArtistIdSongList = navController::navigateToArtistIdSongList,
            onNavigateToTabVersionById = navController::swapToTab
        )

        playlistEntryScreen (
            onNavigateToPlaylistEntry = navController::navigateToPlaylistEntry,
            onNavigateBack = navController::popBackStack,
            onNavigateToArtistIdSongList = navController::navigateToArtistIdSongList,
            onNavigateToTabVersionById = navController::swapToTab
        )

        playlistDetailScreen(
            onNavigateToTabByPlaylistEntryId = navController::navigateToPlaylistEntry,
            onNavigateBack = navController::popBackStack
        )

        searchByTitleScreen(
            onNavigateToSongId = navController::navigateToSongVersion,
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateToTabByTabId = navController::navigateToTab,
            onNavigateToPlaylistEntryByEntryId = navController::navigateToPlaylistEntry,
            onNavigateBack = navController::popUpToHome
        )

        listSongsByArtistIdScreen(
            onNavigateToSongId = navController::navigateToSongVersion,
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateToTabByTabId = navController::navigateToTab,
            onNavigateToPlaylistEntryByEntryId = navController::navigateToPlaylistEntry,
            onNavigateBack = navController::popUpToHome
        )

        selectSongByTitleScreen(
            onNavigateToSongId = navController::navigateToCreateTabContent,
            onNavigateToSearch = navController::navigateToSongSelection,
            onNavigateBack = navController::popUpToHome
        )

        songVersionScreen(
            onNavigateToTabByTabId = navController::navigateToTab,
            onNavigateToTabByPlaylistEntryId = navController::navigateToPlaylistEntry,
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateBack = navController::popBackStack
        )

        createTabScreen(
            onNavigateBack = navController::popBackStack,
            onNavigateToSongSelection = navController::navigateToSongSelection
        )

        createTabContentScreen(
            onNavigateToTabByTabId = navController::navigateToTab,
            onNavigateBack = navController::popBackStack
        )
    }
}

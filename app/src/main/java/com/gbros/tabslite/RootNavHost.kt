package com.gbros.tabslite

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.gbros.tabslite.view.homescreen.HOME_ROUTE
import com.gbros.tabslite.view.homescreen.HomeScreen
import com.gbros.tabslite.view.homescreen.homeScreen
import com.gbros.tabslite.view.homescreen.navigateToHome
import com.gbros.tabslite.view.playlists.PlaylistScreen
import com.gbros.tabslite.view.playlists.navigateToPlaylistDetail
import com.gbros.tabslite.view.playlists.playlistDetailScreen
import com.gbros.tabslite.view.searchresultsonglist.SearchScreen
import com.gbros.tabslite.view.searchresultsonglist.navigateToSearch
import com.gbros.tabslite.view.searchresultsonglist.searchScreen
import com.gbros.tabslite.view.songversionlist.SongVersionScreen
import com.gbros.tabslite.view.songversionlist.navigateToSongVersion
import com.gbros.tabslite.view.songversionlist.songVersionScreen
import com.gbros.tabslite.view.tabview.TabScreen
import com.gbros.tabslite.view.tabview.navigateToPlaylistEntry
import com.gbros.tabslite.view.tabview.navigateToTab
import com.gbros.tabslite.view.tabview.playlistEntryScreen
import com.gbros.tabslite.view.tabview.tabScreen

@Composable
fun TabsLiteNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HOME_ROUTE) {
        homeScreen(
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateToTab = navController::navigateToTab,
            onNavigateToPlaylist = navController::navigateToPlaylistDetail,
            onNavigateToPlaylistEntry = navController::navigateToPlaylistEntry
        )

        tabScreen (onNavigateBack = navController::popBackStack)

        playlistEntryScreen (onNavigateBack = navController::popBackStack)

        playlistDetailScreen(
            onNavigateToTabByPlaylistEntryId = navController::navigateToPlaylistEntry,
            onNavigateBack = navController::popBackStack
        )

        searchScreen(
            onNavigateToSongId = navController::navigateToSongVersion,
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateBack = navController::navigateToHome
        )

        songVersionScreen(
            onNavigateToTabByTabId = navController::navigateToTab,
            onNavigateToSearch = navController::navigateToSearch,
            onNavigateBack = navController::popBackStack
        )
    }
}

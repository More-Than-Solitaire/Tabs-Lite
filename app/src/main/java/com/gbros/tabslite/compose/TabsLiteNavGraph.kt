package com.gbros.tabslite.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gbros.tabslite.compose.homescreen.HomeScreen
import com.gbros.tabslite.compose.playlists.PlaylistScreen
import com.gbros.tabslite.compose.searchresultsonglist.SearchScreen
import com.gbros.tabslite.compose.songversionlist.SongVersionScreen
import com.gbros.tabslite.compose.tabview.TabScreen

@Composable
fun TabsLiteNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onSearch = {q -> navController.navigate("search/$q") },
                navigateToTabByPlaylistEntryId = {id -> navController.navigate("tab/true/$id")},
                navigateToPlaylistById = {id -> navController.navigate("playlist/$id")}
            )
        }

        composable("playlist/{playlistId}", arguments = listOf(navArgument("playlistId") { type = NavType.IntType }))  { navBackStackEntry ->
            PlaylistScreen(
                playlistId = navBackStackEntry.arguments!!.getInt("playlistId"),
                navigateToTabByPlaylistEntryId = {id -> navController.navigate("tab/true/$id")},
                navigateBack = { navController.popBackStack() }
            )
        }

        /**
         * Navigate to the Tab detail screen
         *
         * @param [idIsPlaylistEntryId] (Bool) Whether the provided ID is a playlistEntryId (true) or a tabId (false)
         * @param [id] (Int) The ID used to find the tab content.  This can either be a tabId or a PlaylistEntry id (depending on the value of the other parameters)
         */
        composable("tab/{idIsPlaylistEntryId}/{id}",
            arguments = listOf(navArgument("idIsPlaylistEntryId") { type = NavType.BoolType }, navArgument("id") { type = NavType.IntType } )
        ) {navBackStackEntry ->
            TabScreen(
                id = navBackStackEntry.arguments!!.getInt("id"),
                idIsPlaylistEntryId = navBackStackEntry.arguments!!.getBoolean("idIsPlaylistEntryId"),
                navigateBack = { navController.popBackStack() }
            )
        }

        composable("search/{query}") {navBackStackEntry ->
            SearchScreen(
                query = navBackStackEntry.arguments!!.getString("query", ""),
                navigateToSongVersionsBySongId = { songId: Int -> navController.navigate("songversions/$songId") },
                navigateBack = {
                    navController.navigate("home") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onSearch = {q -> navController.navigate("search/$q") }
            )
        }

        composable("songversions/{songId}",
            arguments = listOf(navArgument("songId") { type = NavType.IntType })) { navBackStackEntry ->
            SongVersionScreen(
                songVersionId = navBackStackEntry.arguments!!.getInt("songId"),
                navigateToTabByTabId = {id -> navController.navigate("tab/false/$id")},
                navigateBack = { navController.popBackStack() },
                onSearch = {q -> navController.navigate("search/$q") }
            )
        }
    }

}
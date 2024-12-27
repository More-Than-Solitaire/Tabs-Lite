package com.gbros.tabslite.view.tabview

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab

private const val LOG_NAME = "tabslite.TabScreen     "

//#region use case tab screen

private const val TAB_NAV_ARG = "tabId"
const val TAB_ROUTE_TEMPLATE = "tab/%s"

fun NavController.navigateToTab(tabId: Int) {
    navigate(TAB_ROUTE_TEMPLATE.format(tabId.toString()))
}

fun NavGraphBuilder.tabScreen(
    onNavigateBack: () -> Unit
) {
    composable(
        TAB_ROUTE_TEMPLATE.format("{$TAB_NAV_ARG}"),
        arguments = listOf(navArgument(TAB_NAV_ARG) { type = NavType.IntType } )) { navBackStackEntry ->
        TabScreen(
            id = navBackStackEntry.arguments!!.getInt(TAB_NAV_ARG),
            idIsPlaylistEntryId = false,
            navigateBack = onNavigateBack
        )
    }
}

//#endregion

//#region use case playlist entry

private const val PLAYLIST_ENTRY_NAV_ARG = "playlistEntryId"
const val PLAYLIST_ENTRY_ROUTE_TEMPLATE = "playlist/entry/%s"

fun NavController.navigateToPlaylistEntry(playlistEntryId: Int) {
    navigate(PLAYLIST_ENTRY_ROUTE_TEMPLATE.format(playlistEntryId.toString()))
}

fun NavGraphBuilder.playlistEntryScreen(
    onNavigateBack: () -> Unit
) {
    composable(
        PLAYLIST_ENTRY_ROUTE_TEMPLATE.format("{$PLAYLIST_ENTRY_NAV_ARG}"),
        arguments = listOf(navArgument(PLAYLIST_ENTRY_NAV_ARG) { type = NavType.IntType } )) { navBackStackEntry ->
        TabScreen(
            id = navBackStackEntry.arguments!!.getInt(PLAYLIST_ENTRY_NAV_ARG),
            idIsPlaylistEntryId = true,
            navigateBack = onNavigateBack
        )
    }
}

//#endregion

/**
 * Gets a tab from the database based on the parameters given and displays the tab in a TabView
 *
 * @param [id]: The ID to use to fetch the tab from the database.  Could be a tab ID or a playlist entry ID, depending on the value of the other parameter.
 * @param [idIsPlaylistEntryId]: Whether the ID passed is a playlist entry ID (true) or a tab ID (false).
 */
@Composable
fun TabScreen(id: Int, idIsPlaylistEntryId: Boolean = false, navigateBack: () -> Unit) {
    // allow internal navigation without adding to the backstack (forward and back within the playlist)
    var currentId by remember(key1 = id) { mutableIntStateOf(id) }
    var currentIdIsPlaylistEntryId by remember(key1 = id) { mutableStateOf(idIsPlaylistEntryId) }
    var currentTabId: Int? by remember(key1 = id) { mutableStateOf(null) }

    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val tab: ITab? by with(db.tabFullDao()) {
        if(currentIdIsPlaylistEntryId)
            getTabFromPlaylistEntryId(currentId).observeAsState()
        else
            getTab(currentId).observeAsState()
    }

    // ensure tab is stored locally and has content
    LaunchedEffect(key1 = currentId) {
        try {
            if (currentIdIsPlaylistEntryId) {
                currentTabId = db.playlistEntryDao().getEntryById(currentId)?.tabId
                currentTabId?.let { ITab.fetchFullTab(it, db) }
            } else {
                currentTabId = currentId
                ITab.fetchFullTab(currentId, db)
            }
        } catch (ex: Exception) {
            Log.w(LOG_NAME, "Couldn't fetch tab $currentId: ${ex.message}", ex)
        }
    }

    if (tab != null) {
        TabView(
            tab = tab,
            navigateBack = navigateBack,
            navigateToTabByPlaylistEntryId = { newPlaylistEntryId ->
                currentId = newPlaylistEntryId
                currentIdIsPlaylistEntryId = true
            })
    } else {
        // fall back to something with at least an ID if we can
        TabView(
            tab = Tab(currentTabId),
            navigateBack = navigateBack,
            navigateToTabByPlaylistEntryId = {}
        )
    }

    BackHandler {
        navigateBack()
    }
}

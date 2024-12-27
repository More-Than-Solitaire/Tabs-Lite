package com.gbros.tabslite.view.searchresultsonglist

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Search

private const val SEARCH_NAV_ARG = "query"
const val SEARCH_ROUTE_TEMPLATE = "search/%s"

fun NavController.navigateToSearch(query: String) {
    navigate(SEARCH_ROUTE_TEMPLATE.format(query))
}

fun NavGraphBuilder.searchScreen(
    onNavigateToSongId: (Int) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(SEARCH_ROUTE_TEMPLATE.format("{$SEARCH_NAV_ARG}")) { navBackStackEntry ->
        SearchScreen(
            query = navBackStackEntry.arguments!!.getString(SEARCH_NAV_ARG, ""),
            navigateToSongVersionsBySongId = onNavigateToSongId,
            navigateBack = onNavigateBack,
            onSearch = onNavigateToSearch
        )
    }
}

@Composable
fun SearchScreen(
    query: String,
    navigateToSongVersionsBySongId: (songId: Int) -> Unit,
    navigateBack: () -> Unit,
    onSearch: (query: String) -> Unit
) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val searchSession = Search(db)

    SearchView(query = query, searchSession = searchSession, navigateToSongVersionsBySongId = navigateToSongVersionsBySongId, navigateBack = navigateBack, onSearch = onSearch)

    BackHandler {
        navigateBack()
    }
}

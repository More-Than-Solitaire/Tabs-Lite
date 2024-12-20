package com.gbros.tabslite.view.searchresultsonglist

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Search

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

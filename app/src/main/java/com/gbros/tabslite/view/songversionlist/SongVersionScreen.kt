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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.view.tabsearchbar.TabsSearchBar
import com.gbros.tabslite.data.AppDatabase

@Composable
fun SongVersionScreen(
    songVersionId: Int,
    navigateToTabByTabId: (id: Int) -> Unit,
    navigateBack: () -> Unit,
    onSearch: (query: String) -> Unit
) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val songVersions by db.tabFullDao().getTabsBySongId(songVersionId).observeAsState(listOf())

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
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.generic_action_back))
                }
            },
            onSearch = onSearch
        )

        SongVersionList(songVersions = songVersions.sortedByDescending { v -> v.votes }, navigateToTabByTabId = navigateToTabByTabId)
    }
    
    BackHandler {
        navigateBack()
    }
}
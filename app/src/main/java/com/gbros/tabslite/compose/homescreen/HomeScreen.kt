package com.gbros.tabslite.compose.homescreen

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
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
    var pagerNav by remember { mutableIntStateOf(pagerState.currentPage) }

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
                onSearch = onSearch
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
                    pagerNav = -1
                    pagerNav = 0
                }
                TabRowItem(
                    selected = pagerState.currentPage == 1,
                    inactiveIcon = Icons.Outlined.Person,
                    activeIcon = Icons.Filled.Person,
                    title = "Popular"
                ) {
                    pagerNav = -1
                    pagerNav = 1
                }
                TabRowItem(
                    selected = pagerState.currentPage == 2,
                    inactiveIcon = ImageVector.vectorResource(R.drawable.ic_playlist_play_light),
                    activeIcon = ImageVector.vectorResource(R.drawable.ic_playlist_play),
                    title = "Playlists"
                ) {
                    pagerNav = -1
                    pagerNav = 2
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
                2 -> PlaylistPage(livePlaylists = db.playlistDao().getPlaylists(), navigateToPlaylistById = navigateToPlaylistById)
            }
        }
    }

    LaunchedEffect(pagerNav) {
        if (pagerNav >= 0 && pagerNav != pagerState.currentPage) {
            pagerState.animateScrollToPage(pagerNav)
        }
    }
}

@Composable
fun TabRowItem(selected: Boolean, inactiveIcon: ImageVector, activeIcon: ImageVector, title: String, onClick: () -> Unit) {
    Tab(
        icon = {
            Icon(
                imageVector =  if(selected) activeIcon else inactiveIcon,
                null
            )
        },
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


package com.gbros.tabslite.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.R
import com.gbros.tabslite.compose.playlists.PlaylistList
import com.gbros.tabslite.compose.songlist.SongList
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.data.TabFullWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(liveFavoriteTabs: LiveData<List<TabFullWithPlaylistEntry>>, livePopularTabs: LiveData<List<TabFullWithPlaylistEntry>>, livePlaylists: LiveData<List<Playlist>>) {
    val pagerState = rememberPagerState {3}
    var pagerNav by remember { mutableIntStateOf(0) }

    Column {
        TopAppBar(title = { /*TODO*/ })
        TabsSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
        TabRow(selectedTabIndex = pagerState.currentPage) {
            TabRowItem(selected = pagerState.currentPage == 0, inactiveIcon = Icons.Outlined.Favorite, activeIcon = Icons.Filled.Favorite, title = "Favorites") {
                pagerNav = -1
                pagerNav = 0
            }
            TabRowItem(selected = pagerState.currentPage == 1, inactiveIcon = Icons.Outlined.Person, activeIcon = Icons.Filled.Person, title = "Popular") {
                pagerNav = -1
                pagerNav = 1
            }
            TabRowItem(selected = pagerState.currentPage == 2, inactiveIcon = ImageVector.vectorResource(R.drawable.ic_playlist_outlined), activeIcon = ImageVector.vectorResource(R.drawable.ic_playlist_filled), title = "Playlists") {
                pagerNav = -1
                pagerNav = 2
            }
        }

        LaunchedEffect(pagerNav) {
            if (pagerNav >= 0) {
                pagerState.animateScrollToPage(pagerNav)
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            beyondBoundsPageCount = 3,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxHeight()
        ) { page ->
            when (page) {
                0 -> SongList(liveSongs = liveFavoriteTabs, navigateToTabById = { /*TODO*/ })
                1 -> SongList(liveSongs = livePopularTabs, navigateToTabById = { /*TODO*/ })
                2 -> PlaylistList(livePlaylists = livePlaylists, navigateToPlaylistById = { /*TODO*/ })  //todo: add create-playlist FAB
            }
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
    val tabForTest = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest1 = MutableLiveData(listOf(tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest))
    val tabListForTest2 = MutableLiveData(listOf(tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest, tabForTest))
    val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
    val playlistListForTest = MutableLiveData(listOf(playlistForTest, playlistForTest, playlistForTest ,playlistForTest, playlistForTest))

    AppTheme {
        HomeScreen(tabListForTest1, tabListForTest2, playlistListForTest)
    }
}


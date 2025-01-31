package com.gbros.tabslite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.UgApi
import com.gbros.tabslite.view.songlist.SortBy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val LOG_NAME = "tabslite.HomeActivity  "

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataAccess = AppDatabase.getInstance(applicationContext).dataAccess()

        // fetch the most popular tabs
        CoroutineScope(Dispatchers.IO).launch { UgApi.fetchTopTabs(dataAccess) }

        // set default preferences if they aren't already set
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.insert(Preference(Preference.FAVORITES_SORT, SortBy.DateAdded.name))
            dataAccess.insert(Preference(Preference.POPULAR_SORT, SortBy.Popularity.name))
            dataAccess.insert(Preference(Preference.PLAYLIST_SORT, SortBy.Name.name))
            dataAccess.insert(Preference(Preference.AUTOSCROLL_DELAY, .5f.toString()))
        }

        // create favorites and popular tabs playlists if they don't exist
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.insert(Playlist(
                playlistId = Playlist.TOP_TABS_PLAYLIST_ID,
                userCreated = false,
                title = "Popular",
                dateCreated = System.currentTimeMillis(),
                dateModified = System.currentTimeMillis(),
                description = "Popular tabs amongst users globally"
            ))
            dataAccess.insert(Playlist(
                playlistId = Playlist.FAVORITES_PLAYLIST_ID,
                userCreated = true,
                title = "Favorites",
                dateCreated = System.currentTimeMillis(),
                dateModified = System.currentTimeMillis(),
                description = "Your favorite tabs, stored offline for easy access"
            ))
        }

        // load any tabs that were added without internet connection
        CoroutineScope(Dispatchers.IO).launch { Tab.fetchAllEmptyPlaylistTabsFromInternet(dataAccess) }

        actionBar?.hide()

        setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    TabsLiteNavGraph()
                }
            }
        }
    }
}


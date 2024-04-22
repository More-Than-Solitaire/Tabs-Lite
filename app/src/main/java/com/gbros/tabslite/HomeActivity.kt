package com.gbros.tabslite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.gbros.tabslite.compose.TabsLiteNavGraph
import com.gbros.tabslite.compose.songlist.SortBy
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.UgApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val LOG_NAME = "tabslite.HomeActivity  "

class HomeActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)

        // fetch the most popular tabs
        GlobalScope.launch { UgApi.fetchTopTabs(db) }

        // set default preferences if they aren't already set
        GlobalScope.launch {
            db.preferenceDao().insert(Preference(Preference.FAVORITES_SORT, SortBy.DateAdded.name))
            db.preferenceDao().insert(Preference(Preference.POPULAR_SORT, SortBy.Popularity.name))
            db.preferenceDao().insert(Preference(Preference.PLAYLIST_SORT, SortBy.Name.name))
            db.preferenceDao().insert(Preference(Preference.AUTOSCROLL_DELAY, .5f.toString()))
        }

        // load any tabs that were added without internet connection
        GlobalScope.launch { Tab.fetchAllEmptyPlaylistTabsFromInternet(db) }

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


package com.gbros.tabslite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gbros.tabslite.compose.HomeScreen
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.UgApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val LOG_NAME = "tabslite.HomeActivity  "

class HomeActivity : ComponentActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        GlobalScope.launch { UgApi.fetchTopTabs(db) }
        setContent {
            AppTheme {
                HomeScreen(liveFavoriteTabs = db.tabFullDao().getFavoriteTabs(), livePopularTabs = db.tabFullDao().getPopularTabs(), livePlaylists = db.playlistDao().getPlaylists())
            }
        }
    }
}


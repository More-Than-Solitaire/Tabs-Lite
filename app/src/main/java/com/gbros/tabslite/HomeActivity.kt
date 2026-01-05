package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.data.ThemeSelection
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.BackendConnection
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.view.playlists.PlaylistsSortBy
import com.gbros.tabslite.view.songlist.SortBy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.collections.filter
import kotlin.collections.isNotEmpty

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // enabled by default on Android 15+ (API 35+), but this is for lower Android versions

        val dataAccess = AppDatabase.getInstance(applicationContext).dataAccess()
        launchInitialFetchAndSetupJobs(dataAccess)
        val darkModePref = dataAccess.getLivePreference(Preference.APP_THEME).map { themePref ->
            return@map ThemeSelection.valueOf(themePref?.value ?: ThemeSelection.System.name)
        }

        setContent {
            AppTheme(theme = darkModePref.observeAsState(ThemeSelection.System).value) {
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

    /**
     * Launch the startup jobs for TabsLite, including pre-loading top tabs, ensuring preferences
     * are created, and loading any tabs that the user favorited or added to a playlist, but weren't
     * downloaded successfully at the time
     */
    private fun launchInitialFetchAndSetupJobs(dataAccess: DataAccess) {
        CoroutineScope(Dispatchers.IO).launch {
            fetchTopTabs(dataAccess)
        }

        CoroutineScope(Dispatchers.IO).launch {
            initializeUserPreferences(dataAccess)
        }

        CoroutineScope(Dispatchers.IO).launch {
            initializeDefaultPlaylists(dataAccess)
        }

        launchAndObserveEmptyTabs(dataAccess)
    }

    /**
     * fetch the most popular tabs
     */
    private suspend fun fetchTopTabs(dataAccess: DataAccess) {
        try {
            BackendConnection.fetchTopTabs(dataAccess)
            Log.i(TAG, "Initial top tabs fetched successfully.")
        } catch (ex: BackendConnection.NoInternetException) {
            Log.i(TAG, "Initial top tabs fetch failed due to no internet connection.", ex)
        } catch (ex: Exception) {
            Log.e(TAG, "Unexpected exception during initial top tabs fetch: ${ex.message}", ex)
        }
    }

    /**
     * set default preferences if they aren't already set
     */
    private suspend fun initializeUserPreferences(dataAccess: DataAccess) {
        dataAccess.insert(Preference(Preference.FAVORITES_SORT, SortBy.DateAdded.name))
        dataAccess.insert(Preference(Preference.POPULAR_SORT, SortBy.Popularity.name))
        dataAccess.insert(Preference(Preference.PLAYLIST_SORT, PlaylistsSortBy.Name.name))
        dataAccess.insert(Preference(Preference.AUTOSCROLL_DELAY, .5f.toString()))
        dataAccess.insert(Preference(Preference.INSTRUMENT, Instrument.Guitar.name))
        dataAccess.insert(Preference(Preference.USE_FLATS, false.toString()))
        dataAccess.insert(Preference(Preference.APP_THEME, ThemeSelection.System.name))
        dataAccess.insert(Preference(Preference.PIN_CHORDS, false.toString()))
    }

    /**
     * create favorites and popular tabs playlists if they don't exist
     */
    private suspend fun initializeDefaultPlaylists(dataAccess: DataAccess) {
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

    /**
     * Observes the database for tabs that are in a playlist but haven't been downloaded.
     * It batches these tabs and fetches them from the internet. The process is managed
     * within a coroutine Flow to handle lifecycle and prevent redundant fetches.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun launchAndObserveEmptyTabs(dataAccess: DataAccess): Job {
        val scope = CoroutineScope(Dispatchers.IO)
        val currentlyFetching = Collections.synchronizedSet(mutableSetOf<String>())

        return dataAccess.getEmptyPlaylistTabIdsLive().asFlow()
            .distinctUntilChanged()
            .debounce(500) // Debounce to batch incoming changes
            .filter { it.isNotEmpty() }
            .onEach { allEmptyIds ->
                val idsToFetch = allEmptyIds.filter { it !in currentlyFetching }

                if (idsToFetch.isNotEmpty()) {
                    currentlyFetching.addAll(idsToFetch)
                    Log.d(TAG, "New empty tabs to fetch: ${idsToFetch.size}. Total pending: ${currentlyFetching.size}")

                    // Process in chunks to avoid overwhelming the network or database
                    // firestore has a max of 30 disjunctions, multiply this by two for status in [accepted, pending]
                    idsToFetch.chunked(15).forEach { batch ->
                        scope.launch {
                            try {
                                Log.i(TAG, "Fetching batch of ${batch.size} empty tabs.")
                                BackendConnection.fetchTabsFromInternet(batch, dataAccess)
                            } catch (ex: BackendConnection.NoInternetException) {
                                Log.i(TAG, "Empty tab fetch failed: no internet. Will retry later.", ex)
                            } catch (ex: Exception) {
                                Log.e(TAG, "Exception during empty tab fetch: ${ex.message}", ex)
                            } finally {
                                // Remove this batch from the set after attempting to fetch
                                currentlyFetching.removeAll(batch.toSet())
                            }
                        }
                    }
                }
            }
            .launchIn(scope)
    }
}


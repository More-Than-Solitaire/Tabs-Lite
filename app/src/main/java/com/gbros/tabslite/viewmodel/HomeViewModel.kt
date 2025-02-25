package com.gbros.tabslite.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.playlist.PlaylistFileExportType
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.UgApi
import com.gbros.tabslite.utilities.combine
import com.gbros.tabslite.view.homescreen.IHomeViewState
import com.gbros.tabslite.view.playlists.PlaylistsSortBy
import com.gbros.tabslite.view.songlist.SortBy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@HiltViewModel(assistedFactory = HomeViewModel.HomeViewModelFactory::class)
class HomeViewModel
@AssistedInject constructor(
    @Assisted private val dataAccess: DataAccess
) : ViewModel(), IHomeViewState {

    //#region dependency injection factory

    @AssistedFactory
    interface HomeViewModelFactory {
        fun create(dataAccess: DataAccess): HomeViewModel
    }

    //#endregion

    //#region view state

    /**
     * The percent value (0 to 100) for any ongoing import/export operation
     */
    override val playlistImportProgress: MutableLiveData<Float> = MutableLiveData(0f)

    /**
     * The current state of any import/export operations
     */
    override val playlistImportState: MutableLiveData<LoadingState> = MutableLiveData()

    /**
     * How the playlists are currently sorted
     */
    override val playlistsSortBy: LiveData<PlaylistsSortBy> = dataAccess.getLivePreference(Preference.PLAYLIST_SORT).map { sortByPreference ->
        sortByPreference?.let { PlaylistsSortBy.valueOf(sortByPreference.value) } ?: PlaylistsSortBy.Name
    }

    /**
     * The user's saved playlists, sorted by [playlistsSortBy]
     */
    override val playlists: LiveData<List<Playlist>> = dataAccess.getLivePlaylists().combine(playlistsSortBy) { playlists, currentSortBy ->
        when(currentSortBy) {
            PlaylistsSortBy.Name -> playlists?.sortedBy { it.title } ?: listOf()
            PlaylistsSortBy.DateAdded -> playlists?.sortedByDescending { it.dateCreated } ?: listOf()
            PlaylistsSortBy.DateModified -> playlists?.sortedByDescending { it.dateModified } ?: listOf()
            null -> playlists ?: listOf()
        }
    }


    //#endregion

    //#region public data

    /**
     * The view model for the TabSearchBar
     */
    val tabSearchBarViewModel = TabSearchBarViewModel(dataAccess = dataAccess)

    /**
     * The view model for the list of favorited songs ("Favorites" tab)
     */
    val favoriteSongListViewModel = SongListViewModel(
        playlistId = Playlist.FAVORITES_PLAYLIST_ID,
        defaultSortBy = SortBy.DateAdded,
        sortPreferenceName = Preference.FAVORITES_SORT,
        dataAccess = dataAccess
    )

    /**
     * The view model for the list of popular songs ("Popular" tab)
     */
    val popularSongListViewModel = SongListViewModel(
        playlistId = Playlist.TOP_TABS_PLAYLIST_ID,
        defaultSortBy = SortBy.Popularity,
        sortPreferenceName = Preference.POPULAR_SORT,
        dataAccess = dataAccess
    )

    //#endregion

    //#region public methods

    /**
     * handle playlist sorting
     */
    fun sortPlaylists(sortBy: PlaylistsSortBy){
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.upsert(Preference(Preference.PLAYLIST_SORT, sortBy.name))
        }
    }

    /**
     * Export all the user's playlists (including Favorites) to the specified file
     */
    fun exportPlaylists(destinationFile: Uri, contentResolver: ContentResolver) {
        playlistImportState.postValue(LoadingState.Loading)
        playlistImportProgress.postValue(0.2f)

        val exportJob = CoroutineScope(Dispatchers.IO).async {
            val allUserPlaylists = dataAccess.getPlaylists()
                .filter { playlist -> playlist.playlistId != Playlist.TOP_TABS_PLAYLIST_ID }
            val allPlaylists = mutableListOf(
                Playlist(
                    -1,
                    false,
                    "Favorites",
                    0,
                    0,
                    ""
                )
            ) // add the Favorites playlist
            allPlaylists.addAll(allUserPlaylists)
            playlistImportProgress.postValue(0.6f)
            val allSelfContainedPlaylists = dataAccess.getSelfContainedPlaylists(allPlaylists)
            val playlistsAndEntries =
                Json.encodeToString(PlaylistFileExportType(playlists = allSelfContainedPlaylists))
            playlistImportProgress.postValue(0.8f)

            contentResolver.openOutputStream(destinationFile).use { outputStream ->
                outputStream?.write(playlistsAndEntries.toByteArray())
                outputStream?.flush()
            }

            playlistImportProgress.postValue(1f)
            delay(700)
        }
        exportJob.invokeOnCompletion { ex ->
            if (ex != null) {
                playlistImportState.postValue(LoadingState.Error(ex.message ?: "No error message"))
                Log.e(TAG, "Unexpected error during playlist export: ${ex.message}")
            } else {
                playlistImportState.postValue(LoadingState.Success)
            }

            // reset for next export
            playlistImportProgress.postValue(0f)
        }
    }

    /**
     * Import user playlists (including Favorites) from the specified file. Also fetches each imported
     * tab from the internet.
     */
    fun importPlaylists(sourceFile: Uri, contentResolver: ContentResolver) {

        // a just-visible value to indicate that we've started the import
        playlistImportProgress.postValue(.05f)
        playlistImportState.postValue(LoadingState.Loading)

        val importJob = CoroutineScope(Dispatchers.IO).async {
            // read file
            var dataToImport: String?
            contentResolver.openInputStream(sourceFile).use {
                dataToImport = it?.reader()?.readText()
            }

            if (!dataToImport.isNullOrBlank()) {
                val importedData = Json.decodeFromString<PlaylistFileExportType>(dataToImport!!)

                // import all playlists (except Favorites and Top Tabs)
                val totalEntriesToImport = importedData.playlists.sumOf { pl -> pl.entries.size }.toFloat()

                // track the amount of progress used by previous playlists, used to add current progress to
                var progressFromPreviouslyImportedPlaylists = 0f

                for (playlist in importedData.playlists.filter { pl -> pl.playlistId != Playlist.TOP_TABS_PLAYLIST_ID }) {
                    val progressForThisPlaylist =
                        playlist.entries.size.toFloat() / totalEntriesToImport  // available portion of 100% to use for this playlist
                    try {
                        playlist.importToDatabase(
                            dataAccess = dataAccess,
                            onProgressChange = { progress ->
                                playlistImportProgress.postValue(progressFromPreviouslyImportedPlaylists + (progress * progressForThisPlaylist))
                            })
                    } catch (ex: UgApi.NoInternetException) {
                        playlistImportState.postValue(LoadingState.Error("No internet connection. Playlist tabs have been added, but won't be downloaded until next time you restart the app with internet access."))
                        Log.i(TAG, "Import of playlist ${playlist.title} (id: ${playlist.playlistId}) completed without internet access.")
                    } catch (ex: Exception) {
                        Log.e(TAG, "Import of playlist ${playlist.title} (id: ${playlist.playlistId}) failed: ${ex.message}", ex)
                    }

                    progressFromPreviouslyImportedPlaylists += progressForThisPlaylist
                }
            }

            // pause at 100% progress for a moment before setting progress to 0
            playlistImportProgress.postValue(1f)
            delay(700)
        }
        importJob.invokeOnCompletion { ex ->
            if (ex != null) {
                playlistImportState.postValue(LoadingState.Error(ex.message ?: "No error message"))
                Log.e(TAG, "Unexpected error during playlist import: ${ex.message}")
            } else {
                playlistImportState.postValue(LoadingState.Success)
            }

            playlistImportProgress.postValue(0f)
        }
    }

    /**
     * Create a new playlist and add it to the local database
     */
    fun createPlaylist(title: String, description: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val newPlaylist = Playlist(userCreated = true, title = title, description = description, dateCreated = System.currentTimeMillis(), dateModified = System.currentTimeMillis())
            dataAccess.upsert(newPlaylist)
        }
    }

    //#endregion

}
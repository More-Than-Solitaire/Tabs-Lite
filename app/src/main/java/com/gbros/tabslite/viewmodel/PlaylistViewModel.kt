package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.playlist.IDataPlaylistEntry
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.view.playlists.IPlaylistViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PlaylistViewModel.PlaylistViewModelFactory::class)
class PlaylistViewModel
@AssistedInject constructor(
    @Assisted private val playlistId: Int,
    @Assisted private val dataAccess: DataAccess
) : ViewModel(), IPlaylistViewState {

    //#region dependency injection factory

    @AssistedFactory
    interface PlaylistViewModelFactory {
        fun create(playlistId: Int, dataAccess: DataAccess): PlaylistViewModel
    }

    //#endregion

    //#region private data

    private val playlist: LiveData<Playlist> = dataAccess.getLivePlaylist(playlistId)

    //#endregion

    //#region view state

    /**
     * The title of the playlist to display
     */
    override val title: LiveData<String> = playlist.map { p -> p.title }

    /**
     * The description of the playlist to display
     */
    override val description: LiveData<String> = playlist.map { p -> p.description }

    /**
     * The ordered list of songs in the playlist
     */
    override val songs: LiveData<List<TabWithDataPlaylistEntry>> = dataAccess.getSortedPlaylistTabs(playlistId)

    //#endregion

    //#region public methods

    /**
     * Rearrange playlist entries
     */
    fun reorderPlaylistEntry(fromIndex: Int, toIndex: Int) {
        val currentSongs = songs.value
        if (currentSongs != null) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "Moving ${currentSongs[fromIndex].songName} to ${if (toIndex > fromIndex) "after" else "before"} ${currentSongs[toIndex].songName}")
                entryMoved(currentSongs[fromIndex], currentSongs[toIndex], toIndex > fromIndex)
            }

        } else {
            Log.e(TAG, "Attempting to reorder songs in an uninitialized song list")
        }
    }

    /**
     * Rearrange playlist entries
     */
    private suspend fun entryMoved(src: IDataPlaylistEntry, dest: IDataPlaylistEntry, moveAfter: Boolean) {
        if (moveAfter) {
            dataAccess.moveEntryAfter(src, dest)
        } else {
            dataAccess.moveEntryBefore(src, dest)
        }
    }

    /**
     * Remove an entry from the playlist
     */
    fun entryRemoved(entry: IDataPlaylistEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.removeEntryFromPlaylist(entry)
        }
    }

    /**
     * Delete the playlist and all playlist entries for this playlist from the database
     */
    fun playlistDeleted() {
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.deletePlaylist(playlistId)
        }
    }

    /**
     * Update playlist description
     */
    fun descriptionChanged(newDescription: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.updateDescription(playlistId = playlistId, newDescription = newDescription)
        }
    }

    /**
     * Update playlist title
     */
    fun titleChanged(newTitle: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.updateTitle(playlistId = playlistId, newTitle = newTitle)
        }
    }

    //#endregion
}
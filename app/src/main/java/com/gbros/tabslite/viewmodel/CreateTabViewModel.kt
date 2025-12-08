package com.gbros.tabslite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.TabDataType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel(assistedFactory = CreateTabViewModel.CreateTabViewModelFactory::class)
class CreateTabViewModel @AssistedInject constructor(@Assisted private val dataAccess: DataAccess) : ViewModel() {

    //#region dependency injection factory

    @AssistedFactory
    interface CreateTabViewModelFactory {
        fun create(dataAccess: DataAccess): CreateTabViewModel
    }

    //#endregion

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        viewModelScope.launch {
            _playlists.value = dataAccess.getPlaylists()
        }
    }

    fun createTab(
        songName: String, artistName: String, content: String, playlistId: Int,
        versionDescription: String, difficulty: String, tuning: String, capo: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val tabId = Random.nextInt(Int.MIN_VALUE, -1)
            val songId = Random.nextInt(Int.MIN_VALUE, -1)
            val newTab = TabDataType(
                tabId = tabId, songId = songId, songName = songName, artistName = artistName, content = content, type = "Player Pro",
                versionDescription = versionDescription, difficulty = difficulty, tuning = tuning, capo = capo
            )
            dataAccess.upsert(newTab)
            dataAccess.appendToPlaylist(playlistId, tabId, 0)
        }
    }
}

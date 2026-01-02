package com.gbros.tabslite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.servertypes.SongRequestType
import com.gbros.tabslite.data.tab.SongGenre
import com.gbros.tabslite.data.tab.TabDifficulty
import com.gbros.tabslite.utilities.BackendConnection
import com.gbros.tabslite.utilities.combine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CreateSongViewModel.CreateSongViewModelFactory::class)
class CreateSongViewModel @AssistedInject constructor(
    @Assisted initialSongName: String
) : ViewModel() {

    //#region dependency injection factory

    @AssistedFactory
    interface CreateSongViewModelFactory {
        fun create(initialSongName: String = ""): CreateSongViewModel
    }

    //#endregion dependency injection factory

    //#region view state

    val songName = MutableLiveData(initialSongName)
    val artistName = MutableLiveData("")
    val songGenre = MutableLiveData(SongGenre.Other)

    val newSongId = MutableLiveData<String?>(null)

    val songCreationState = MutableLiveData<LoadingState>(LoadingState.NotStarted)

    val fieldValidation: LiveData<Boolean> = combine(
        songName.map { it.isNotEmpty() },
        artistName.map { it.isNotEmpty() },
        songGenre.map { it != SongGenre.Other },
        combineFn = { values -> values.all { it == true } }
    )

    //#endregion view state

    //#region view events

    fun createSong() {
        viewModelScope.async {
            val song = SongRequestType(
                song_name = songName.value ?: "",
                artist_name = artistName.value ?: "",
                song_genre = (songGenre.value ?: SongGenre.Other).toString(),
                total_votes = 0,
                versions_count = 0,
                status = "pending"
            )
            val songId = BackendConnection.createSong(song)
            newSongId.postValue(songId)
            songCreationState.postValue(LoadingState.Success)
        }.invokeOnCompletion { throwable ->
            if (throwable != null) {
                songCreationState.postValue(LoadingState.Error(messageStringRef = R.string.message_error_creating_song, errorDetails = throwable.message.toString()))
            }
        }
    }

    fun songGenreUpdated(genre: SongGenre) {
        songGenre.value = genre
    }

    fun songNameUpdated(name: String) {
        songName.value = name
    }

    fun artistNameUpdated(name: String) {
        artistName.value = name
    }

    //#endregion view events
}

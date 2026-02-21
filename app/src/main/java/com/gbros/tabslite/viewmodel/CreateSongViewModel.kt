package com.gbros.tabslite.viewmodel

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.servertypes.SongRequestType
import com.gbros.tabslite.data.tab.SongGenre
import com.gbros.tabslite.utilities.BackendConnection
import com.gbros.tabslite.utilities.combine
import com.gbros.tabslite.view.createtab.ICreateSongViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CreateSongViewModel.CreateSongViewModelFactory::class)
class CreateSongViewModel @AssistedInject constructor(
    @Assisted initialSongName: String
) : ViewModel(), ICreateSongViewState {

    //#region dependency injection factory

    @AssistedFactory
    interface CreateSongViewModelFactory {
        fun create(initialSongName: String = ""): CreateSongViewModel
    }

    //#endregion dependency injection factory

    //#region view state

    override val songName = MutableLiveData(initialSongName)
    override val artistName = MutableLiveData("")
    override val artistFetchState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.NotStarted)
    private val artistId = MutableLiveData("")
    override val songGenre = MutableLiveData(SongGenre.Other)

    override val newSongId = MutableLiveData<String?>(null)

    override val songCreationState = MutableLiveData<LoadingState>(LoadingState.NotStarted)

    override val fieldValidation: LiveData<Boolean> = combine(
        songName.map { it.isNotEmpty() },
        artistName.map { it.isNotEmpty() },
        songGenre.map { it != SongGenre.Other },
        artistId.map { it.isNotEmpty() },
        artistFetchState.map { it is LoadingState.Success },
        combineFn = { values -> values.all { it == true } }
    )

    //#endregion view state

    //#region view events

    override fun createSong() {
        viewModelScope.async {
            val song = SongRequestType(
                song_name = songName.value ?: "",
                artist_name = artistName.value ?: "",
                artist_id = artistId.value ?: "",
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

    override fun songGenreUpdated(genre: SongGenre) {
        songGenre.value = genre
    }

    override fun songNameUpdated(name: String) {
        songName.value = name
    }

    override fun artistNameUpdated(name: String) {
        artistName.value = name.trim()
        artistFetchState.value = LoadingState.Loading

        if (name.isBlank()) {
            artistId.value = ""
            artistFetchState.postValue(LoadingState.NotStarted)
            return
        }

        // attempt to fetch an artist ID for the artist name
        CoroutineScope(Dispatchers.IO).launch {
            try {
                artistId.postValue(BackendConnection.fetchArtistId(name.trim()))
                artistFetchState.postValue(LoadingState.Success)
            } catch (ex: Resources.NotFoundException) {
                artistFetchState.postValue(LoadingState.Error(messageStringRef = R.string.message_artist_name_not_found))
            } catch (ex: Exception) {
                artistFetchState.postValue(LoadingState.Error(messageStringRef = R.string.message_error_fetching_artist_id, errorDetails = ex.message.toString()))
            }
        }
    }

    override fun createNewArtist() {
        if (!artistName.value.isNullOrBlank()) {
            artistFetchState.value = LoadingState.Loading

            CoroutineScope(Dispatchers.IO).async {
                val newArtistId = BackendConnection.createNewArist(artistName.value ?: "")
                artistId.postValue(newArtistId)
                artistFetchState.postValue(LoadingState.Success)
            }.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    artistFetchState.postValue(LoadingState.Error(messageStringRef = R.string.message_error_creating_artist, errorDetails = throwable.message.toString()))
                }
            }
        }
    }

    //#endregion view events
}

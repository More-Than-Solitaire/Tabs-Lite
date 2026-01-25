package com.gbros.tabslite.view.createtab

import androidx.lifecycle.LiveData
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.tab.SongGenre

interface ICreateSongViewState {
    val songName: LiveData<String>
    val artistName: LiveData<String>
    val songGenre: LiveData<SongGenre>
    val fieldValidation: LiveData<Boolean>
    val songCreationState: LiveData<LoadingState>
    val newSongId: LiveData<String?>
    val artistFetchState: LiveData<LoadingState>

    fun songNameUpdated(name: String)
    fun artistNameUpdated(name: String)
    fun songGenreUpdated(genre: SongGenre)
    fun createNewArtist()
    fun createSong()


}
package com.gbros.tabslite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.view.songversionlist.ISongVersionViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = SongVersionViewModel.SongVersionViewModelFactory::class)
class SongVersionViewModel
@AssistedInject constructor(
    @Assisted songId: String,
    @Assisted dataAccess: DataAccess,
) : ViewModel(), ISongVersionViewState {

    //#region dependency injection factory

    @AssistedFactory
    interface SongVersionViewModelFactory {
        fun create(songId: String, dataAccess: DataAccess): SongVersionViewModel
    }

    //#endregion

    //#region view state

    /**
     * The versions of the selected song to be displayed
     */
    override val songVersions: LiveData<List<ITab>> = dataAccess.getTabsBySongId(songId).map { tabList -> tabList }

    /**
     * The search query to be displayed in the search bar
     */
    override val songName: LiveData<String> = songVersions.map { tabList -> tabList.firstOrNull()?.songName ?: "" }

    //#endregion

    //#region public data

    val tabSearchBarViewModel = TabSearchBarViewModel(
        initialQuery = songName.value ?: "",
        dataAccess = dataAccess
    )

    //#endregion

    //#region init

    init {
        // this may cause a small memory leak, since observeForever doesn't get garbage collected automatically
        songName.observeForever { name -> tabSearchBarViewModel.onQueryChange(name) }
    }

    //#endregion

}
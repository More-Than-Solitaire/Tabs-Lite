package com.gbros.tabslite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.TabDifficulty
import com.gbros.tabslite.data.TabTuning
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabDataType
import com.gbros.tabslite.utilities.BackendConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

@HiltViewModel(assistedFactory = CreateTabViewModel.CreateTabViewModelFactory::class)
class CreateTabViewModel @AssistedInject constructor(
    @Assisted private val dataAccess: DataAccess,
    @Assisted private val selectedSongId: String
) : ViewModel() {

    //#region dependency injection factory

    @AssistedFactory
    interface CreateTabViewModelFactory {
        fun create(dataAccess: DataAccess, selectedSongId: String): CreateTabViewModel
    }

    //#endregion dependency injection factory

    //#region private data

    val selectedSong: LiveData<Tab?> = dataAccess.getFirstTabBySongId(selectedSongId)

    //#endregion

    //#region view state

    val content: MutableLiveData<String> = MutableLiveData("")
    val selectedSongName: LiveData<String> = selectedSong.map { tab -> tab?.songName ?: "Error: $selectedSongId not found" }
    val selectedArtistName: LiveData<String> = selectedSong.map { tab -> tab?.artistName ?: "" }

    val capo: MutableLiveData<Int> = MutableLiveData(0)
    val difficulty: MutableLiveData<TabDifficulty> = MutableLiveData(TabDifficulty.NotSet)
    val versionDescription: MutableLiveData<String> = MutableLiveData("")
    val tuning: MutableLiveData<TabTuning> = MutableLiveData(TabTuning.Standard)

    //#endregion view state

    //#region view actions

    fun difficultyUpdated(difficulty: TabDifficulty) = this.difficulty.postValue(difficulty)

    fun capoUpdated(capo: Int) = this.capo.postValue(capo)

    fun contentUpdated(content: String) = this.content.postValue(content)

    fun versionDescriptionUpdated(versionDescription: String) = this.versionDescription.postValue(versionDescription)

    fun tuningUpdated(tuning: TabTuning) = this.tuning.postValue(tuning)

    //#endregion

    //#region methods

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

    //#endregion

    //#region constructor

    init {

    }
}

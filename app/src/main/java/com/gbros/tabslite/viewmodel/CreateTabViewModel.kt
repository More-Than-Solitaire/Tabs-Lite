package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.servertypes.TabRequestType
import com.gbros.tabslite.data.tab.TabDifficulty
import com.gbros.tabslite.data.tab.TabTuning
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabDataType
import com.gbros.tabslite.utilities.BackendConnection
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.combine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    val content: MutableLiveData<TextFieldValue> = MutableLiveData(TextFieldValue(""))
    val selectedSongName: LiveData<String> = selectedSong.map { tab -> tab?.songName ?: "Error: $selectedSongId not found" }
    val selectedArtistName: LiveData<String> = selectedSong.map { tab -> tab?.artistName ?: "" }

    val capo: MutableLiveData<Int> = MutableLiveData(0)
    val difficulty: MutableLiveData<TabDifficulty> = MutableLiveData(TabDifficulty.NotSet)
    val versionDescription: MutableLiveData<String> = MutableLiveData("")
    val tuning: MutableLiveData<TabTuning> = MutableLiveData(TabTuning.Standard)

    // track status of Submit action. Once it's complete, the view should navigate to the next page; this page's actions are done
    val submissionStatus: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.NotStarted)

    val dataValidated: LiveData<Boolean> = combine(
        content.map { it.text.isNotEmpty() },
        selectedSongName.map { it.isNotEmpty() },
        selectedArtistName.map { it.isNotEmpty() },
        difficulty.map { it != TabDifficulty.NotSet },
        versionDescription.map { it.length <= 10000 },
        tuning.map { it != null },
        capo.map { it in 0..30 },
        combineFn = { values -> values.all { it == true } }
    )

    //#endregion view state

    //#region view actions

    fun difficultyUpdated(difficulty: TabDifficulty) = this.difficulty.postValue(difficulty)

    fun capoUpdated(capo: Int) = this.capo.postValue(capo)

    fun contentUpdated(content: TextFieldValue) = this.content.postValue(content)

    fun versionDescriptionUpdated(versionDescription: String) = this.versionDescription.postValue(versionDescription)

    fun tuningUpdated(tuning: TabTuning) = this.tuning.postValue(tuning)

    fun insertChord(chord: String) {
        if (chord.isEmpty()) return

        val currentContent = content.value ?: TextFieldValue()
        val textToInsert = "{ch:$chord}"
        val selection = currentContent.selection
        val newText = currentContent.text.replaceRange(selection.start, selection.end, textToInsert)
        val newSelectionStart = selection.start + textToInsert.length
        val newContent = TextFieldValue(newText, selection = TextRange(newSelectionStart))
        content.postValue(newContent)
    }

    //#endregion

    //#region methods

    fun submitTab() {
        submissionStatus.value = LoadingState.Loading

        // data checking
        try {
            val content = content.value?.text ?: throw IllegalArgumentException("Content is null")
            if (content.isEmpty()) {
                throw IllegalArgumentException("Content is empty")
            }

            val songName = selectedSongName.value ?: throw IllegalArgumentException("Song name is null")
            if (songName.isEmpty()) {
                throw IllegalArgumentException("Song name is empty")
            }

            val artistName = selectedArtistName.value ?: throw IllegalArgumentException("Artist name is null")
            if (artistName.isEmpty()) {
                throw IllegalArgumentException("Artist name is empty")
            }

            val difficulty = difficulty.value ?: throw IllegalArgumentException("Difficulty is null")
            if (difficulty == TabDifficulty.NotSet) {
                throw IllegalArgumentException("Difficulty is not set")
            }

            val versionDescription = versionDescription.value ?: throw IllegalArgumentException("Version description is null")
            if (versionDescription.length > 10000) {
                throw IllegalArgumentException("Version description is too long")
            }

            val tuning = tuning.value ?: throw IllegalArgumentException("Tuning is null")
            val capo = capo.value ?: throw IllegalArgumentException("Capo is null")
            if (capo !in 0..30) {
                throw IllegalArgumentException("Capo must be between 0 and 30")
            }

            val newTab = TabRequestType(
                song_id = selectedSongId,
                content = content,
                version_description = versionDescription,
                difficulty = difficulty.name,
                tuning = tuning.toString(),
                capo = capo,
            )

            CoroutineScope(Dispatchers.IO).async {
                val completedTab = BackendConnection.createTab(tab = newTab)
                dataAccess.upsert(completedTab)
                dataAccess.insertToFavorites(completedTab.tabId, 0)
                submissionStatus.postValue(LoadingState.Success)
            }.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Error submitting tab", throwable)
                    submissionStatus.postValue(LoadingState.Error(messageStringRef = R.string.message_tab_creation_failed, errorDetails = throwable.message ?: ""))
                }
            }

        }
        catch (e: IllegalArgumentException) {
            Log.e(TAG, "Missing or invalid data in CreateTabViewModel.submitTab(); the app shouldn't have let the user click Submit", e)
            submissionStatus.postValue(LoadingState.Error(messageStringRef = R.string.message_tab_creation_failed_missing_arguments))
        }
    }

    //#endregion

    //#region constructor

    init {

    }
}

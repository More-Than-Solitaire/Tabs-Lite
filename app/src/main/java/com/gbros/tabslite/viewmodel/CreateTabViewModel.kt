package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.FontStyle
import com.gbros.tabslite.data.servertypes.TabRequestType
import com.gbros.tabslite.data.tab.TabContentBlock
import com.gbros.tabslite.data.tab.TabDifficulty
import com.gbros.tabslite.data.tab.TabTuning
import com.gbros.tabslite.utilities.BackendConnection
import com.gbros.tabslite.utilities.TAG
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CreateTabViewModel.CreateTabViewModelFactory::class)
class CreateTabViewModel @AssistedInject constructor(
    /**
     * The data access object for the database, used to save the tab.
     */
    @Assisted private val dataAccess: DataAccess,

    /**
     * The ID of the song to create a version of
     */
    @Assisted("selectedSongId") private val selectedSongId: String,

    /**
     * (Optional) Pre-fill the content field with the content of the tab with this ID.
     */
    @Assisted("startingContentTabId") private val startingContentTabId: String? = null
) : ViewModel() {

    //#region dependency injection factory

    @AssistedFactory
    interface CreateTabViewModelFactory {
        fun create(dataAccess: DataAccess, @Assisted("selectedSongId") selectedSongId: String, @Assisted("startingContentTabId") startingContentTabId: String? = null): CreateTabViewModel
    }

    //#endregion dependency injection factory

    //#region view state

    val content: MutableStateFlow<TextFieldValue> = MutableStateFlow(TextFieldValue(""))
    val annotatedContent: StateFlow<List<TabContentBlock>> = content.map { textFieldValue ->
        TabContent(urlHandler = {}, content = textFieldValue.text).contentBlocks
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = emptyList()
    )
    val selectedSongName: MutableStateFlow<String> = MutableStateFlow("")
    val selectedArtistName: MutableStateFlow<String> = MutableStateFlow("")

    val capo: MutableStateFlow<Int> = MutableStateFlow(0)
    val difficulty: MutableStateFlow<TabDifficulty> = MutableStateFlow(TabDifficulty.NotSet)
    val versionDescription: MutableStateFlow<String> = MutableStateFlow("")
    val tuning: MutableStateFlow<TabTuning> = MutableStateFlow(TabTuning.Standard)

    // track status of Submit action. Once it's complete, the view should navigate to the next page; this page's actions are done
    val submissionStatus: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.NotStarted)

    val dataValidated: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        content.map { it.text.isNotEmpty() },
        selectedSongName.map { it.isNotEmpty() },
        selectedArtistName.map { it.isNotEmpty() },
        difficulty.map { it != TabDifficulty.NotSet },
        versionDescription.map { it.length <= 10000 },
        tuning.map { it != null },
        capo.map { it in 0..30 },
        transform = { values: Array<Boolean> -> values.all { it } }
    ).stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = false
    )

    val createdTabId: MutableStateFlow<String> = MutableStateFlow("")

    val fontStylePreference: LiveData<FontStyle> = dataAccess.getLivePreference(com.gbros.tabslite.data.Preference.FONT_STYLE).map { p ->
        if (p != null) FontStyle.valueOf(p.value) else FontStyle.Modern
    }

    //#endregion view state

    //#region view actions

    fun difficultyUpdated(difficulty: TabDifficulty) { this.difficulty.value = difficulty }

    fun capoUpdated(capo: Int) { this.capo.value = capo }

    fun contentUpdated(content: TextFieldValue) { this.content.value = content }

    fun versionDescriptionUpdated(versionDescription: String) { this.versionDescription.value = versionDescription }

    fun tuningUpdated(tuning: TabTuning) { this.tuning.value = tuning }

    fun insertChord(chord: String) {
        if (chord.isEmpty()) return

        val currentContent = content.value ?: TextFieldValue()
        val textToInsert = "{ch:$chord}"
        val selection = currentContent.selection
        val newText = currentContent.text.replaceRange(selection.start, selection.end, textToInsert)
        val newSelectionStart = selection.start + textToInsert.length
        val newContent = TextFieldValue(newText, selection = TextRange(newSelectionStart))
        content.value = newContent
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
                createdTabId.value = completedTab.tabId
                dataAccess.upsert(completedTab)
                dataAccess.insertToFavorites(completedTab.tabId, 0)
                submissionStatus.value = LoadingState.Success
            }.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Error submitting tab", throwable)
                    submissionStatus.value = LoadingState.Error(messageStringRef = R.string.message_tab_creation_failed, errorDetails = throwable.message ?: "")
                }
            }

        }
        catch (e: IllegalArgumentException) {
            Log.e(TAG, "Missing or invalid data in CreateTabViewModel.submitTab(); the app shouldn't have let the user click Submit", e)
            submissionStatus.value = LoadingState.Error(messageStringRef = R.string.message_tab_creation_failed_missing_arguments)
        }
    }

    //#endregion

    //#region constructor

    init {
        if (startingContentTabId != null) {
            CoroutineScope(Dispatchers.IO).async {
                val tabContent = dataAccess.getTabInstance(startingContentTabId)
                content.value = TextFieldValue(tabContent.content)
                capo.value = tabContent.capo
                tuning.value = TabTuning.fromString(tabContent.tuning)
                Log.d(TAG, "prefilled tab content: ${tabContent.content}")
            }.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Error prefilling tab content", throwable)
                    submissionStatus.value = LoadingState.Error(messageStringRef = R.string.message_prefilling_tab_failed, errorDetails = throwable.message ?: "Unknown error")
                }
            }
        }

        // ensure that the song name has been fetched
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val songDetails = BackendConnection.fetchSongDetails(selectedSongId)
                if (songDetails == null) {
                    Log.e(TAG, "Error fetching song details for tab creation")
                    selectedSongName.value = "Error: $selectedSongId not found."
                    selectedArtistName.value = ""
                } else {
                    selectedSongName.value = songDetails.song_name
                    selectedArtistName.value = songDetails.artist_name
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching song details for tab creation", e)
                selectedSongName.value = "Error: $selectedSongId not found."
                selectedArtistName.value = ""
            }
        }
    }
}

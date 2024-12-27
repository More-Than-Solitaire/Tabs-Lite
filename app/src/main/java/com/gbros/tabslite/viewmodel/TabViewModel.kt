package com.gbros.tabslite.viewmodel

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.view.tabview.ITabViewState

class TabViewModel(id: Int, idIsPlaylistEntryId: Boolean = false) : ViewModel(), ITabViewState {
    //#region view state

    override var title: String = ""
        private set

    override var isFavorite: Boolean = false
        private set

    /**
     * Whether to display the playlist navigation bar
     */
    override var isPlaylistEntry: Boolean = false
        private set

    override var playlistTitle: String = ""
        private set

    override var difficulty: String = ""
        private set
    
    override var tuning: String = ""
        private set

    override var capoText: String = ""
        private set

    override var key: String = ""
        private set
    
    override var author: String = ""
        private set

    override var currentTranspose: Int = 0
        private set

    override var content: AnnotatedString = AnnotatedString("")
        private set

    override var state: LoadingState = LoadingState.Loading
        private set

    /**
     * Whether we're currently autoscrolling (the Play button has been pressed)
     */
    override var autoscrollEnabled: Boolean = false
        private set

    override var autoScrollSpeedSliderPosition: Float = 1f
        private set

    /**
     * Whether to display the chord fingerings for the current chord
     */
    override var chordDetailsActive: Boolean = false
        private set

    /**
     * The title for the chord details section (usually the name of the active chord being displayed)
     */
    override var chordDetailsTitle: String = ""
        private set

    /**
     * The state of the chord details section (loading until the details have been fetched successfully)
     */
    override var chordDetailsState: LoadingState = LoadingState.Loading
        private set

    /**
     * A list of chord fingerings to be displayed in the chord details section
     */
    override var chordDetailsVariations: List<ChordVariation> = emptyList()
        private set

    //#endregion

    //#region event handling

    val onPlaylistNextSongClick: () -> Unit = { }

    val onPlaylistPreviousSongClick: () -> Unit = { }

    val onTransposeUpClick: () -> Unit = { }

    val onTransposeDownClick: () -> Unit = { }

    /**
     * Callback to be called when the user triggers a tab refresh
     */
    val onReload: () -> Unit = { }

    /**
     * Callback for when a chord is clicked, to display the chord fingering diagram
     */
    val onChordClick: (String) -> Unit = { }

    //#endregion
}
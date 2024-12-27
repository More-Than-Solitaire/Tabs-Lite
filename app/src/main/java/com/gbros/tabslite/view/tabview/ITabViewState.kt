package com.gbros.tabslite.view.tabview

import androidx.compose.ui.text.AnnotatedString
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.chord.ChordVariation

/**
 * The view state for [TabView] to control the state of each UI element in the view
 */
interface ITabViewState {
    val title: String
    val isFavorite: Boolean

    /**
     * Whether to display the playlist navigation bar
     */
    val isPlaylistEntry: Boolean

    val playlistTitle: String

    val difficulty: String

    val tuning: String

    val capoText: String

    val key: String

    val author: String

    val currentTranspose: Int

    val content: AnnotatedString

    val state: LoadingState

    /**
     * Whether we're currently autoscrolling (the Play button has been pressed)
     */
    val autoscrollEnabled: Boolean

    val autoScrollSpeedSliderPosition: Float

    /**
     * Whether to display the chord fingerings for the current chord
     */
    val chordDetailsActive: Boolean

    /**
     * The title for the chord details section (usually the name of the active chord being displayed)
     */
    val chordDetailsTitle: String

    /**
     * The state of the chord details section (loading until the details have been fetched successfully)
     */
    val chordDetailsState: LoadingState

    /**
     * A list of chord fingerings to be displayed in the chord details section
     */
    val chordDetailsVariations: List<ChordVariation>
}
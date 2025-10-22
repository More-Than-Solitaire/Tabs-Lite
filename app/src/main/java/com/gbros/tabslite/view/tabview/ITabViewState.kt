package com.gbros.tabslite.view.tabview

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.LiveData
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.ITab

/**
 * The view state for [TabScreen] to control the state of each UI element in the view
 */
interface ITabViewState {

    /**
     * The name of the song being displayed
     */
    val songName: LiveData<String>

    val isFavorite: LiveData<Boolean>

    /**
     * Whether to display the playlist navigation bar
     */
    val isPlaylistEntry: LiveData<Boolean>

    val playlistTitle: LiveData<String>

    val playlistNextSongButtonEnabled: LiveData<Boolean>

    val playlistPreviousSongButtonEnabled: LiveData<Boolean>

    val difficulty: LiveData<String>

    val tuning: LiveData<String>

    fun getCapoText(context: Context): LiveData<String>

    val key: LiveData<String>

    /**
     * The author of the tab not the song
     */
    val author: LiveData<String>

    /**
     * The author of the song, not the tab
     */
    val artist: LiveData<String>

    /**
     * The ID of the song author
     */
    val artistId: LiveData<Int?>

    val version: LiveData<Int>

    val songVersions: LiveData<List<ITab>>

    /**
     * How many steps up or down this tab's content is transposed
     */
    val transpose: LiveData<Int>

    /**
     * The wrapped, transposed tab content to display
     */
    val content: LiveData<AnnotatedString>

    /**
     * The unwrapped, plaintext tab content, for copying to clipboard
     */
    val plainTextContent: LiveData<String>

    /**
     * The font size that should be used to support the custom wrapping for [content]
     */
    val fontSizeSp: LiveData<Float>

    /**
     * The current status of this tab's load process
     */
    val state: LiveData<LoadingState>

    /**
     * Whether we're currently autoscrolling (the Play button has been pressed)
     */
    val autoscrollPaused: LiveData<Boolean>

    val autoScrollSpeedSliderPosition: LiveData<Float>

    /**
     * The delay between 1px scrolls during autoscroll if not [autoscrollPaused]
     */
    val autoscrollDelay: LiveData<Float>

    /**
     * Whether to display the chord fingerings for the current chord
     */
    val chordDetailsActive: LiveData<Boolean>

    /**
     * The title for the chord details section (usually the name of the active chord being displayed)
     */
    val chordDetailsTitle: LiveData<String>

    /**
     * The state of the chord details section (loading until the details have been fetched successfully)
     */
    val chordDetailsState: LiveData<LoadingState>

    /**
     * A list of chord fingerings to be displayed in the chord details section
     */
    val chordDetailsVariations: LiveData<List<ChordVariation>>

    val shareUrl: LiveData<String>

    fun getShareTitle(context: Context): LiveData<String>

    val allPlaylists: LiveData<List<Playlist>>

    val addToPlaylistDialogSelectedPlaylistTitle: LiveData<String?>

    val addToPlaylistDialogConfirmButtonEnabled: LiveData<Boolean>

    /**
     * The selected instrument to display chords for
     */
    val chordInstrument: LiveData<Instrument>

    /**
     * Whether to display chords as flats or sharps
     */
    val useFlats: LiveData<Boolean>
}
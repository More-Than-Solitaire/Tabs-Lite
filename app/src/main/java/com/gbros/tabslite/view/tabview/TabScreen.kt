
package com.gbros.tabslite.view.tabview

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.text.Annotation
import android.text.SpannedString
import android.util.Log
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.KeepScreenOn
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.view.card.ErrorCard
import com.gbros.tabslite.view.chorddisplay.ChordModalBottomSheet
import com.gbros.tabslite.viewmodel.TabViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.pow
import kotlin.math.sqrt

private const val FALLBACK_FONT_SIZE_SP = 14f  // fall back to a font size of 14.sp if the system font size can't be read

//#region use case tab screen

private const val TAB_NAV_ARG = "tabId"
const val TAB_ROUTE_TEMPLATE = "tab/%s"

fun NavController.navigateToTab(tabId: String) {
    navigate(TAB_ROUTE_TEMPLATE.format(tabId))
}

/**
 * Navigate to a tab by tab ID, but replace the current item in the back stack.
 */
fun NavController.swapToTab(tabId: String) {
    navigate(TAB_ROUTE_TEMPLATE.format(tabId)) {
        popUpTo(route = TAB_ROUTE_TEMPLATE.format("{$TAB_NAV_ARG}")) { inclusive = true }
    }
}

fun NavGraphBuilder.tabScreen(
    onNavigateBack: () -> Unit,
    onNavigateToArtistIdSongList: (artistId: String) -> Unit,
    onNavigateToTabVersionById: (id: String) -> Unit,
    onNavigateToEditTab: (songId: String, tabId: String) -> Unit
) {
    composable(
        route = TAB_ROUTE_TEMPLATE.format("{$TAB_NAV_ARG}"),
        arguments = listOf(navArgument(TAB_NAV_ARG) { type = NavType.StringType } )
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments!!.getString(TAB_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)

        // default the font size to whatever the user default font size is.  This respects system font settings.
        val defaultFontSize = MaterialTheme.typography.bodyLarge.fontSize
        val defaultFontSizeInSp = if (defaultFontSize.isSp) {
            defaultFontSize.value
        } else if (defaultFontSize.isEm) {
            defaultFontSize.value / LocalDensity.current.density
        } else {
            FALLBACK_FONT_SIZE_SP
        }

        val urlHandler = LocalUriHandler.current
        val viewModel: TabViewModel = hiltViewModel<TabViewModel, TabViewModel.TabViewModelFactory> { factory -> factory.create(
            tabId = id,
            defaultFontSize = defaultFontSizeInSp,
            dataAccess = db.dataAccess(),
            urlHandler = urlHandler,
            navigateToPlaylistEntryById = { /* ignore playlist navigation because we're not in a playlist */ }
        )}

        TabScreen(
            viewState = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToTabByTabId = onNavigateToTabVersionById,
            onArtistClicked = onNavigateToArtistIdSongList,
            onPlaylistNextSongClick = viewModel::onPlaylistNextSongClick,
            onPlaylistPreviousSongClick = viewModel::onPlaylistPreviousSongClick,
            onTransposeUpClick = viewModel::onTransposeUpClick,
            onTransposeDownClick = viewModel::onTransposeDownClick,
            onTransposeResetClick = viewModel::onTransposeResetClick,
            onTextClick = viewModel::onChordClick,
            onZoom = viewModel::onZoom,
            onChordDetailsDismiss = viewModel::onChordDetailsDismiss,
            onAutoscrollButtonClick = viewModel::onAutoscrollButtonClick,
            onAutoscrollSliderValueChange = viewModel::onAutoscrollSliderValueChange,
            onAutoscrollSliderValueChangeFinished = viewModel::onAutoscrollSliderValueChangeFinished,
            onReload = viewModel::onReload,
            onFavoriteButtonClick = viewModel::onFavoriteButtonClick,
            onAddPlaylistDialogPlaylistSelected = viewModel::onAddPlaylistDialogPlaylistSelected,
            onAddToPlaylist = viewModel::onAddToPlaylist,
            onCreatePlaylist = viewModel::onCreatePlaylist,
            onInstrumentSelected = viewModel::onInstrumentSelected,
            onUseFlatsToggled = viewModel::onUseFlatsToggled,
            onChordsPinnedToggled = viewModel::onChordsPinnedToggled,
            onExportToPdfClick = viewModel::onExportToPdfClick,
            onEditTabClick = onNavigateToEditTab
        )
    }
}

//#endregion

//#region use case playlist entry

private const val PLAYLIST_ENTRY_NAV_ARG = "playlistEntryId"
private const val PLAYLIST_ENTRY_ROUTE_TEMPLATE = "playlist/entry/%s"
private val PLAYLIST_ENTRY_ROUTE = PLAYLIST_ENTRY_ROUTE_TEMPLATE.format("{$PLAYLIST_ENTRY_NAV_ARG}")

fun NavController.navigateToPlaylistEntry(playlistEntryId: Int) {
    navigate(PLAYLIST_ENTRY_ROUTE_TEMPLATE.format(playlistEntryId.toString())) {
        popUpTo(route = PLAYLIST_ENTRY_ROUTE) { inclusive = true }
    }
}

fun NavGraphBuilder.playlistEntryScreen(
    onNavigateToPlaylistEntry: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToArtistIdSongList: (artistId: String) -> Unit,
    onNavigateToTabVersionById: (id: String) -> Unit,
    onNavigateToEditTab: (songId: String, tabId: String) -> Unit
) {
    composable(
        route = PLAYLIST_ENTRY_ROUTE,
        arguments = listOf(navArgument(PLAYLIST_ENTRY_NAV_ARG) { type = NavType.IntType } )
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments!!.getInt(PLAYLIST_ENTRY_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)

        // default the font size to whatever the user default font size is.  This respects system font settings.
        val defaultFontSize = MaterialTheme.typography.bodyLarge.fontSize
        val defaultFontSizeInSp = if (defaultFontSize.isSp) {
            defaultFontSize.value
        } else if (defaultFontSize.isEm) {
            defaultFontSize.value / LocalDensity.current.density
        } else {
            FALLBACK_FONT_SIZE_SP
        }

        val urlHandler = LocalUriHandler.current
        val viewModel: TabViewModel = hiltViewModel<TabViewModel, TabViewModel.TabViewModelFactory> { factory -> factory.create(
            entryId = id,
            defaultFontSize = defaultFontSizeInSp,
            dataAccess = db.dataAccess(),
            urlHandler = urlHandler,
            navigateToPlaylistEntryById = onNavigateToPlaylistEntry
        )}
        TabScreen(
            viewState = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToTabByTabId = onNavigateToTabVersionById,
            onArtistClicked = onNavigateToArtistIdSongList,
            onPlaylistNextSongClick = viewModel::onPlaylistNextSongClick,
            onPlaylistPreviousSongClick = viewModel::onPlaylistPreviousSongClick,
            onTransposeUpClick = viewModel::onTransposeUpClick,
            onTransposeDownClick = viewModel::onTransposeDownClick,
            onTransposeResetClick = viewModel::onTransposeResetClick,
            onTextClick = viewModel::onChordClick,
            onZoom = viewModel::onZoom,
            onChordDetailsDismiss = viewModel::onChordDetailsDismiss,
            onAutoscrollButtonClick = viewModel::onAutoscrollButtonClick,
            onAutoscrollSliderValueChange = viewModel::onAutoscrollSliderValueChange,
            onAutoscrollSliderValueChangeFinished = viewModel::onAutoscrollSliderValueChangeFinished,
            onReload = viewModel::onReload,
            onFavoriteButtonClick = viewModel::onFavoriteButtonClick,
            onAddPlaylistDialogPlaylistSelected = viewModel::onAddPlaylistDialogPlaylistSelected,
            onAddToPlaylist = viewModel::onAddToPlaylist,
            onCreatePlaylist = viewModel::onCreatePlaylist,
            onInstrumentSelected = viewModel::onInstrumentSelected,
            onUseFlatsToggled = viewModel::onUseFlatsToggled,
            onChordsPinnedToggled = viewModel::onChordsPinnedToggled,
            onExportToPdfClick = viewModel::onExportToPdfClick,
            onEditTabClick = onNavigateToEditTab
        )
    }
}

//#endregion

@Composable
fun TabScreen(
    viewState: ITabViewState,
    onNavigateBack: () -> Unit,
    onNavigateToTabByTabId: (id: String) -> Unit,
    onArtistClicked: (artistId: String) -> Unit,
    onPlaylistNextSongClick: () -> Unit,
    onPlaylistPreviousSongClick: () -> Unit,
    onTransposeUpClick: () -> Unit,
    onTransposeDownClick: () -> Unit,
    onTransposeResetClick: () -> Unit,
    onTextClick: (chord: String) -> Unit,
    onZoom: (zoomFactor: Float) -> Unit,
    onChordDetailsDismiss: () -> Unit,
    onAutoscrollSliderValueChange: (Float) -> Unit,
    onAutoscrollButtonClick: () -> Unit,
    onAutoscrollSliderValueChangeFinished: () -> Unit,
    onReload: () -> Unit,
    onFavoriteButtonClick: () -> Unit,
    onAddPlaylistDialogPlaylistSelected: (Playlist) -> Unit,
    onAddToPlaylist: () -> Unit,
    onCreatePlaylist: (title: String, description: String) -> Unit,
    onInstrumentSelected: (instrument: Instrument) -> Unit,
    onUseFlatsToggled: (useFlats: Boolean) -> Unit,
    onChordsPinnedToggled: (pinChords: Boolean) -> Unit,
    onExportToPdfClick: (exportFile: Uri, contentResolver: ContentResolver) -> Unit,
    onEditTabClick: (songId: String, tabId: String) -> Unit
) {
    // handle autoscroll
    val scrollState = rememberLazyListState()
    // create clickable title
    val songName by viewState.songName.observeAsState("...")
    val artistName by viewState.artist.observeAsState("...")
    val currentContext = LocalContext.current
    val artistId = viewState.artistId.observeAsState("").value
    val titleText = remember { currentContext.getText(R.string.tab_title) as SpannedString }
    val annotations = remember { titleText.getSpans(0, titleText.length, Annotation::class.java) }
    val titleBuilder = buildAnnotatedString {
        annotations.forEach { annotation ->
            if (annotation.key == "arg") {
                when (annotation.value) {
                    "songName" -> {
                        append(songName)
                    }  // do nothing to the song name

                    "artistName" -> {
                        // make the artist name clickable
                        withLink(
                            link = LinkAnnotation.Clickable(
                                tag = "artistId",
                                linkInteractionListener = LinkInteractionListener {
                                    Log.d(TAG, "artist $artistId ($artistName) clicked")
                                    if (artistId != null) {
                                        onArtistClicked(artistId)
                                    }
                                }
                            )) {
                            append(artistName)
                        }
                    }

                    "plainText" -> {
                        append(
                            titleText.subSequence(
                                titleText.getSpanStart(annotation),
                                titleText.getSpanEnd(annotation)
                            )
                        )
                    }
                }
            }
        }
    }
    val isPlaylistEntry by viewState.isPlaylistEntry.observeAsState(false)
    val playlistTitle by viewState.playlistTitle.observeAsState("...")
    val songId by viewState.songId.observeAsState()
    val tabId by viewState.tabId.observeAsState()
    val chordsPinned by viewState.chordsPinned.observeAsState(false)

    KeepScreenOn()

    LazyColumn (
        state = scrollState,
        modifier = Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointers = event.changes
                        if (pointers.size > 1) {
                            val zoom = pointers.calculateZoom()
                            if (zoom != 1f) {
                                onZoom(zoom)
                                // don't consume the pointer events so scrolling still works
                            }
                        }
                    }
                }
            }
            .windowInsetsPadding(
                WindowInsets(
                    left = max(4.dp, WindowInsets.safeDrawing.asPaddingValues().calculateLeftPadding(LocalLayoutDirection.current)),
                    right = max(4.dp, WindowInsets.safeDrawing.asPaddingValues().calculateRightPadding(LocalLayoutDirection.current))
                )
            )
    )
    {
        item {
            TabTopAppBar(
                title = titleBuilder.toString(),
                allPlaylists = viewState.allPlaylists.observeAsState(listOf()).value,
                selectedPlaylistTitle = viewState.addToPlaylistDialogSelectedPlaylistTitle.observeAsState(null).value,
                shareUrl = viewState.shareUrl.observeAsState("https://tabslite.com/").value,
                isFavorite = viewState.isFavorite.observeAsState(false).value,
                chordsPinned = chordsPinned,
                onNavigateBack = onNavigateBack,
                onReloadClick = onReload,
                onFavoriteButtonClick = onFavoriteButtonClick,
                onAddToPlaylist = onAddToPlaylist,
                onCreatePlaylist = onCreatePlaylist,
                onPlaylistSelectionChange = onAddPlaylistDialogPlaylistSelected,
                selectPlaylistConfirmButtonEnabled = viewState.addToPlaylistDialogConfirmButtonEnabled.observeAsState(false).value,
                onExportToPdfClick = onExportToPdfClick,
                onChordsPinnedToggled = onChordsPinnedToggled,
                onEditTabClick = {
                    if (songId != null && tabId != null) {
                        onEditTabClick(songId!!, tabId!!)
                    } else {
                        Log.w(TAG, "onEditTabClick called with null songId or tabId: ${viewState.songId.value}, ${viewState.tabId.value}")
                    }
                }
            )

            Text(  // Tab title
                text = titleBuilder,
                style = MaterialTheme.typography.headlineMedium,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp,)
            )
            if (isPlaylistEntry) {
                TabPlaylistNavigation(
                    title = playlistTitle,
                    nextSongButtonEnabled = viewState.playlistNextSongButtonEnabled.observeAsState(false).value,
                    previousSongButtonEnabled = viewState.playlistPreviousSongButtonEnabled.observeAsState(false).value,
                    onNextSongClick = onPlaylistNextSongClick,
                    onPreviousSongClick = onPlaylistPreviousSongClick
                )
            }

            TabSummary(
                difficulty = viewState.difficulty.observeAsState("").value,
                tuning = viewState.tuning.observeAsState("").value,
                capo = viewState.getCapoText(context = LocalContext.current).observeAsState("").value,
                key = viewState.key.observeAsState("").value,
                author = viewState.author.observeAsState("").value,
                version = viewState.version.observeAsState(-1).value,
                songVersions = viewState.songVersions.observeAsState(listOf(Tab(tabId = "198052", version = 3))).value,
                onNavigateToTabById = onNavigateToTabByTabId
            )

            TabTransposeSection(
                currentTransposition = viewState.transpose.observeAsState(0).value,
                onTransposeResetClick = onTransposeResetClick,
                onTransposeUpClick = onTransposeUpClick,
                onTransposeDownClick = onTransposeDownClick
            )
        }

        // Sticky pinned chords header
        if (chordsPinned) {
            stickyHeader {
                val chordVariations = viewState.pinnedChordVariations.observeAsState(emptyList()).value
                val instrument = viewState.chordInstrument.observeAsState(Instrument.Guitar).value

                PinnedChords(
                    chords = chordVariations,
                    instrument = instrument,
                    onChordClick = onTextClick
                )
            }
        }

        // content
        item {
            if (viewState.state.observeAsState(LoadingState.Loading).value is LoadingState.Success) {
                TabText(
                    text = viewState.content.observeAsState(AnnotatedString("")).value,
                    fontSizeSp = viewState.fontSizeSp.observeAsState(FALLBACK_FONT_SIZE_SP).value,
                    onChordClick = onTextClick,
                )
                Spacer(modifier = Modifier.padding(vertical = 24.dp))

                if (isPlaylistEntry) {
                    TabPlaylistNavigation(
                        modifier = Modifier.padding(end = 96.dp),  // extra for the autoscroll button
                        title = playlistTitle,
                        nextSongButtonEnabled = viewState.playlistNextSongButtonEnabled.observeAsState(false).value,
                        previousSongButtonEnabled = viewState.playlistPreviousSongButtonEnabled.observeAsState(false).value,
                        onNextSongClick = onPlaylistNextSongClick,
                        onPreviousSongClick = onPlaylistPreviousSongClick,
                    )
                } else {
                    Spacer(Modifier.padding(vertical = 16.dp))
                }

                Spacer(
                    Modifier.windowInsetsPadding(
                        WindowInsets(
                            bottom = max(
                                WindowInsets.safeDrawing.asPaddingValues()
                                    .calculateBottomPadding() + 16.dp,  // leave room between the navigation bar
                                WindowInsets.safeContent.asPaddingValues().calculateBottomPadding()
                            )  // if we're just leaving room for gestures, that's fine
                        )
                    )
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewState.state.value is LoadingState.Error) {
                        val error = (viewState.state.value as LoadingState.Error)
                        ErrorCard(text = String.format(stringResource(error.messageStringRef), error.errorDetails))
                    } else {
                        CircularProgressIndicator()  // still loading
                    }
                }
            }
        }
    }

    // chord bottom sheet display if a chord was clicked
    if (viewState.chordDetailsActive.observeAsState(false).value) {
        ChordModalBottomSheet(
            title = viewState.chordDetailsTitle.observeAsState("").value,
            chordVariations = viewState.chordDetailsVariations.observeAsState(emptyList()).value,
            instrument = viewState.chordInstrument.observeAsState(Instrument.Guitar).value,
            useFlats = viewState.useFlats.observeAsState(false).value,
            loadingState = viewState.chordDetailsState.observeAsState(LoadingState.Loading).value,
            onDismiss = onChordDetailsDismiss,
            onInstrumentSelected = onInstrumentSelected,
            onUseFlatsToggled = onUseFlatsToggled
        )
    }

    AutoscrollFloatingActionButton(
        sliderValue = viewState.autoScrollSpeedSliderPosition.observeAsState(0.5f).value,
        onButtonClick = onAutoscrollButtonClick,
        onValueChange = onAutoscrollSliderValueChange,
        paused = viewState.autoscrollPaused.observeAsState(false).value,
        onValueChangeFinished = onAutoscrollSliderValueChangeFinished,

        )

    // scroll if autoscroll isn't paused
    if (!viewState.autoscrollPaused.observeAsState(true).value) {
        val autoscrollDelay = viewState.autoscrollDelay.observeAsState(Float.POSITIVE_INFINITY)
        LaunchedEffect(key1 = autoscrollDelay.value) {
            while (isActive) {
                delay(autoscrollDelay.value.toLong())
                if (!scrollState.isScrollInProgress && scrollState.canScrollForward) {  // pause autoscroll while user is manually scrolling
                    scrollState.scrollBy(1f)
                }
            }
        }
    }
}

private fun List<PointerInputChange>.calculateZoom(): Float {
    if (size < 2) return 1f

    val first = this[0]
    val second = this[1]

    val prevDist = sqrt((second.previousPosition.x - first.previousPosition.x).pow(2) + (second.previousPosition.y - first.previousPosition.y).pow(2))
    val currDist = sqrt((second.position.x - first.position.x).pow(2) + (second.position.y - first.position.y).pow(2))

    return if (prevDist > 0f) currDist / prevDist else 1f
}
//#region previews

@Composable @Preview(showBackground = true)
private fun TabViewPreview() {
    data class TabViewStateForTest(
        override val tabId: LiveData<String>,
        override val songId: LiveData<String>,
        override val songName: LiveData<String>,
        override val isFavorite: LiveData<Boolean>,
        override val isPlaylistEntry: LiveData<Boolean>,
        override val playlistTitle: LiveData<String>,
        override val playlistNextSongButtonEnabled: LiveData<Boolean>,
        override val playlistPreviousSongButtonEnabled: LiveData<Boolean>,
        override val difficulty: LiveData<String>,
        override val tuning: LiveData<String>,
        override val key: LiveData<String>,
        override val author: LiveData<String>,
        override val version: LiveData<Int>,
        override val songVersions: LiveData<List<ITab>>,
        override val transpose: LiveData<Int>,
        override val content: LiveData<AnnotatedString>,
        override val state: LiveData<LoadingState>,
        override val autoscrollPaused: LiveData<Boolean>,
        override val autoScrollSpeedSliderPosition: LiveData<Float>,
        override val autoscrollDelay: LiveData<Float>,
        override val chordDetailsActive: LiveData<Boolean>,
        override val chordDetailsTitle: LiveData<String>,
        override val chordDetailsState: LiveData<LoadingState>,
        override val chordDetailsVariations: LiveData<List<ChordVariation>>,
        override val shareUrl: LiveData<String>,
        override val allPlaylists: LiveData<List<Playlist>>,
        override val artist: LiveData<String>,
        override val artistId: LiveData<String?>,
        override val addToPlaylistDialogSelectedPlaylistTitle: LiveData<String?>,
        override val addToPlaylistDialogConfirmButtonEnabled: LiveData<Boolean>,
        override val fontSizeSp: LiveData<Float>,
        override val chordInstrument: LiveData<Instrument>,
        override val useFlats: LiveData<Boolean>,
        override val chordsPinned: LiveData<Boolean>,
        override val pinnedChordVariations: LiveData<List<ChordVariation>>

    ) : ITabViewState {
        constructor(tab: ITab): this(
            tabId = MutableLiveData(tab.tabId),
            songId = MutableLiveData(tab.songId),
            songName = MutableLiveData(tab.songName),
            isFavorite = MutableLiveData(true),
            isPlaylistEntry = MutableLiveData(false),
            playlistTitle = MutableLiveData("none"),
            playlistNextSongButtonEnabled = MutableLiveData(false),
            playlistPreviousSongButtonEnabled = MutableLiveData(false),
            difficulty = MutableLiveData(tab.difficulty),
            tuning = MutableLiveData(tab.tuning),
            key = MutableLiveData(tab.tonalityName),
            author = MutableLiveData(tab.artistName),
            version = MutableLiveData(tab.version),
            songVersions = MutableLiveData(listOf()),
            transpose = MutableLiveData(tab.transpose),
            content = MutableLiveData(AnnotatedString(tab.content)),
            state = MutableLiveData(LoadingState.Success),
            autoscrollPaused = MutableLiveData(true),
            autoScrollSpeedSliderPosition = MutableLiveData(0.5f),
            autoscrollDelay = MutableLiveData(Float.POSITIVE_INFINITY),
            chordDetailsActive = MutableLiveData(false),
            chordDetailsTitle = MutableLiveData("A#m"),
            chordDetailsState = MutableLiveData(LoadingState.Loading),
            chordDetailsVariations = MutableLiveData(listOf()),
            shareUrl = MutableLiveData("https://tabslite.com/tab/1234"),
            allPlaylists = MutableLiveData(listOf()),
            artist = MutableLiveData("Artist Name"),
            artistId = MutableLiveData("1"),
            addToPlaylistDialogSelectedPlaylistTitle = MutableLiveData("Playlist1"),
            addToPlaylistDialogConfirmButtonEnabled = MutableLiveData(false),
            fontSizeSp = MutableLiveData(FALLBACK_FONT_SIZE_SP),
            chordInstrument = MutableLiveData(Instrument.Guitar),
            useFlats = MutableLiveData(false),
            chordsPinned = MutableLiveData(true),
            pinnedChordVariations = MutableLiveData(emptyList())
        )

        override fun getCapoText(context: Context): LiveData<String> {
            return MutableLiveData("4th fret")
        }

        override fun getShareTitle(context: Context): LiveData<String> {
            return MutableLiveData("Song Name by Author (test)")
        }
    }

    val hallelujahTabForTest = """
        [Intro]
        [ch]C[/ch] [ch]Em[/ch] [ch]C[/ch] [ch]Em[/ch]
         
        [Verse]
        [tab][ch]C[/ch]                [ch]Em[/ch]
          Hey there Delilah, What’s it like in New York City?[/tab]
        [tab]      [ch]C[/ch]                                      [ch]Em[/ch]                                  [ch]Am[/ch]   [ch]G[/ch]
        I’m a thousand miles away, But girl tonight you look so pretty, Yes you do, [/tab]
        
        [tab]F                   [ch]G[/ch]                  [ch]Am[/ch]
          Time Square can’t shine as bright as you, [/tab]
        [tab]             [ch]G[/ch]
        I swear it’s true. [/tab]
        [tab][ch]C[/ch]
          Hey there Delilah, [/tab]
        [tab]          [ch]Em[/ch]
        Don’t you worry about the distance, [/tab]
        [tab]          [ch]C[/ch]
        I’m right there if you get lonely, [/tab]
        [tab]          [ch]Em[/ch]
        [ch]G[/ch]ive this song another listen, [/tab]
        [tab]           [ch]Am[/ch]     [ch]G[/ch]
        Close your eyes, [/tab]
        [tab]F              [ch]G[/ch]                [ch]Am[/ch]
          Listen to my voice it’s my disguise, [/tab]
        [tab]            [ch]G[/ch]
        I’m by your side.[/tab]    """.trimIndent()

    val tabForTest = TabWithDataPlaylistEntry(1, 1, "1", 1, 1, 1234, "0", "Long Time Ago", "rock","CoolGuyz", "1", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , "public", "C", "E A D G B E", false, ArrayList(), "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")


    AppTheme {
        TabScreen(
            viewState = TabViewStateForTest(tabForTest),
            onNavigateBack = { },
            onPlaylistNextSongClick = { },
            onPlaylistPreviousSongClick = { },
            onTransposeUpClick = { },
            onTransposeDownClick = { },
            onTransposeResetClick = { },
            onTextClick = { },
            onChordDetailsDismiss = { },
            onAutoscrollSliderValueChange = { },
            onAutoscrollButtonClick = { },
            onAutoscrollSliderValueChangeFinished = { },
            onReload = { },
            onFavoriteButtonClick = { },
            onAddPlaylistDialogPlaylistSelected = { },
            onAddToPlaylist = { },
            onCreatePlaylist = { _, _ -> },
            onZoom = { },
            onInstrumentSelected = { },
            onUseFlatsToggled = { },
            onArtistClicked = { },
            onExportToPdfClick = { _, _ -> },
            onNavigateToTabByTabId = { _ -> },
            onChordsPinnedToggled = { },
            onEditTabClick = { _, _ -> }
        )
    }
}

//#endregion

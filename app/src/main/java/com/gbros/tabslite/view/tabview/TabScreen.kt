package com.gbros.tabslite.view.tabview

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.ErrorCard
import com.gbros.tabslite.view.chorddisplay.ChordModalBottomSheet
import com.gbros.tabslite.viewmodel.TabViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val LOG_NAME = "tabslite.TabScreen     "

//#region use case tab screen

private const val TAB_NAV_ARG = "tabId"
const val TAB_ROUTE_TEMPLATE = "tab/%s"

fun NavController.navigateToTab(tabId: Int) {
    navigate(TAB_ROUTE_TEMPLATE.format(tabId.toString()))
}

fun NavGraphBuilder.tabScreen(
    onNavigateToPlaylistEntry: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        route = TAB_ROUTE_TEMPLATE.format("{$TAB_NAV_ARG}"),
        arguments = listOf(navArgument(TAB_NAV_ARG) { type = NavType.IntType } )
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments!!.getInt(TAB_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: TabViewModel = hiltViewModel<TabViewModel, TabViewModel.TabViewModelFactory> { factory -> factory.create(id, false, db.dataAccess(), onNavigateToPlaylistEntry)}

        TabScreen(
            viewState = viewModel,
            onNavigateBack = onNavigateBack,
            onPlaylistNextSongClick = viewModel::onPlaylistNextSongClick,
            onPlaylistPreviousSongClick = viewModel::onPlaylistPreviousSongClick,
            onTransposeUpClick = viewModel::onTransposeUpClick,
            onTransposeDownClick = viewModel::onTransposeDownClick,
            onTransposeResetClick = viewModel::onTransposeResetClick,
            onTextClick = viewModel::onContentClick,
            onScreenMeasured = viewModel::onScreenMeasured,
            onChordDetailsDismiss = viewModel::onChordDetailsDismiss,
            onAutoscrollButtonClick = viewModel::onAutoscrollButtonClick,
            onAutoscrollSliderValueChange = viewModel::onAutoscrollSliderValueChange,
            onAutoscrollSliderValueChangeFinished = viewModel::onAutoscrollSliderValueChangeFinished,
            onReload = viewModel::onReload,
            onFavoriteButtonClick = viewModel::onFavoriteButtonClick,
            onAddPlaylistDialogPlaylistSelected = viewModel::onAddPlaylistDialogPlaylistSelected,
            onAddToPlaylist = viewModel::onAddToPlaylist,
            onCreatePlaylist = viewModel::onCreatePlaylist
        )
    }
}

//#endregion

//#region use case playlist entry

private const val PLAYLIST_ENTRY_NAV_ARG = "playlistEntryId"
const val PLAYLIST_ENTRY_ROUTE_TEMPLATE = "playlist/entry/%s"

fun NavController.navigateToPlaylistEntry(playlistEntryId: Int) {
    navigate(PLAYLIST_ENTRY_ROUTE_TEMPLATE.format(playlistEntryId.toString())) {
        launchSingleTop = true  // don't include multiple instances of this destination in the back stack
    }
}

fun NavGraphBuilder.playlistEntryScreen(
    onNavigateToPlaylistEntry: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        route = PLAYLIST_ENTRY_ROUTE_TEMPLATE.format("{$PLAYLIST_ENTRY_NAV_ARG}"),
        arguments = listOf(navArgument(PLAYLIST_ENTRY_NAV_ARG) { type = NavType.IntType } )
    ) { navBackStackEntry ->
        val id = navBackStackEntry.arguments!!.getInt(PLAYLIST_ENTRY_NAV_ARG)
        val db = AppDatabase.getInstance(LocalContext.current)
        val viewModel: TabViewModel = hiltViewModel<TabViewModel, TabViewModel.TabViewModelFactory> { factory -> factory.create(id, true, db.dataAccess(), onNavigateToPlaylistEntry)}
        TabScreen(
            viewState = viewModel,
            onNavigateBack = onNavigateBack,
            onPlaylistNextSongClick = viewModel::onPlaylistNextSongClick,
            onPlaylistPreviousSongClick = viewModel::onPlaylistPreviousSongClick,
            onTransposeUpClick = viewModel::onTransposeUpClick,
            onTransposeDownClick = viewModel::onTransposeDownClick,
            onTransposeResetClick = viewModel::onTransposeResetClick,
            onTextClick = viewModel::onContentClick,
            onScreenMeasured = viewModel::onScreenMeasured,
            onChordDetailsDismiss = viewModel::onChordDetailsDismiss,
            onAutoscrollButtonClick = viewModel::onAutoscrollButtonClick,
            onAutoscrollSliderValueChange = viewModel::onAutoscrollSliderValueChange,
            onAutoscrollSliderValueChangeFinished = viewModel::onAutoscrollSliderValueChangeFinished,
            onReload = viewModel::onReload,
            onFavoriteButtonClick = viewModel::onFavoriteButtonClick,
            onAddPlaylistDialogPlaylistSelected = viewModel::onAddPlaylistDialogPlaylistSelected,
            onAddToPlaylist = viewModel::onAddToPlaylist,
            onCreatePlaylist = viewModel::onCreatePlaylist
        )
    }
}

//#endregion

//#region view

@Composable
fun TabScreen(
    viewState: ITabViewState,
    onNavigateBack: () -> Unit,
    onPlaylistNextSongClick: () -> Unit,
    onPlaylistPreviousSongClick: () -> Unit,
    onTransposeUpClick: () -> Unit,
    onTransposeDownClick: () -> Unit,
    onTransposeResetClick: () -> Unit,
    onTextClick: (Int, UriHandler, ClipboardManager) -> Unit,
    onScreenMeasured: (screenWidth: Int, localDensity: Density, fontSizeSp: Float, colorScheme: ColorScheme) -> Unit,
    onChordDetailsDismiss: () -> Unit,
    onAutoscrollSliderValueChange: (Float) -> Unit,
    onAutoscrollButtonClick: () -> Unit,
    onAutoscrollSliderValueChangeFinished: () -> Unit,
    onReload: () -> Unit,
    onFavoriteButtonClick: () -> Unit,
    onAddPlaylistDialogPlaylistSelected: (Playlist) -> Unit,
    onAddToPlaylist: () -> Unit,
    onCreatePlaylist: (title: String, description: String) -> Unit
) {

    // handle autoscroll
    val scrollState = rememberScrollState()

    KeepScreenOn()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        val title = String.format(format = stringResource(R.string.tab_title), viewState.songName.observeAsState("...").value, viewState.artist.observeAsState("...").value)
        TabTopAppBar(
            title = title,
            allPlaylists = viewState.allPlaylists.observeAsState(listOf()).value,
            selectedPlaylistTitle = viewState.addToPlaylistDialogSelectedPlaylistTitle.observeAsState(
                null
            ).value,
            shareTitle = title,
            shareUrl = viewState.shareUrl.observeAsState("https://tabslite.com/").value,
            isFavorite = viewState.isFavorite.observeAsState(false).value,
            onNavigateBack = onNavigateBack,
            onReloadClick = onReload,
            onFavoriteButtonClick = onFavoriteButtonClick,
            onAddToPlaylist = onAddToPlaylist,
            onCreatePlaylist = onCreatePlaylist,
            onPlaylistSelectionChange = onAddPlaylistDialogPlaylistSelected,
            selectPlaylistConfirmButtonEnabled = viewState.addToPlaylistDialogConfirmButtonEnabled.observeAsState(false).value
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            if (viewState.isPlaylistEntry) {
                TabPlaylistNavigation(
                    title = viewState.playlistTitle.observeAsState("").value,
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
                author = viewState.author.observeAsState("").value
            )

            TabTransposeSection(
                currentTransposition = viewState.transpose.observeAsState(0).value,
                onTransposeResetClick = onTransposeResetClick,
                onTransposeUpClick = onTransposeUpClick,
                onTransposeDownClick = onTransposeDownClick
            )

            // content
            if (viewState.state.observeAsState(LoadingState.Loading).value is LoadingState.Success) {
                TabText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 64.dp),
                    text = viewState.content.observeAsState(AnnotatedString("")).value,
                    onTextClick = onTextClick,
                    onScreenMeasured = onScreenMeasured
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewState.state.value is LoadingState.Error) {
                        ErrorCard(text = (viewState.state.value as LoadingState.Error).message)
                    } else {
                        CircularProgressIndicator()  // still loading
                    }
                }
            }
        }

        // chord bottom sheet display if a chord was clicked
        if (viewState.chordDetailsActive.observeAsState(false).value) {
            ChordModalBottomSheet(title = viewState.chordDetailsTitle.observeAsState("").value,
                chordVariations = viewState.chordDetailsVariations.observeAsState(emptyList()).value,
                loadingState = viewState.chordDetailsState.observeAsState(LoadingState.Loading).value,
                onDismiss = onChordDetailsDismiss)
        }
    }

    AutoscrollFloatingActionButton(
        sliderValue = viewState.autoScrollSpeedSliderPosition.observeAsState(0.5f).value,
        onButtonClick = onAutoscrollButtonClick,
        onValueChange = onAutoscrollSliderValueChange,
        paused = viewState.autoscrollPaused.observeAsState(false).value,
        onValueChangeFinished = onAutoscrollSliderValueChangeFinished
    )

    // scroll if autoscroll isn't paused
    if (!viewState.autoscrollPaused.observeAsState(true).value) {
        val autoscrollDelay = viewState.autoscrollDelay.observeAsState(Float.POSITIVE_INFINITY)
        LaunchedEffect(key1 = autoscrollDelay.value) {
            val maxScrollValue = scrollState.maxValue
            while (isActive) {
                delay(autoscrollDelay.value.toLong())
                if (!scrollState.isScrollInProgress) {  // pause autoscroll while user is manually scrolling
                    val newScrollPosition = scrollState.value + 1

                    if (newScrollPosition > maxScrollValue) {
                        // we got to the end of the song; skip scrolling to minimize jitters
                        continue
                    }
                    scrollState.scrollTo(newScrollPosition)
                }
            }
        }
    }
}

@Composable @Preview
private fun TabViewPreview() {

    data class TabViewStateForTest(
        override val songName: LiveData<String>,
        override val isFavorite: LiveData<Boolean>,
        override val isPlaylistEntry: Boolean,
        override val playlistTitle: LiveData<String>,
        override val playlistNextSongButtonEnabled: LiveData<Boolean>,
        override val playlistPreviousSongButtonEnabled: LiveData<Boolean>,
        override val difficulty: LiveData<String>,
        override val tuning: LiveData<String>,
        override val key: LiveData<String>,
        override val author: LiveData<String>,
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
        override val addToPlaylistDialogSelectedPlaylistTitle: LiveData<String?>,
        override val addToPlaylistDialogConfirmButtonEnabled: LiveData<Boolean>
    ) : ITabViewState {
        constructor(tab: ITab): this(
            songName = MutableLiveData(tab.songName),
            isFavorite = MutableLiveData(true),
            isPlaylistEntry = false,
            playlistTitle = MutableLiveData("none"),
            playlistNextSongButtonEnabled = MutableLiveData(false),
            playlistPreviousSongButtonEnabled = MutableLiveData(false),
            difficulty = MutableLiveData(tab.difficulty),
            tuning = MutableLiveData(tab.tuning),
            key = MutableLiveData(tab.tonalityName),
            author = MutableLiveData(tab.artistName),
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
            addToPlaylistDialogSelectedPlaylistTitle = MutableLiveData("Playlist1"),
            addToPlaylistDialogConfirmButtonEnabled = MutableLiveData(false)
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

    val tabForTest = TabWithDataPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = hallelujahTabForTest)


    AppTheme {
        TabScreen(
            viewState = TabViewStateForTest(tabForTest),
            onNavigateBack = {},
            onPlaylistNextSongClick = {  },
            onPlaylistPreviousSongClick = {  },
            onTransposeUpClick = {  },
            onTransposeDownClick = {  },
            onTransposeResetClick = {  },
            onTextClick = { _, _, _ -> },
            onScreenMeasured = { _, _, _, _ -> },
            onChordDetailsDismiss = {  },
            onAutoscrollSliderValueChange = {  },
            onAutoscrollButtonClick = {  },
            onAutoscrollSliderValueChangeFinished = {  },
            onReload = {  },
            onFavoriteButtonClick = {  },
            onAddPlaylistDialogPlaylistSelected = {  },
            onAddToPlaylist = {  },
            onCreatePlaylist = { _, _ -> }
        )
    }
}



//#endregion

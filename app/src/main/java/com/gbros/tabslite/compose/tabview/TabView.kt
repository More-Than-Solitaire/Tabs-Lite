package com.gbros.tabslite.compose.tabview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.compose.addtoplaylistdialog.AddToPlaylistDialog
import com.gbros.tabslite.compose.chorddisplay.ChordModalBottomSheet
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.chord.CompleteChord
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val LOG_NAME = "tabslite.TabView    "

@Composable
fun TabView(tab: ITab, navigateBack: () -> Unit) {
    var transposedContent by remember(key1 = tab.content) {mutableStateOf(tab.content)}
    var transposeLevel by remember(key1 = tab.content) { mutableIntStateOf(tab.transpose) }

    // handle chord clicks
    var chordToShow by remember { mutableStateOf("") }
    var bottomSheetTrigger by remember { mutableStateOf(false) }
    val currentContext = LocalContext.current
    val db: AppDatabase = remember {AppDatabase.getInstance(currentContext) }

    // handle autoscroll
    val scrollState = rememberScrollState()

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    Column(
    modifier = Modifier
        .verticalScroll(scrollState)
    ) {

        TabTopAppBar(tab = tab, navigateBack = navigateBack)
        if (tab is TabWithPlaylistEntry && tab.playlistId > 0) {
            TabPlaylistNavigation(tab = tab)
        }
        TabSummary(tab = tab)
        TabTransposeSection(currentTransposition = transposeLevel) {
            tab.transpose(it)
//            transposeLevel += it
            transposedContent = tab.content
        }

        // content
        TabText(text = transposedContent) { chord ->
            chordToShow = chord
            bottomSheetTrigger = true
        }

        // chord bottom sheet display if a chord was clicked
        if (bottomSheetTrigger) {
            ChordModalBottomSheet(chords = CompleteChord(db, chordToShow)) {
                bottomSheetTrigger = false
            }
        }
    }

    val scrollSpeed = remember { mutableFloatStateOf(1.0f) }
    val autoscrollEnabled = remember { mutableStateOf(false)}
    var forcePauseScroll by remember{mutableStateOf(false)}
    AutoscrollFloatingActionButton(
        onPlay = { initialSpeed ->
            scrollSpeed.floatValue = initialSpeed
            autoscrollEnabled.value = true
        },
        onPause = {
            autoscrollEnabled.value = false
            forcePauseScroll = false  // ensure we can still manually start autoscroll again
        },
        onSpeedChange = { newSpeed -> scrollSpeed.floatValue = newSpeed },
        forcePause = forcePauseScroll
    )

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            tabId = tab.tabId,
            transpose = tab.transpose,
            onConfirm = { showAddToPlaylistDialog = false },
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    if (autoscrollEnabled.value) {
        LaunchedEffect(key1 = scrollSpeed.floatValue) {
            val maxScrollValue = scrollState.maxValue
            while (isActive) {
                delay((100 / scrollSpeed.floatValue).toLong())
                if (!scrollState.isScrollInProgress) {  // pause autoscroll while user is manually scrolling
                    val newScrollPosition = scrollState.value + 1

                    if (newScrollPosition > maxScrollValue) {
                        // we got to the end of the song; pause autoscroll
                        forcePauseScroll = true
                        break
                    }
                    scrollState.scrollTo(newScrollPosition)
                }
            }
        }
    }

    // update transpose in database
    LaunchedEffect(key1 = transposeLevel) {
        if (tab is TabWithPlaylistEntry) {
            db.playlistEntryDao().updateEntryTransposition(tab.entryId, tab.transpose)
        }
    }
}

@Composable @Preview
private fun TabViewPreview() {
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

    val tabForTest = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = hallelujahTabForTest)
    AppTheme {
        TabView(tab = tabForTest, navigateBack = {})
    }
}

package com.gbros.tabslite.compose.tabview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.compose.ErrorCard
import com.gbros.tabslite.compose.addtoplaylistdialog.AddToPlaylistDialog
import com.gbros.tabslite.compose.chorddisplay.ChordModalBottomSheet
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val LOG_NAME = "tabslite.TabView      "

@Composable
fun TabView(tab: ITab?, navigateBack: () -> Unit, navigateToTabByPlaylistEntryId: (id: Int) -> Unit) {
    val myTab = tab ?: Tab()
    var transposedContent by remember(key1 = myTab.content) {mutableStateOf(myTab.content)}

    // handle chord clicks
    var chordToShow by remember { mutableStateOf("") }
    val currentContext = LocalContext.current
    val db: AppDatabase = remember {AppDatabase.getInstance(currentContext) }

    // handle autoscroll
    val scrollState = rememberScrollState()

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    // handle reload
    var loading by remember(transposedContent) { mutableStateOf(transposedContent.isBlank()) }
    var reloadTab by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = reloadTab) {
        if (reloadTab) {
            loading = true
            try {
                myTab.fetchFullTab(db, true)
            } catch (ex: Exception) {
                Log.w(LOG_NAME, "Tab reload failed", ex)
            }
            loading = false
        }
    }

    Column(
    modifier = Modifier
        .verticalScroll(scrollState)
        .background(color = MaterialTheme.colorScheme.background)
    ) {
        TabTopAppBar(tab = myTab, navigateBack = navigateBack, reload = {reloadTab = true})

        Column(
            modifier = Modifier
                .padding(horizontal = 2.dp)
        ) {
            Text(
                text = if (tab != null) stringResource(
                    R.string.tab_title,
                    tab.songName,
                    tab.artistName
                ) else "",
                style = MaterialTheme.typography.headlineMedium,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            if (myTab is TabWithPlaylistEntry && myTab.playlistId > 0) {
                TabPlaylistNavigation(
                    tab = myTab,
                    navigateToTabByPlaylistEntryId = navigateToTabByPlaylistEntryId
                )
            }
            TabSummary(tab = myTab)
            TabTransposeSection(currentTransposition = myTab.transpose) {
                myTab.transpose(it)
                transposedContent = myTab.content
            }

            // content
            if (!loading && transposedContent.isNotBlank()) {
                TabText(
                    text = transposedContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 64.dp)
                ) { chord ->
                    chordToShow = chord
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 24.dp), contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator()
                        LaunchedEffect(key1 = Unit) {
                            delay(5000)
                            loading = false // loading failed after 5 seconds
                        }
                    } else {
                        ErrorCard(text = "Couldn't load tab.  Please check your internet connection.")
                    }
                }
            }
        }

        // chord bottom sheet display if a chord was clicked
        if (chordToShow.isNotBlank()) {
            ChordModalBottomSheet(chord = chordToShow, onDismiss = { chordToShow = "" })
        }
    }

    var autoscrollDelay by remember { mutableFloatStateOf(1.0f) }
    var autoscrollEnabled by remember { mutableStateOf(false)}
    var forcePauseScroll by remember{mutableStateOf(false)}
    AutoscrollFloatingActionButton(
        onPlay = { initialSpeed ->
            autoscrollDelay = initialSpeed
            autoscrollEnabled = true
        },
        onPause = {
            autoscrollEnabled = false
            forcePauseScroll = false  // ensure we can still manually start autoscroll again
        },
        onValueChange = { newValue -> autoscrollDelay = newValue },
        forcePause = forcePauseScroll
    )

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            tabId = myTab.tabId,
            transpose = myTab.transpose,
            onConfirm = { showAddToPlaylistDialog = false },
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    if (autoscrollEnabled) {
        KeepScreenOn()

        LaunchedEffect(key1 = autoscrollDelay) {
            val maxScrollValue = scrollState.maxValue
            while (isActive) {
                delay(autoscrollDelay.toLong())
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
    LaunchedEffect(key1 = myTab.transpose) {
        if (myTab is TabWithPlaylistEntry) {
            db.playlistEntryDao().updateEntryTransposition(myTab.entryId, myTab.transpose)
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
        TabView(tab = tabForTest, navigateBack = {}, navigateToTabByPlaylistEntryId = {})
    }
}

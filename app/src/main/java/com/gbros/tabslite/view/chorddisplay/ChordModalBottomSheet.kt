package com.gbros.tabslite.view.chorddisplay

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chrynan.chords.model.ChordMarker
import com.chrynan.chords.model.Finger
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringNumber
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.ErrorCard
import com.gbros.tabslite.view.tabview.TabText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordModalBottomSheet(
    title: String,
    chordVariations: List<ChordVariation>,
    loadingState: LoadingState,
    onDismiss: () -> Unit
){
    var loading by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val screenWidth = LocalConfiguration.current.smallestScreenWidthDp
    val screenHeight = LocalConfiguration.current.screenWidthDp
    val startPadding = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) (screenHeight - screenWidth - 16).dp else 0.dp

    ModalBottomSheet(
        modifier = Modifier.padding(start = startPadding),
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        sheetMaxWidth = screenWidth.dp
    ) {
        if (loadingState is LoadingState.Success) {
            loading = false
            ChordPager(
                chordVariations = chordVariations,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            // show loading progress indicator
            Box(
                modifier = Modifier
                    .height(312.dp)  // this is the size of the components above added together, minus the text
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (loadingState is LoadingState.Error) {
                    ErrorCard(
                        text = String.format(
                            stringResource(id = R.string.message_chord_load_failed),
                            title
                        )
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }

}

@Composable @Preview
private fun ChordModalBottomSheetPreview () {
    AppTheme {
        val testCase1 = AnnotatedString("""
        [tab]     [ch]C[/ch]                   [ch]Am[/ch] 
        That David played and it pleased the Lord[/tab]
    """.trimIndent())
        var bottomSheetTrigger by remember { mutableStateOf(true) }
        var chordToShow by remember { mutableStateOf("Am") }
        val chords = listOf(
        ChordVariation("varid1234", "Am",
            arrayListOf(
                ChordMarker.Note(FretNumber(1), Finger.INDEX, StringNumber(4)),
                ChordMarker.Note(FretNumber(2), Finger.MIDDLE, StringNumber(3)),
                ChordMarker.Note(FretNumber(2), Finger.RING, StringNumber(2))
            ),
            arrayListOf(
                ChordMarker.Open(StringNumber(1)),
                ChordMarker.Open(StringNumber(5))
            ),
            arrayListOf(
                ChordMarker.Muted(StringNumber(6))
            ),
            arrayListOf()
        ),
        ChordVariation("varid1234", "Am",
            arrayListOf(
                ChordMarker.Note(FretNumber(1), Finger.INDEX, StringNumber(4)),
                ChordMarker.Note(FretNumber(2), Finger.MIDDLE, StringNumber(3)),
                ChordMarker.Note(FretNumber(2), Finger.RING, StringNumber(2))
            ),
            arrayListOf(
                ChordMarker.Open(StringNumber(1)),
                ChordMarker.Open(StringNumber(5))
            ),
            arrayListOf(
                ChordMarker.Muted(StringNumber(6))
            ),
            arrayListOf()
        ),
        ChordVariation("varid1234", "Am",
            arrayListOf(
                ChordMarker.Note(FretNumber(1), Finger.INDEX, StringNumber(4)),
                ChordMarker.Note(FretNumber(2), Finger.MIDDLE, StringNumber(3)),
                ChordMarker.Note(FretNumber(2), Finger.RING, StringNumber(2))
            ),
            arrayListOf(
                ChordMarker.Open(StringNumber(1)),
                ChordMarker.Open(StringNumber(5))
            ),
            arrayListOf(
                ChordMarker.Muted(StringNumber(6))
            ),
            arrayListOf()
        )
    )

        TabText(
            text = testCase1,
            fontSizeSp = 14f,
            onTextClick = { _, _, _ ->
                chordToShow = "Am"
                bottomSheetTrigger = true
            },
            onScreenMeasured = {_, _, _->},
            onZoom = {},
            modifier = Modifier.fillMaxSize()
        )

        if (bottomSheetTrigger) {
            ChordModalBottomSheet(
                title = chordToShow,
                chordVariations = chords,
                loadingState = LoadingState.Success,
                onDismiss = { }
            )
        }
    }
}

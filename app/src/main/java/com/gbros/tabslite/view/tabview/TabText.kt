package com.gbros.tabslite.view.tabview

import android.os.Build
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.chrynan.chords.model.ChordMarker
import com.chrynan.chords.model.Finger
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringNumber
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.chorddisplay.ChordModalBottomSheet
import com.smarttoolfactory.gesture.detectTransformGestures

@Composable
fun TabText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    fontSizeSp: Float,
    onTextClick: (clickLocation: Int, uriHandler: UriHandler, clipboardManager: Clipboard) -> Unit,
    onScreenMeasured: (screenWidth: Int, localDensity: Density, colorScheme: ColorScheme) -> Unit,
    onZoom: (zoomFactor: Float) -> Unit
){
    val font = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Only Android 8+ supports variable weight fonts
            FontFamily(Font(R.font.roboto_mono_variable_weight))
        } else {
            FontFamily(Font(R.font.roboto_mono_regular))
        }
    }
    val localDensity = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboard.current

    ClickableText(
        text = text,
        style = TextStyle(
            fontFamily = font,
            fontSize = TextUnit(fontSizeSp, TextUnitType.Sp),
            color = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures(consume = false, onGesture = { _, _, zoom, _, _, _ ->
                    if (zoom != 1.0f) {
                        onZoom(zoom)
                    }
                })
            }
            .onGloballyPositioned { layoutResult ->
                onScreenMeasured(layoutResult.size.width, localDensity, colorScheme)
            },
        onClick = { clickLocation ->
            onTextClick(clickLocation, uriHandler, clipboardManager)
        }
    )
}

@Composable @Preview
fun TabTextTestCase1() {
    AppTheme {
        val oneLine = AnnotatedString("""
        [tab]     [ch]C[/ch]                   [ch]Am[/ch] 
        That David played and it pleased the Lord[/tab]
    """.trimIndent())
        var bottomSheetTrigger by remember { mutableStateOf(false) }

        TabText(
            text = oneLine,
            fontSizeSp = 14f,
            onTextClick = { _, _, _ ->
                bottomSheetTrigger = true
            },
            onScreenMeasured = { _, _, _->},
            onZoom = {}
        )
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
                arrayListOf(),
                Instrument.Guitar
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
                arrayListOf(),
                Instrument.Guitar
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
                arrayListOf(),
                Instrument.Guitar
            )
        )

        if (bottomSheetTrigger) {
            ChordModalBottomSheet(
                title = "Am",
                chordVariations = chords,
                loadingState = LoadingState.Success,
                instrument = Instrument.Guitar,
                useFlats = false,
                onInstrumentSelected = { },
                onDismiss =  { bottomSheetTrigger = false },
                onUseFlatsToggled = { }
            )
        }
    }
}

@Composable @Preview
fun TabTextPreview() {
    val hallelujahTabForTest = AnnotatedString("""
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
        I’m by your side.[/tab]    """.trimIndent())

    AppTheme {
        TabText(
            text = hallelujahTabForTest,
            fontSizeSp = 14f,
            onTextClick = {_, _, _ ->},
            onScreenMeasured = { _, _, _->},
            onZoom = {}
        )
    }
}

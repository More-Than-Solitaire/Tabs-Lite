package com.gbros.tabslite.compose.chorddisplay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chrynan.chords.compose.ChordWidget
import com.chrynan.chords.model.ChordChart
import com.chrynan.chords.model.ChordMarker
import com.chrynan.chords.model.ChordViewData
import com.chrynan.chords.model.Finger
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringLabelState
import com.chrynan.chords.model.StringNumber
import com.chrynan.colors.RgbaColor
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalUnsignedTypes::class)
@Composable
fun ChordPager(modifier: Modifier = Modifier, chordVariations: List<ChordVariation>) {
    if (chordVariations.isNotEmpty()) {
        HorizontalIndicatorPager(modifier = modifier, pageCount = chordVariations.size) { page ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = chordVariations[page].chordId,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth(),
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .height(240.dp)
                ) {
                    ChordWidget(
                        chord = chordVariations[page].toChrynanChord(),
                        chart = ChordChart.STANDARD_TUNING_GUITAR_CHART,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        viewData = ChordViewData(
                            noteColor = MaterialTheme.colorScheme.primary.toChrynanRgba(),
                            noteLabelTextColor = MaterialTheme.colorScheme.onPrimary.toChrynanRgba(),
                            fretColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                            fretLabelTextColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                            stringColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                            stringLabelTextColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                            stringLabelState = StringLabelState.SHOW_LABEL,
                            fitToHeight = true
                        )
                    )
                }
            }
        }
    }
}

fun Color.toChrynanRgba() : RgbaColor {
    return RgbaColor(red, green, blue, alpha)
}

//region preview

@Composable @Preview
fun ChordPagerPreview() {
    /**
     * Automatically add these chords to an empty constructor
     */
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

    AppTheme {
        ChordPager(chordVariations = chords)
    }
}

//endregion

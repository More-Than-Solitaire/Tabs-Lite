package com.gbros.tabslite.view.chorddisplay

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.chrynan.chords.util.maxFret
import com.chrynan.chords.util.minFret
import com.chrynan.colors.RgbaColor
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.TAG
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class, ExperimentalUnsignedTypes::class)
@Composable
fun ChordPager(
    title: String,
    modifier: Modifier = Modifier,
    chordVariations: List<ChordVariation>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth(),
        )

        HorizontalIndicatorPager(
            modifier = modifier,
            pageCount = chordVariations.size
        ) { page ->

            val chord = chordVariations[page].toChrynanChord()
            // set which frets are shown for this chord
            val defaultMaxFret = 3
            val defaultMinFret = 1
            val endFret = max(
                chord.maxFret,
                defaultMaxFret
            )                                 // last fret shown
            val startFret = min(
                chord.minFret,
                max(chord.maxFret - 2, defaultMinFret)
            )    // first fret shown

            // get the chart layout based on the selected instrument
            val chartLayout = if (chordVariations[page].instrument == Instrument.Guitar) {
                ChordChart.STANDARD_TUNING_GUITAR_CHART.copy(
                    fretStart = FretNumber(startFret),
                    fretEnd = FretNumber(endFret)
                )
            } else if (chordVariations[page].instrument == Instrument.Ukulele) {
                ChordChart.STANDARD_TUNING_UKELELE.copy(
                    fretStart = FretNumber(startFret),
                    fretEnd = FretNumber(endFret)
                )
            } else {
                Log.e(TAG, "Invalid instrument selection: ${chordVariations[page].instrument}, defaulting to guitar")
                ChordChart.STANDARD_TUNING_GUITAR_CHART.copy(
                    fretStart = FretNumber(startFret),
                    fretEnd = FretNumber(endFret)
                )
            }

            ChordWidget(
                chord = chord,
                chart = chartLayout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
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

fun Color.toChrynanRgba() : RgbaColor {
    return RgbaColor(red, green, blue, alpha)
}

//region preview

@Preview
@Composable
fun UkulelePreview() {
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
            ),
            arrayListOf(),
            arrayListOf(),
            Instrument.Ukulele
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
            Instrument.Ukulele
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
            Instrument.Ukulele
        )
    )

    AppTheme {
        ChordPager(title = "Am", chordVariations = chords)
    }
}

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

    AppTheme {
        ChordPager(title = "Am", chordVariations = chords)
    }
}


@Composable @Preview
fun ChordPagerBarredChordsPreview() {
    /**
     * Automatically add these chords to an empty constructor
     */
    val chords = listOf(
        ChordVariation("varid1234", "Am",
            noteChordMarkers = arrayListOf(
                ChordMarker.Note(FretNumber(4), Finger.MIDDLE, StringNumber(2)),
                ChordMarker.Note(FretNumber(5), Finger.RING, StringNumber(3)),
                ChordMarker.Note(FretNumber(5), Finger.PINKY, StringNumber(4))
            ),
            openChordMarkers = arrayListOf(),
            mutedChordMarkers = arrayListOf(
                ChordMarker.Muted(StringNumber(6))
            ),
            barChordMarkers = arrayListOf(
                ChordMarker.Bar(FretNumber(3), Finger.INDEX, StringNumber(1), StringNumber(5))
            ),
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

    AppTheme {
        ChordPager(title = "Am", chordVariations = chords)
    }
}

//endregion

package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrynan.chords.compose.ChordWidget
import com.chrynan.chords.model.ChordChart
import com.chrynan.chords.model.ChordMarker
import com.chrynan.chords.model.ChordViewData
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringNumber
import com.chrynan.chords.util.maxFret
import com.chrynan.chords.util.minFret
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.chorddisplay.toChrynanRgba
import kotlin.math.max
import kotlin.math.min

/**
 * Displays a pinned list of all chords used in the tab with chord diagrams
 */
@Composable
fun PinnedChords(
    chords: List<ChordVariation>,
    instrument: Instrument,
    modifier: Modifier = Modifier,
    onChordClick: (String) -> Unit = {}
) {
    // Calculate top padding: status bar
    val topPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .shadow(elevation = 4.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    ) {
        Spacer(Modifier.height(topPadding))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chords.forEach { chord ->
                ChordDiagramChip(
                    chord = chord,
                    instrument = instrument,
                    onClick = { onChordClick(chord.chordId) }
                )
            }
        }
    }
}

/**
 * Individual chord diagram chip
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Composable
private fun ChordDiagramChip(
    chord: ChordVariation,
    instrument: Instrument,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .width(90.dp)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 3.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = chord.chordId,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        
        val chord = chord.toChrynanChord()
        val defaultMaxFret = 3
        val defaultMinFret = 1
        val endFret = max(chord.maxFret, defaultMaxFret)
        val startFret = min(chord.minFret, max(chord.maxFret - 2, defaultMinFret))

        val chartLayout = if (instrument == Instrument.Guitar) {
            ChordChart.STANDARD_TUNING_GUITAR_CHART.copy(
                fretStart = FretNumber(startFret),
                fretEnd = FretNumber(endFret)
            )
        } else {
            ChordChart.STANDARD_TUNING_UKELELE.copy(
                fretStart = FretNumber(startFret),
                fretEnd = FretNumber(endFret)
            )
        }

        ChordWidget(
            chord = chord,
            chart = chartLayout,
            modifier = Modifier
                .width(80.dp)
                .height(100.dp),
            viewData = ChordViewData(
                noteColor = MaterialTheme.colorScheme.primary.toChrynanRgba(),
                noteLabelTextColor = MaterialTheme.colorScheme.onPrimary.toChrynanRgba(),
                fretColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                fretLabelTextColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                stringColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                stringLabelTextColor = MaterialTheme.colorScheme.onBackground.toChrynanRgba(),
                mutedStringText = "x",
                openStringText = "",
                showFingerNumbers = false,
                fitToHeight = true
            )
        )
    }
}


@Preview
@Composable
private fun PinnedChordsPreview() {
    val chords = listOf(
        ChordVariation("Am_1", "Am", arrayListOf(
            ChordMarker.Note(fret = FretNumber(1), string = StringNumber(2)),
            ChordMarker.Note(fret = FretNumber(2), string = StringNumber(3)),
            ChordMarker.Note(fret = FretNumber(2), string = StringNumber(4))
        ), arrayListOf(
            ChordMarker.Open(string = StringNumber(1))
        ), arrayListOf(
            ChordMarker.Muted(string = StringNumber(6))
        ), arrayListOf(), Instrument.Guitar),
        ChordVariation("G_1", "G", arrayListOf(
            ChordMarker.Note(fret = FretNumber(3), string = StringNumber(1)),
            ChordMarker.Note(fret = FretNumber(2), string = StringNumber(5)),
            ChordMarker.Note(fret = FretNumber(3), string = StringNumber(6))
        ), arrayListOf(
            ChordMarker.Open(string = StringNumber(2)),
            ChordMarker.Open(string = StringNumber(3)),
            ChordMarker.Open(string = StringNumber(4))
        ), arrayListOf(), arrayListOf(), Instrument.Guitar),
        ChordVariation("C_1", "C", arrayListOf(
            ChordMarker.Note(fret = FretNumber(1), string = StringNumber(2)),
            ChordMarker.Note(fret = FretNumber(2), string = StringNumber(4)),
            ChordMarker.Note(fret = FretNumber(3), string = StringNumber(5))
        ), arrayListOf(
            ChordMarker.Open(string = StringNumber(1)),
            ChordMarker.Open(string = StringNumber(3))
        ), arrayListOf(
            ChordMarker.Muted(string = StringNumber(6))
        ), arrayListOf(), Instrument.Guitar),
        ChordVariation(
            "F_1",
            "F",
            arrayListOf(
                ChordMarker.Note(fret = FretNumber(2), string = StringNumber(3)),
                ChordMarker.Note(fret = FretNumber(3), string = StringNumber(4)),
                ChordMarker.Note(fret = FretNumber(3), string = StringNumber(5))
            ),
            arrayListOf(),
            arrayListOf(),
            arrayListOf(ChordMarker.Bar(fret = FretNumber(1), startString = StringNumber(1), endString = StringNumber(6)),),
            Instrument.Guitar
        )
    )

    AppTheme {
        PinnedChords(chords = chords, instrument = Instrument.Guitar)
    }
}

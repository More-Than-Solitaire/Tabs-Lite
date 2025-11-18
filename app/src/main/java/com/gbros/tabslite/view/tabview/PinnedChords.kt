package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.chrynan.chords.model.ChordViewData
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.model.StringLabelState
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
    chords: List<String>,
    chordVariations: Map<String, ChordVariation?>,
    instrument: Instrument,
    modifier: Modifier = Modifier,
    onChordClick: (String) -> Unit = {}
) {
    // Calculate top padding: status bar + app bar height (approximately 64dp for Material3)
    val topPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() + 64.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topPadding)
            .shadow(elevation = 4.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chords.forEach { chord ->
                ChordDiagramChip(
                    chordName = chord,
                    chordVariation = chordVariations[chord],
                    instrument = instrument,
                    onClick = { onChordClick(chord) }
                )
            }
        }
    }
}

/**
 * Individual chord diagram chip
 */
@Composable
private fun ChordDiagramChip(
    chordName: String,
    chordVariation: ChordVariation?,
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
            text = chordName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        
        if (chordVariation != null) {
            val chord = chordVariation.toChrynanChord()
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
                    // stringLabelState = StringLabelState.SHOW_LABEL,
                    mutedStringText = "x",
                    openStringText = "",
                    showFingerNumbers = false,
                    fitToHeight = true
                )
            )
        } else {
            // Placeholder when chord variation is not loaded
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
@Preview
private fun PinnedChordsPreview() {
    val sampleChords = listOf("C", "Em", "Am", "F", "G", "Dm", "A#m", "D")
    
    AppTheme {
        PinnedChords(
            chords = sampleChords,
            chordVariations = emptyMap(),
            instrument = Instrument.Guitar,
            onChordClick = { }
        )
    }
}

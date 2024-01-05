package com.gbros.tabslite.compose.chorddisplay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.gbros.tabslite.data.chord.ICompleteChord
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalUnsignedTypes::class)
@Composable
fun ChordPager(chords: ICompleteChord) {
    val chordItems by chords.observeAsState()
    if (!chordItems.isNullOrEmpty()) {
        val pagerState = rememberPagerState {
            chordItems!!.size
        }

        Column {
            HorizontalPager(
                pagerState,
                modifier = Modifier
                    .fillMaxWidth()
            ) { page ->
                Column {
                    Text(
                        text = chordItems!![page].chordId,
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
                            chord = chordItems!![page].toChrynanChord(),
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

            // current page indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) {  // draw current page indicator
                    val color =
                        if (pagerState.currentPage == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
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

    AppTheme {
        ChordPager(chords = CompleteChordForTest())
    }
}

internal class CompleteChordForTest: ICompleteChord {
    /**
     * Automatically add these chords to an empty constructor
     */
    constructor(): this(listOf(
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
    ))

    constructor(initialValues: List<ChordVariation>) : super(initialValues)
}

//endregion

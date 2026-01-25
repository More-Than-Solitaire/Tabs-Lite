package com.gbros.tabslite.view.tabview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.em
import com.gbros.tabslite.data.tab.TabContentBlock

/**
 * Displays a single TabContentBlock with chords positioned above the text
 */
@Composable
fun TabContentBlockView(
    modifier: Modifier = Modifier,
    block: TabContentBlock,
    fontSizeSp: Float,
    onChordClick: (chord: String) -> Unit
) {
    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val chordAnnotations = remember(block.content) {
        block.content.getStringAnnotations("chord", 0, block.content.length)
    }

    Layout(
        modifier = modifier,
        content = {
            Text(
                text = block.content,
                onTextLayout = {
                    textLayoutResult.value = it
                },
                style = TextStyle(
                    fontSize = TextUnit(fontSizeSp, TextUnitType.Sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = if (block.tab) 2.8.em else 1.5.em, // double spaced for tabs, normal for non-tab content
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Bottom, LineHeightStyle.Trim.None),
                    baselineShift = if (block.tab) BaselineShift(.15f) else BaselineShift(0f)
                )
            )

            for (annotation in chordAnnotations) {
                val chord = if (annotation.item.startsWith("{il}")) annotation.item.substring(4) else annotation.item
                ChordButton(
                    text = chord,
                    fontSizeSp = fontSizeSp,
                ) {
                    onChordClick(chord)
                }
            }
        }
    ) { measurables, constraints ->
        placeChordButtons(chordAnnotations, textLayoutResult, measurables, constraints)
    }
}

private fun MeasureScope.placeChordButtons(
    chordAnnotations: List<AnnotatedString.Range<String>>,
    textLayoutResult: MutableState<TextLayoutResult?>,
    measurables: List<Measurable>,
    constraints: Constraints
): MeasureResult {
    val basicTextPlaceable = measurables[0].measure(constraints)

    return layout(basicTextPlaceable.width, basicTextPlaceable.height) {
        basicTextPlaceable.placeRelative(0, 0)

        textLayoutResult.value?.let { layoutResult ->
            var lastButtonRightEdge = 0
            var lastButtonLine = -1

            for (i in chordAnnotations.indices) {
                val chordButton = measurables[i + 1].measure(Constraints()) // Unconstrained button size

                // the annotation tells us which chord goes above this place in the text
                val boundingBox = layoutResult.getBoundingBox(chordAnnotations[i].start)
                val currentButtonLine = layoutResult.getLineForOffset(chordAnnotations[i].start)

                // If we're on a new line, reset the tracking of the last button's edge
                if (currentButtonLine != lastButtonLine) {
                    lastButtonRightEdge = 0
                    lastButtonLine = currentButtonLine
                }

                val inline = chordAnnotations[i].item.startsWith("{il}")

                // Determine the horizontal position, ensuring it doesn't overlap the previous button
                val x = maxOf(boundingBox.left.toInt(), lastButtonRightEdge)
                val y = if (inline) boundingBox.bottom.toInt() - chordButton.height
                        else boundingBox.top.toInt() + (chordButton.height.toDouble() * 0.25).toInt()
                chordButton.placeRelative(x, y)

                // Update the right edge for the next button on this line
                lastButtonRightEdge = x + chordButton.width + 16 // 16 is some extra space between buttons
            }
        }
    }
}

package com.gbros.tabslite.view.tabview

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.tooling.data.position
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.em
import androidx.compose.ui.util.fastForEach
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun TabText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    fontSizeSp: Float,
    onZoom: (zoomFactor: Float) -> Unit,
    onChordClick: (chord: String) -> Unit
){
    val font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Only Android 8+ supports variable weight fonts
        FontFamily(Font(R.font.roboto_mono_variable_weight))
    } else {
        FontFamily(Font(R.font.roboto_mono_regular))
    }
    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val chordAnnotations = remember(text) { text.getStringAnnotations("chord", 0, text.length) }


    Layout(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointers = event.changes
                        if (pointers.size > 1) {
                            val zoom = pointers.calculateZoom()
                            if (zoom != 1f) {
                                onZoom(zoom)
                                // don't consume the pointer events so scrolling still works
                            }
                        }
                    }
                }
            },
        content = {
            Text(
                text = text,
                onTextLayout = {
                    textLayoutResult.value = it
                },
                style = TextStyle(
                    fontFamily = font,
                    fontSize = TextUnit(fontSizeSp, TextUnitType.Sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 2.8.em, // double spaced lines
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Bottom, LineHeightStyle.Trim.None),
                    baselineShift = BaselineShift(.15f)
                )
            )

            for (annotation in chordAnnotations) {
                ChordButton(
                    text = annotation.item,
                    fontSizeSp = fontSizeSp,
                ) {
                    onChordClick(annotation.item)
                }
            }
        }
    ) { measurables, constraints ->
        placeChordButtons(chordAnnotations, textLayoutResult, measurables, constraints)
    }
}

private fun List<PointerInputChange>.calculateZoom(): Float {
    if (size < 2) return 1f

    val first = this[0]
    val second = this[1]

    val prevDist = sqrt((second.previousPosition.x - first.previousPosition.x).pow(2) + (second.previousPosition.y - first.previousPosition.y).pow(2))
    val currDist = sqrt((second.position.x - first.position.x).pow(2) + (second.position.y - first.position.y).pow(2))

    return if (prevDist > 0f) currDist / prevDist else 1f
}

private fun MeasureScope.placeChordButtons(
    chordAnnotations:  List<AnnotatedString.Range<String>>,
    textLayoutResult: MutableState<TextLayoutResult?>,
    measurables: List<Measurable>,
    constraints: Constraints): MeasureResult {
    val basicTextPlaceable = measurables[0].measure(constraints)

    return layout(basicTextPlaceable.width, basicTextPlaceable.height) {
        basicTextPlaceable.placeRelative(0, 0)

        textLayoutResult.value?.let { layoutResult ->
            var lastButtonRightEdge = 0
            var lastButtonLine = -1

            for (i in chordAnnotations.indices) {
                val chordButton = measurables[i+1].measure(Constraints()) // Unconstrained button size

                // the annotation tells us which chord goes above this place in the text
                val boundingBox = layoutResult.getBoundingBox(chordAnnotations[i].start)
                val currentButtonLine = layoutResult.getLineForOffset(chordAnnotations[i].start)

                // If we're on a new line, reset the tracking of the last button's edge
                if (currentButtonLine != lastButtonLine) {
                    lastButtonRightEdge = 0
                    lastButtonLine = currentButtonLine
                }

                // Determine the horizontal position, ensuring it doesn't overlap the previous button
                val x = maxOf(boundingBox.left.toInt(), lastButtonRightEdge + 16)
                val y = boundingBox.top.toInt()

                // Position above the text
                chordButton.placeRelative(x, y)

                // Update the right edge for the next button on this line
                lastButtonRightEdge = x + chordButton.width
            }
        }
    }
}

@Composable @Preview
private fun TabTextTestCase1() {
    AppTheme {
        val builder = AnnotatedString.Builder("That ")
        builder.withAnnotation("chord", "C", block = {append("D")})
        builder.append("avid played and it ")
        builder.withAnnotation("chord", "Am", block = {append("p")})
        builder.append("leased the Lord")

        TabText(
            modifier = Modifier.background(color = Color.White),
            text = builder.toAnnotatedString(),
            fontSizeSp = 14f,
            onZoom = {},
            onChordClick = {}
        )
    }
}

@Composable @Preview
private fun TabTextTestCase2() {
    AppTheme {
        val builder = AnnotatedString.Builder("[Intro]\n")
        builder.withAnnotation("chord", "C", block = {append(" ")})
        builder.append(" ")
        builder.withAnnotation("chord", "Am", block = {append(" ")})
        builder.append(" ")
        builder.withAnnotation("chord", "C", block = {append(" ")})
        builder.append(" ")
        builder.withAnnotation("chord", "Am", block = {append(" ")})

        TabText(
            modifier = Modifier.background(color = Color.White),
            text = builder.toAnnotatedString(),
            fontSizeSp = 14f,
            onZoom = {},
            onChordClick = {}
        )
    }
}

@Composable @Preview
private fun TabTextPreview() {
    val hallelujahTabForTest = AnnotatedString("""
        [Intro]
        {ch:C} {ch:Em} {ch:C} {ch:Em}
         
        [Verse]
        {ch:C}  Hey there Delilah, What’s it {ch:Em}like in New York City?
        I’m a {ch:C}thousand miles away, But girl {ch:Em}tonight you look so pretty, Yes you {ch:Am}do, {ch:G}
        
        {ch:F}  Time Square can’t {ch:G}shine as bright as {ch:Am}you, 
        I swear it’s {ch:G}true. 
        {ch:C}  Hey there Delilah, 
        Don’t you {ch:Em}worry about the distance, 
        I’m right {ch:C}there if you get lonely, 
        {ch:G}ive this {ch:Em}song another listen, 
        Close your {ch:Am}eyes, {ch:G}
        {ch:F}  Listen to my {ch:G}voice it’s my {ch:Am}disguise, 
        I’m by your {ch:G}side.    """.trimIndent())

    AppTheme {
        TabText(
            modifier = Modifier.background(color = Color.White),
            text = hallelujahTabForTest,
            fontSizeSp = 14f,
            onZoom = {},
            onChordClick = {}
        )
    }
}

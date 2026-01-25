package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun TabText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    fontSizeSp: Float,
    onChordClick: (chord: String) -> Unit
){
    val textLayoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val chordAnnotations = remember(text) { text.getStringAnnotations("chord", 0, text.length) }


    Layout(
        modifier = modifier,
        content = {
            Text(
                text = text,
                onTextLayout = {
                    textLayoutResult.value = it
                },
                style = TextStyle(
                    fontSize = TextUnit(fontSizeSp, TextUnitType.Sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 2.8.em, // double spaced lines
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Bottom, LineHeightStyle.Trim.None),
                    baselineShift = BaselineShift(.15f)
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

                val inline = chordAnnotations[i].item.startsWith("{il}")

                // Determine the horizontal position, ensuring it doesn't overlap the previous button
                boundingBox.center
                val x = maxOf(boundingBox.left.toInt(), lastButtonRightEdge)
                val y = if (inline) boundingBox.bottom.toInt() - chordButton.height else boundingBox.top.toInt() + (chordButton.height.toDouble() * 0.25).toInt()
                chordButton.placeRelative(x, y)

                // Update the right edge for the next button on this line
                lastButtonRightEdge = x + chordButton.width + 16 // 16 is some extra space between buttons
            }
        }
    }
}

@Composable @Preview(showBackground = true)
private fun TabTextTestCase1() {
    AppTheme {
        val builder = AnnotatedString.Builder("[Intro]\n")
        builder.withAnnotation("chord", "{il}C", block = {append(" ")})
        builder.append(" ")
        builder.withAnnotation("chord", "{il}Am", block = {append(" ")})
        builder.withAnnotation("chord", "{il}C", block = {append(" ")})
        builder.withAnnotation("chord", "{il}Am", block = {append(" ")})
        builder.append("\n\n[Verse 1]\nI ")
        builder.withAnnotation("chord", "C", block = {append("h")})
        builder.append("eard there was a ")
        builder.withAnnotation("chord", "Am", block = {append("s")})
        builder.append("ecret chord \nThat ")
        builder.withAnnotation("chord", "C", block = {append("D")})
        builder.append("avid played and it ")
        builder.withAnnotation("chord", "Am", block = {append("p")})
        builder.append("leased the Lord")

        TabText(
            modifier = Modifier.padding(16.dp),
            text = builder.toAnnotatedString(),
            fontSizeSp = 14f,
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
            onChordClick = {}
        )
    }
}

package com.gbros.tabslite.view.tabview

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.em
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

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
        modifier = modifier,
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
        val basicTextPlaceable = measurables[0].measure(constraints)


        layout(basicTextPlaceable.width, basicTextPlaceable.height) {
            basicTextPlaceable.placeRelative(0, 0)

            textLayoutResult.value?.let { layoutResult ->
                for (i in chordAnnotations.indices) {
                    val chordButton = measurables[i+1].measure(Constraints()) // Unconstrained button size

                    // the annotation tells us which chord goes above this place in the text
                    val boundingBox = layoutResult.getBoundingBox(chordAnnotations[i].start)

                    // Position above the text
                    chordButton.placeRelative(boundingBox.left.toInt(), boundingBox.top.toInt())
                }
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
        builder.withAnnotation("chord", "Am", block = {append(" ")})
        builder.withAnnotation("chord", "C", block = {append(" ")})
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

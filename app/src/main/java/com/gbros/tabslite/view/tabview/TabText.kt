package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.data.tab.TabContentBlock
import com.gbros.tabslite.ui.theme.AppTheme

/**
 * Displays a list of TabContentBlock objects as a complete tab
 */
@Composable
fun TabText(
    modifier: Modifier = Modifier,
    blocks: List<TabContentBlock>,
    fontSizeSp: Float,
    onChordClick: (chord: String) -> Unit
){
    Column(modifier = modifier) {
        blocks.forEach { block ->
            TabContentBlockView(
                block = block,
                fontSizeSp = fontSizeSp,
                onChordClick = onChordClick
            )
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

        val blocks = listOf(
            TabContentBlock(builder.toAnnotatedString(), tab = true)
        )

        TabText(
            modifier = Modifier.padding(16.dp),
            blocks = blocks,
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
        {ch:C}  Hey there Delilah, What's it {ch:Em}like in New York City?
        I'm a {ch:C}thousand miles away, But girl {ch:Em}tonight you look so pretty, Yes you {ch:Am}do, {ch:G}
        
        {ch:F}  Time Square can't {ch:G}shine as bright as {ch:Am}you, 
        I swear it's {ch:G}true. 
        {ch:C}  Hey there Delilah, 
        Don't you {ch:Em}worry about the distance, 
        I'm right {ch:C}there if you get lonely, 
        {ch:G}ive this {ch:Em}song another listen, 
        Close your {ch:Am}eyes, {ch:G}
        {ch:F}  Listen to my {ch:G}voice it's my {ch:Am}disguise, 
        I'm by your {ch:G}side.    """.trimIndent())

    val blocks = listOf(
        TabContentBlock(hallelujahTabForTest, tab = true)
    )

    AppTheme {
        TabText(
            modifier = Modifier.background(color = Color.White),
            blocks = blocks,
            fontSizeSp = 14f,
            onChordClick = {}
        )
    }
}

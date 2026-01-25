package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun ChordButton(
    modifier: Modifier = Modifier,
    text: String,
    fontSizeSp: Float,
    fontFamily: FontFamily? = null,
    onClick: () -> Unit
){
    BasicText(
        text = text,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 0.dp),
        style = TextStyle(
            color = MaterialTheme.colorScheme.primary,
            fontSize = TextUnit(fontSizeSp, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily
        )
    )
}

@Preview
@Composable
private fun ChordButtonPreview() {
    AppTheme {
        ChordButton(text = "Am", fontSizeSp = 14f, fontFamily = null, onClick = {})
    }
}

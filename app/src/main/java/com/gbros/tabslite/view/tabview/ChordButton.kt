package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
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
    onClick: () -> Unit
){
    BasicText(
        text = text,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 0.dp),
        style = TextStyle(
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = TextUnit(fontSizeSp, TextUnitType.Sp)
        )
    )
}

@Preview
@Composable
private fun ChordButtonPreview() {
    AppTheme {
        ChordButton(text = "Am", fontSizeSp = 14f, onClick = {})
    }
}

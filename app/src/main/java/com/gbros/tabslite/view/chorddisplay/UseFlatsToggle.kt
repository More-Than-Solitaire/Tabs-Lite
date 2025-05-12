package com.gbros.tabslite.view.chorddisplay

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun UseFlatsToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    OutlinedIconToggleButton (checked = checked, onCheckedChange = onCheckedChange) {
        Text(text = "b", fontStyle = FontStyle.Italic)
    }
}


@Preview
@Composable
private fun UseFlatsTogglePreview() {
    AppTheme {
        Column {
            UseFlatsToggle(true, {})
            UseFlatsToggle(false, {})
        }
    }
}
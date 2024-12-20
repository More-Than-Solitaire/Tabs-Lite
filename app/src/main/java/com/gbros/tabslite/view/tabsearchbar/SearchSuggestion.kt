package com.gbros.tabslite.view.tabsearchbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun SearchSuggestion(modifier: Modifier = Modifier, suggestionText: String, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Text(
            text = suggestionText,
            modifier = Modifier.padding(all = 4.dp)
        )
    }
}

@Composable @Preview
private fun SearchSuggestionPreview() {
    AppTheme {
        SearchSuggestion(suggestionText = "This is an example suggested search (clickable)", onClick = {})
    }
}
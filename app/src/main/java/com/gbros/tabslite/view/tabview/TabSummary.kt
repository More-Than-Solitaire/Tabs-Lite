package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun TabSummary(difficulty: String, tuning: String, capo: String, key: String, author: String) {
    Row(
        modifier = Modifier.windowInsetsPadding(WindowInsets(
            left = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(
                LayoutDirection.Ltr),
            right = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(
                LayoutDirection.Ltr)
        ))
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.tab_difficulty, difficulty),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.tab_tuning, tuning),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.tab_capo, capo),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.tab_key, key),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.tab_author, author),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable @Preview
private fun TabSummaryPreview() {
    AppTheme {
        TabSummary(difficulty = "expert", tuning = "E A D G B E", capo = "2nd Fret", key = "C", author = "Joe Blow")
    }
}
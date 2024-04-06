package com.gbros.tabslite.compose.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun InfoCard(text: String) {
    GenericInformationCard(
        text = text,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        icon = Icons.Default.Info,
        iconContentDescription = "Info"
    )
}

@Composable @Preview
private fun InfoCardPreview() {
    AppTheme {
        InfoCard(text = "Add songs to your playlist by finding the song you'd like and selecting the three dot menu at the top right of the screen.")
    }
}
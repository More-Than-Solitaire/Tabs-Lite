package com.gbros.tabslite.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun InfoCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(all = 8.dp)

        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Info", modifier = Modifier.padding(all = 8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(all = 4.dp)
            )
        }
    }
}

@Composable @Preview
private fun InfoCardPreview() {
    AppTheme {
        InfoCard(text = "Add songs to your playlist by finding the song you'd like and selecting the three dot menu at the top right of the screen.")
    }
}
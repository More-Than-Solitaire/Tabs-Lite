package com.gbros.tabslite.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
fun ErrorCard(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(all = 2.dp)) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                modifier = Modifier
                    .padding(all = 8.dp)
            )
            Text(text)
        }
    }
}

@Composable @Preview
private fun ErrorCardPreview() {
    AppTheme {
        ErrorCard(text = "Error!  Something bad happened and now we need to show this message.")
    }
}
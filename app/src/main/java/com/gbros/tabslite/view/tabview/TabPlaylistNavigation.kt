package com.gbros.tabslite.view.tabview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun TabPlaylistNavigation(title: String, nextSongButtonEnabled: Boolean, previousSongButtonEnabled: Boolean, onNextSongClick: () -> Unit, onPreviousSongClick: () -> Unit) {
    Row {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(all = 6.dp)
                        .weight(1f)
                )
                IconButton(enabled = previousSongButtonEnabled, onClick = onPreviousSongClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_skip_back) ,
                        contentDescription = "Previous"
                    )
                }
                IconButton(enabled = nextSongButtonEnabled, onClick = onNextSongClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_skip_forward),
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable @Preview
private fun TabPlaylistNavigationPreview() {
    AppTheme {
        TabPlaylistNavigation("My Playlist", true, false, {}, {})
    }
}
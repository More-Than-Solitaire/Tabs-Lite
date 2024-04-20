package com.gbros.tabslite.compose.homescreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun AboutDialog(onCloseClicked: () -> Unit, onExportPlaylistsClicked: () -> Unit, onImportPlaylistsClicked: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Left
        ) {
            IconButton(onClick = onCloseClicked ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.generic_action_close))
            }
        }

        Card (
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.large.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))
        ) {
            Text(modifier = Modifier.padding(all = 16.dp), text = stringResource(id = R.string.app_about))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Card (
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.large.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onExportPlaylistsClicked() }
            ) {
                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_download), contentDescription = "")
                Text(modifier = Modifier.padding(all = 16.dp), text = stringResource(id = R.string.app_action_export_playlists))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onImportPlaylistsClicked() }
            ) {
                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload), contentDescription = "")
                Text(modifier = Modifier.padding(all = 16.dp), text = stringResource(id = R.string.app_action_import_playlists))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly) {
            val uriHandler = LocalUriHandler.current
            TextButton(onClick = { uriHandler.openUri("https://play.google.com/store/apps/details?id=com.gbros.tabslite") }) {
                Text(text = stringResource(id = R.string.app_action_leave_review))
            }
            TextButton(onClick = { uriHandler.openUri("https://github.com/sponsors/More-Than-Solitaire") }) {
                Text(text = stringResource(id = R.string.app_action_donate))
            }
        }
    }
}

@Composable @Preview
private fun AboutDialogPreview() {
    AppTheme {
        AboutDialog({}, {}) {}
    }
}
package com.gbros.tabslite.view.homescreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gbros.tabslite.R
import com.gbros.tabslite.data.FontStyle
import com.gbros.tabslite.data.ThemeSelection
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(
    modifier: Modifier = Modifier,
    selectedTheme: ThemeSelection,
    selectedFontStyle: FontStyle,
    onDismissRequest: () -> Unit,
    onExportPlaylistsClicked: () -> Unit,
    onImportPlaylistsClicked: () -> Unit,
    onNavigateToCreateTab: () -> Unit,
    onSwitchThemeMode: (ThemeSelection) -> Unit,
    onSwitchFontStyle: (FontStyle) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(modifier = Modifier.padding(all = 4.dp), onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.generic_action_close))
                    }
                }
                Row(
                    modifier = Modifier
                        .matchParentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.titleLarge)
                }
            }

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = MaterialTheme.shapes.extraSmall.bottomStart, bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd)
            ) {
                Text(modifier = Modifier.padding(all = 16.dp), text = stringResource(id = R.string.app_about))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Column {
                    // theme selection
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(modifier = Modifier.padding(all = 8.dp), text = stringResource(R.string.theme_selection_title))
                        Spacer(modifier = Modifier.weight(1f))
                        // versions dropdown to switch versions of this song
                        var themeDropdownExpanded by remember { mutableStateOf(false) }
                        val currentDarkModePreference = when (selectedTheme) {
                            ThemeSelection.ForceDark -> {
                                stringResource(id = R.string.theme_selection_dark)
                            }

                            ThemeSelection.ForceLight -> {
                                stringResource(id = R.string.theme_selection_light)
                            }

                            else -> {
                                stringResource(id = R.string.theme_selection_system)
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = themeDropdownExpanded,
                            onExpandedChange = { themeDropdownExpanded = !themeDropdownExpanded },
                            modifier = Modifier
                                .width(200.dp)
                                .padding(start = 8.dp)
                        ) {
                            TextField(
                                value = currentDarkModePreference,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = themeDropdownExpanded,
                                onDismissRequest = { themeDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.theme_selection_system)) },
                                    onClick = {
                                        onSwitchThemeMode(ThemeSelection.System)
                                        themeDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.theme_selection_light)) },
                                    onClick = {
                                        onSwitchThemeMode(ThemeSelection.ForceLight)
                                        themeDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.theme_selection_dark)) },
                                    onClick = {
                                        onSwitchThemeMode(ThemeSelection.ForceDark)
                                        themeDropdownExpanded = false
                                    }
                                )
                            }
                        }

                    }

                    // font style
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(modifier = Modifier.padding(all = 8.dp), text = stringResource(R.string.font_style_selection_title))
                        Spacer(modifier = Modifier.weight(1f))
                        // versions dropdown to switch versions of this song
                        var fontStyleDropdownExpanded by remember { mutableStateOf(false) }
                        val currentFontStylePreference = when (selectedFontStyle) {
                            FontStyle.Modern -> {
                                stringResource(id = R.string.font_style_selection_modern)
                            }

                            FontStyle.Mono -> {
                                stringResource(id = R.string.font_style_selection_mono)
                            }

                            else -> ""
                        }
                        ExposedDropdownMenuBox(
                            expanded = fontStyleDropdownExpanded,
                            onExpandedChange = { fontStyleDropdownExpanded = !fontStyleDropdownExpanded },
                            modifier = Modifier
                                .width(200.dp)
                                .padding(start = 8.dp)
                        ) {
                            TextField(
                                value = currentFontStylePreference,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontStyleDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = fontStyleDropdownExpanded,
                                onDismissRequest = { fontStyleDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.font_style_selection_modern)) },
                                    onClick = {
                                        onSwitchFontStyle(FontStyle.Modern)
                                        fontStyleDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.font_style_selection_mono)) },
                                    onClick = {
                                        onSwitchFontStyle(FontStyle.Mono)
                                        fontStyleDropdownExpanded = false
                                    }
                                )
                            }
                        }

                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.extraLarge.copy(topStart = MaterialTheme.shapes.extraSmall.topStart, topEnd = MaterialTheme.shapes.extraSmall.topEnd)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                        .clickable { onImportPlaylistsClicked() }
                ) {
                    Icon(modifier = Modifier.padding(all = 8.dp), imageVector = ImageVector.vectorResource(id = R.drawable.ic_download), contentDescription = "")
                    Text(modifier = Modifier.padding(all = 8.dp), text = stringResource(id = R.string.app_action_import_playlists))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                        .clickable { onExportPlaylistsClicked() }
                ) {
                    Icon(modifier = Modifier.padding(all = 8.dp), imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload), contentDescription = "")
                    Text(modifier = Modifier.padding(all = 8.dp), text = stringResource(id = R.string.app_action_export_playlists))
                }
                TextButton(onClick = onNavigateToCreateTab) {
                    Text(text = "Create New Tab")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
}

@Composable @Preview
private fun AboutDialogPreview() {
    AppTheme {
        AboutDialog(Modifier, ThemeSelection.System, FontStyle.Modern, {}, {}, {}, {}, {}, {})
    }
}

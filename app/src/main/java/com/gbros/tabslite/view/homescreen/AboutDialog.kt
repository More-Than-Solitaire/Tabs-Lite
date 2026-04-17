package com.gbros.tabslite.view.homescreen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = !isLandscape)
    ) {
        BoxWithConstraints {
            val dialogMaxHeight = maxHeight * 0.98f

            Card(
                modifier = modifier
                    .heightIn(max = dialogMaxHeight)
                    .then(if (isLandscape) Modifier.fillMaxWidth(0.95f) else Modifier),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.heightIn(max = dialogMaxHeight)) {
                    if (isLandscape) {
                        // Landscape: Custom header with title left, buttons right
                        LandscapeAboutDialogHeader(onDismissRequest)
                    } else {
                        // Portrait: Standard centered header
                        AboutDialogHeader(onDismissRequest)
                    }

                    if (isLandscape) {
                        // Landscape: Two-column layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            // Left column: About text (40%) - rounded corners on left side
                            Card(
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                    .weight(0.4f)
                                    .fillMaxHeight(),
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                                shape = MaterialTheme.shapes.extraLarge.copy(
                                    topEnd = MaterialTheme.shapes.extraSmall.topEnd,
                                    bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd
                                )
                            ) {
                                AboutTextCard()
                            }

                            // Right column: Settings and actions (60%) - rounded corners on right side
                            Card(
                                modifier = Modifier
                                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                    .weight(0.6f)
                                    .fillMaxHeight(),
                                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                                shape = MaterialTheme.shapes.extraLarge.copy(
                                    topStart = MaterialTheme.shapes.extraSmall.topStart,
                                    bottomStart = MaterialTheme.shapes.extraSmall.bottomStart
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    // Top: Import/Export with Create New Tab
                                    ImportExportActionsCompact(
                                        onImportPlaylistsClicked = onImportPlaylistsClicked,
                                        onExportPlaylistsClicked = onExportPlaylistsClicked,
                                        onNavigateToCreateTab = onNavigateToCreateTab
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Bottom: Theme/Font Settings
                                    ThemeFontSettingsCard(
                                        selectedTheme = selectedTheme,
                                        selectedFontStyle = selectedFontStyle,
                                        onSwitchThemeMode = onSwitchThemeMode,
                                        onSwitchFontStyle = onSwitchFontStyle
                                    )
                                }
                            }
                        }
                    } else {
                        // Portrait: Original vertical layout
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                            shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = MaterialTheme.shapes.extraSmall.bottomStart, bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd)
                        ) {
                            AboutTextCard()
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        ThemeFontSettingsCard(
                            selectedTheme = selectedTheme,
                            selectedFontStyle = selectedFontStyle,
                            onSwitchThemeMode = onSwitchThemeMode,
                            onSwitchFontStyle = onSwitchFontStyle
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ImportExportActionsCard(
                            onImportPlaylistsClicked = onImportPlaylistsClicked,
                            onExportPlaylistsClicked = onExportPlaylistsClicked,
                            onNavigateToCreateTab = onNavigateToCreateTab
                        )
                        ReviewDonateButtons()
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutDialogHeader(onDismissRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(modifier = Modifier.padding(all = 4.dp), onClick = onDismissRequest) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.generic_action_close))
            }
        }
        Row(
            modifier = Modifier.matchParentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun LandscapeAboutDialogHeader(onDismissRequest: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button
        IconButton(modifier = Modifier.padding(all = 4.dp), onClick = onDismissRequest) {
            Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.generic_action_close))
        }

        // Title - left aligned
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Review and Donate buttons - right aligned
        TextButton(onClick = { uriHandler.openUri("https://play.google.com/store/apps/details?id=com.gbros.tabslite") }) {
            Text(text = stringResource(id = R.string.app_action_leave_review))
        }
        TextButton(onClick = { uriHandler.openUri("https://github.com/sponsors/More-Than-Solitaire") }) {
            Text(text = stringResource(id = R.string.app_action_donate))
        }
    }
}

@Composable
private fun AboutTextCard() {
    BoxWithConstraints {
        val scrollState = rememberScrollState()
        val contentMaxHeight = maxHeight
        val scrollbarTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        val scrollbarThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        val textHeightModifier = Modifier
            .padding(all = 16.dp)
            .verticalScroll(scrollState)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = textHeightModifier.weight(1f),
                text = stringResource(id = R.string.app_about)
            )
            if (scrollState.maxValue > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, end = 8.dp)
                        .width(4.dp)
                        .height(contentMaxHeight)
                        .drawBehind {
                            drawRect(color = scrollbarTrackColor)
                            val thumbHeight = size.height * (size.height / (size.height + scrollState.maxValue))
                            val thumbTop = (size.height - thumbHeight) * (scrollState.value.toFloat() / scrollState.maxValue.toFloat())
                            drawRect(
                                color = scrollbarThumbColor,
                                topLeft = Offset(0f, thumbTop),
                                size = androidx.compose.ui.geometry.Size(size.width, thumbHeight)
                            )
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeFontSettingsCard(
    selectedTheme: ThemeSelection,
    selectedFontStyle: FontStyle,
    onSwitchThemeMode: (ThemeSelection) -> Unit,
    onSwitchFontStyle: (FontStyle) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun ImportExportActionsCard(
    onImportPlaylistsClicked: () -> Unit,
    onExportPlaylistsClicked: () -> Unit,
    onNavigateToCreateTab: () -> Unit,
    compact: Boolean = false
) {
    val rowPadding = if (compact) 4.dp else 8.dp
    val iconPadding = if (compact) 4.dp else 8.dp
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.extraLarge.copy(topStart = MaterialTheme.shapes.extraSmall.topStart, topEnd = MaterialTheme.shapes.extraSmall.topEnd)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(all = rowPadding)
                    .fillMaxWidth()
                    .clickable { onImportPlaylistsClicked() }
            ) {
                Icon(modifier = Modifier.padding(all = iconPadding), imageVector = ImageVector.vectorResource(id = R.drawable.ic_download), contentDescription = "")
                Text(
                    modifier = Modifier.padding(all = iconPadding),
                    text = stringResource(id = R.string.app_action_import_playlists),
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(all = rowPadding)
                    .fillMaxWidth()
                    .clickable { onExportPlaylistsClicked() }
            ) {
                Icon(modifier = Modifier.padding(all = iconPadding), imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload), contentDescription = "")
                Text(
                    modifier = Modifier.padding(all = iconPadding),
                    text = stringResource(id = R.string.app_action_export_playlists),
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = onNavigateToCreateTab) {
                Text(
                    text = "Create New Tab",
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ImportExportActionsCompact(
    onImportPlaylistsClicked: () -> Unit,
    onExportPlaylistsClicked: () -> Unit,
    onNavigateToCreateTab: () -> Unit
) {
    val rowPadding = 4.dp
    val iconPadding = 4.dp
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(all = rowPadding)
                .fillMaxWidth()
                .clickable { onImportPlaylistsClicked() }
        ) {
            Icon(modifier = Modifier.padding(all = iconPadding), imageVector = ImageVector.vectorResource(id = R.drawable.ic_download), contentDescription = "")
            Text(
                modifier = Modifier.padding(all = iconPadding),
                text = stringResource(id = R.string.app_action_import_playlists),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(all = rowPadding)
                .fillMaxWidth()
                .clickable { onExportPlaylistsClicked() }
        ) {
            Icon(modifier = Modifier.padding(all = iconPadding), imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload), contentDescription = "")
            Text(
                modifier = Modifier.padding(all = iconPadding),
                text = stringResource(id = R.string.app_action_export_playlists),
                style = MaterialTheme.typography.bodySmall
            )
        }
        TextButton(onClick = onNavigateToCreateTab) {
            Text(
                text = "Create New Tab",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ReviewDonateButtons() {
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


@Composable @Preview
private fun AboutDialogPreview() {
    AppTheme {
        AboutDialog(Modifier, ThemeSelection.System, FontStyle.Modern, {}, {}, {}, {}, {}, {})
    }
}

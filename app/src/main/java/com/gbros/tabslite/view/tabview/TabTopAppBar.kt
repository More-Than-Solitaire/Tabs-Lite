package com.gbros.tabslite.view.tabview

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.addtoplaylistdialog.AddToPlaylistDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTopAppBar(isFavorite: Boolean,
                 title: String,
                 shareUrl: String,
                 copyText: String,
                 allPlaylists: List<Playlist>,
                 selectedPlaylistTitle: String?,
                 selectPlaylistConfirmButtonEnabled: Boolean,
                 onNavigateBack: () -> Unit,
                 onReloadClick: () -> Unit,
                 onAddToPlaylist: () -> Unit,
                 onCreatePlaylist: (title: String, description: String) -> Unit,
                 onPlaylistSelectionChange: (Playlist) -> Unit,
                 onFavoriteButtonClick: () -> Unit,
                 onExportToPdfClick: (Context) -> Unit
) {
    val currentContext = LocalContext.current

    // remember whether three-dot menu is shown currently
    var showMenu by remember { mutableStateOf(false) }

    // remember whether the Add To Playlist dialog is shown currently
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val topAppBarState = rememberTopAppBarState()
    TopAppBar(
        title = {
            Text(
                text = if (topAppBarState.overlappedFraction > 0) title else "",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState,),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.generic_action_back))
            }
        },
        actions = {
            IconButton(onClick = onFavoriteButtonClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.tab_favorite_button_accessibility_text),
                    tint = Color(4294925653)
                )
            }

            IconButton(onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareUrl)
                    putExtra(Intent.EXTRA_TITLE, title)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                currentContext.startActivity(shareIntent)
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.generic_action_share),
                )
            }

            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.generic_action_more))
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.title_add_to_playlist_dialog),
                            )
                            Text(text = stringResource(R.string.title_add_to_playlist_dialog), modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    },
                    onClick = {
                        showMenu = false
                        showAddToPlaylistDialog = true
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.generic_action_reload),
                            )
                            Text(text = stringResource(R.string.generic_action_reload), modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    },
                    onClick = {
                        showMenu = false
                        onReloadClick()
                    }
                )
                val clipboardManager = LocalClipboardManager.current
                DropdownMenuItem(
                    text = {
                        Row {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_content_copy),
                                contentDescription = stringResource(R.string.generic_action_copy),
                            )
                            Text(text = stringResource(R.string.generic_action_copy), modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    },
                    onClick = {
                        clipboardManager.nativeClipboard.setPrimaryClip(ClipData.newPlainText(title, copyText))
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_picture_as_pdf),
                                contentDescription = stringResource(R.string.generic_action_export_to_pdf),
                            )
                            Text(text = stringResource(R.string.generic_action_export_to_pdf), modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    },
                    onClick = {
                        showMenu = false
                        onExportToPdfClick(currentContext)
                    }
                )
            }
        }
    )

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = allPlaylists,
            selectedPlaylistDropdownText = selectedPlaylistTitle,
            onSelectionChange = onPlaylistSelectionChange,
            confirmButtonEnabled = selectPlaylistConfirmButtonEnabled,
            onConfirm = {
                showAddToPlaylistDialog = false
                onAddToPlaylist()
            },
            onCreatePlaylist = onCreatePlaylist,
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }
}

@Composable @Preview
private fun TabTopAppBarPreview() {
    val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
    val list = listOf(playlistForTest, playlistForTest, playlistForTest ,playlistForTest, playlistForTest)
    AppTheme {
        TabTopAppBar(
            isFavorite = true,
            shareUrl = "https://tabslite.com/tab/1234",
            allPlaylists = list,
            selectedPlaylistTitle = "Test",
            copyText = "",
            selectPlaylistConfirmButtonEnabled = false,
            onAddToPlaylist = {},
            onCreatePlaylist = {_, _->},
            onFavoriteButtonClick = {},
            onPlaylistSelectionChange = {},
            onNavigateBack = {},
            onReloadClick = {},
            onExportToPdfClick = {},
            title = ""
        )
    }
}

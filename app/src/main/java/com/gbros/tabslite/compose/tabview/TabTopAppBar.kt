package com.gbros.tabslite.compose.tabview

import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gbros.tabslite.R
import com.gbros.tabslite.compose.addtoplaylistdialog.AddToPlaylistDialog
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabTopAppBar(tab: ITab, navigateBack: () -> Unit) {
    val currentContext = LocalContext.current
    val db: AppDatabase = remember { AppDatabase.getInstance(currentContext) }
    val isFavorite by db.playlistEntryDao().tabExistsInFavorites(tab.tabId).observeAsState(initial = false)
    var newFavoriteValue: Boolean? by remember { mutableStateOf(null) }

    // remember whether three-dot menu is shown currently
    var showMenu by remember { mutableStateOf(false) }

    // remember whether the Add To Playlist dialog is shown currently
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.tab_title, tab.songName, tab.artistName),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = {
                newFavoriteValue = !isFavorite
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = Color(4294925653)
                )
            }

            IconButton(onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, tab.getUrl())
                    putExtra(Intent.EXTRA_TITLE, String.format(format = ContextCompat.getString(currentContext, R.string.tab_title), tab.songName, tab.artistName))
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                currentContext.startActivity(shareIntent)
            }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                )
            }

            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Default.MoreVert, "More")
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
                                contentDescription = "Add to playlist",
                            )
                            Text(text = "Add to playlist", modifier = Modifier.padding(top = 2.dp, start = 4.dp))
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
                                contentDescription = "Reload",
                            )
                            Text(text = "Reload", modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                        }
                    },
                    onClick = {
                        showMenu = false
                        // todo: reload tab from internet
                    }
                )
            }
        },
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    )

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            tabId = tab.tabId,
            transpose = tab.transpose,
            onConfirm = { showAddToPlaylistDialog = false},
            onDismiss = { showAddToPlaylistDialog = false }
        )
    }

    LaunchedEffect(key1 = newFavoriteValue) {
        val copyOfNewFavoriteValue = newFavoriteValue
        if (copyOfNewFavoriteValue != null) {
            if (copyOfNewFavoriteValue) {
                db.playlistEntryDao().insertToFavorites(tab.tabId, tab.transpose)
            } else {
                db.playlistEntryDao().deleteTabFromFavorites(tab.tabId)
            }

        }
    }
}

@Composable @Preview
private fun TabTopAppBarPreview() {
    val tabForTest = TabWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "C", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow", content = "hallelujahTabForTest")
    AppTheme {
        TabTopAppBar(tab = tabForTest, navigateBack = {})
    }
}

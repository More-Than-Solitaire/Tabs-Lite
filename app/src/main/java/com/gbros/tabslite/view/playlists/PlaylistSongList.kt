package com.gbros.tabslite.view.playlists

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.view.card.InfoCard
import com.gbros.tabslite.view.songlist.SongListItem
import com.gbros.tabslite.view.swipetodismiss.MaterialSwipeToDismiss
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Represents a playlist of songs.  Handles reordering of songs.
 */
@Composable
fun PlaylistSongList(
    songs: List<TabWithDataPlaylistEntry>,
    navigateToTabByPlaylistEntryId: (entryId: Int) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemove: (tabToRemove: TabWithDataPlaylistEntry) -> Unit
) {
    // Use remember to create a MutableState object with a mutable collection type
    var reorderedSongsForDisplay by remember { mutableStateOf(songs) }

    // Observe changes in songs and update current songs accordingly
    DisposableEffect(songs) {
        // normally this effect will run when the list is reordered, in which case reorderedSongsForDisplay
        // should already match the incoming list. Avoiding reassigning reorderedSongsForDisplay prevents
        // the need for a redraw with a new list, allowing reorder animations to complete.
        if (!equals(songs, reorderedSongsForDisplay)) {
            Log.d(TAG, "Reassigning reorderedSongsForDisplay due to list inequality")
            reorderedSongsForDisplay = songs.toMutableList()
        }
        onDispose { }  // only run this effect once per update to songs
    }

    var reorderFrom: Int? by remember { mutableStateOf(null) }
    var reorderTo: Int? by remember { mutableStateOf(null) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        reorderedSongsForDisplay = reorderedSongsForDisplay.toMutableList().apply {
            add(to.index, removeAt(from.index))

            // save the initial from value for updating the database after the reorder is finished
            if (reorderFrom == null) {
                reorderFrom = from.index
            }
            reorderTo = to.index  // save the most recent to value for updating the database after the reorder is finished
        }
    }

    if (songs.isEmpty()) {
        // empty playlist
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp)
        ) {
            InfoCard(text = stringResource(id = R.string.playlist_empty_description))
        }
    }
    else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(reorderedSongsForDisplay, key = { it }) {
                ReorderableItem(reorderableLazyListState, key = it) { isDragging ->
                    val interactionSource = remember { MutableInteractionSource() }
                    MaterialSwipeToDismiss(
                        onRemove = { onRemove(it) },
                        enable = !isDragging,
                        content = {
                            Card(
                                onClick = { navigateToTabByPlaylistEntryId(it.entryId) },
                                interactionSource = interactionSource
                            ) {
                                Row {
                                    IconButton(
                                        modifier = Modifier.draggableHandle(
                                            onDragStopped = {
                                                if (reorderFrom != null && reorderTo != null) {
                                                    Log.d(TAG, "reordering $reorderFrom to $reorderTo")
                                                    onReorder(reorderFrom!!, reorderTo!!)
                                                }
                                                // reset saved reorder for next move
                                                reorderFrom = null
                                                reorderTo = null
                                            },
                                            interactionSource = interactionSource
                                        ),
                                        onClick = {},
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_drag_handle),
                                            contentDescription = stringResource(R.string.generic_action_drag_to_reorder)
                                        )
                                    }
                                    SongListItem(song = it)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Check equality of two playlists. Checks that the entries are the same entries, not just the contents
 */
private fun equals (playlist1: List<TabWithDataPlaylistEntry>, playlist2: List<TabWithDataPlaylistEntry>): Boolean {
    if (playlist1.size != playlist2.size) {
        return false
    }

    for (i in playlist1.indices) {
        if (playlist1[i].entryId != playlist2[i].entryId) {
            return false
        }
    }

    return true
}

@Composable @Preview
private fun PlaylistSongListPreview() {
    AppTheme {
        PlaylistSongList(songs = createListOfTabWithPlaylistEntry(20), navigateToTabByPlaylistEntryId = {}, onReorder = { _, _->}, onRemove = {})
    }
}

@Composable @Preview
private fun EmptyPlaylistSongListPreview() {
    AppTheme {
        PlaylistSongList(
            songs = listOf(),
            navigateToTabByPlaylistEntryId = {},
            onReorder = { _, _ -> },
            onRemove = {}
        )
    }
}

private fun createListOfTabWithPlaylistEntry(size: Int): List<TabWithDataPlaylistEntry> {
    val listOfEntries = mutableListOf<TabWithDataPlaylistEntry>()
    for (id in 0..size) {
        listOfEntries.add(TabWithDataPlaylistEntry(entryId = id, playlistId = 1, tabId = id * 20, nextEntryId = if(id<size) id+1 else null,
            prevEntryId = if(id>0) id-1 else null, dateAdded = 0, songId = 12, songName = "Song $id", artistName ="Artist name",
            isVerified = false, numVersions = 4, type = "Chords", part = "part", version = 2, votes = 0,
            rating = 0.0, date = 0, status = "", presetId = 0, tabAccessType = "public", tpVersion = 0,
            tonalityName = "D", versionDescription = "version desc", recordingIsAcoustic = false, recordingTonalityName = "",
            recordingPerformance = "", recordingArtists = arrayListOf(), recommended = arrayListOf(), userRating = 0,
            playlistUserCreated = false, playlistTitle = "playlist title", playlistDateCreated = 0, playlistDescription = "playlist desc",
            playlistDateModified = 0))
    }

    return listOfEntries
}
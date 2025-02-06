package com.gbros.tabslite.view.playlists

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.gbros.tabslite.R
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.InfoCard
import com.gbros.tabslite.view.songlist.SongListItem
import com.gbros.tabslite.view.swipetodismiss.MaterialSwipeToDismiss
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * Represents a playlist of songs.  Handles reordering of songs.
 */
@Composable
fun PlaylistSongList(
    songs: List<TabWithDataPlaylistEntry>,
    navigateToTabByPlaylistEntryId: (entryId: Int) -> Unit,
    onReorder: (src: TabWithDataPlaylistEntry, dest: TabWithDataPlaylistEntry, moveAfter: Boolean) -> Unit,
    onRemove: (tabToRemove: TabWithDataPlaylistEntry) -> Unit
) {
    // Use remember to create a MutableState object with a mutable collection type
    var reorderedSongsForDisplay by remember { mutableStateOf(songs) }
    var currentSongs by remember { mutableStateOf(songs) }

    // Observe changes in songs and update currentSongs accordingly
    DisposableEffect(songs) {
        reorderedSongsForDisplay = songs.toMutableList()
        currentSongs = songs.toMutableList()
        onDispose { }
    }

    // handle reorder state
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val reorderedSongList = reorderedSongsForDisplay.toMutableList().apply {
                if (from.index < reorderedSongsForDisplay.size) {  // if we're moving the spacer at the end, don't bother
                    // handle special case since we have a spacer at the end of the list which means
                    // we have one more list item than songs.  Always keep the spacer at the end.
                    val toIndex = to.index.coerceAtMost(reorderedSongsForDisplay.size-1)
                    if (toIndex != from.index) {
                        add(toIndex, removeAt(from.index))
                    }
                }
            }

            reorderedSongsForDisplay = reorderedSongList
        },
        onDragEnd = { startIndex, endIndex ->
            if (startIndex < currentSongs.size && startIndex != endIndex) {  // startIndex should always be <= currentSongs.size since we shouldn't be able to drag the spacer
                // handle special case since we have a spacer at the end of the list which means
                // we have one more list item than songs.  Always keep the spacer at the end.
                val toIndex = endIndex.coerceAtMost(currentSongs.size-1)
                if (toIndex != startIndex) {
                    onReorder(currentSongs[startIndex], currentSongs[toIndex], startIndex < toIndex)
                }
            }
        }
    )

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
    } else {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets(
                    left = max(8.dp, WindowInsets.safeDrawing.asPaddingValues().calculateLeftPadding(
                        LocalLayoutDirection.current)),
                    right = max(8.dp, WindowInsets.safeDrawing.asPaddingValues().calculateRightPadding(
                        LocalLayoutDirection.current))
                ))
        ) {
            LazyColumn(
                state = reorderState.listState,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .reorderable(reorderState)
                    .detectReorderAfterLongPress(reorderState)
                    .fillMaxHeight()
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(items = reorderedSongsForDisplay, key = { it }) { song ->
                    ReorderableItem(state = reorderState, key = song) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp,
                            label = "playlist item elevation"
                        )

                        MaterialSwipeToDismiss(onRemove = {
                            onRemove(song)
                        }) {
                            SongListItem(
                                song = song,
                                modifier = Modifier
                                    .shadow(elevation.value),
                                prependItem = {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_drag_handle),
                                        contentDescription = stringResource(R.string.generic_action_drag_to_reorder),
                                        modifier = Modifier
                                            .detectReorder(reorderState)
                                            .padding(end = 4.dp)
                                    )
                                },
                                onClick = { navigateToTabByPlaylistEntryId(song.entryId) }
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(height = 24.dp))
                    Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets(
                        bottom = WindowInsets.safeContent.getBottom(LocalDensity.current)
                    )))
                }
            }
        }
    }

}

@Composable @Preview
private fun PlaylistSongListPreview() {
    AppTheme {
        PlaylistSongList(songs = createListOfTabWithPlaylistEntry(20), navigateToTabByPlaylistEntryId = {}, onReorder = { _, _, _->}, onRemove = {})
    }
}

@Composable @Preview
private fun EmptyPlaylistSongListPreview() {
    AppTheme {
        PlaylistSongList(
            songs = listOf(),
            navigateToTabByPlaylistEntryId = {},
            onReorder = { _, _, _ -> },
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
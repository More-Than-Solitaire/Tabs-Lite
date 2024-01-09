package com.gbros.tabslite.compose.playlists

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.compose.songlist.SongListItem
import com.gbros.tabslite.data.tab.TabWithPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme
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
    songs: List<TabWithPlaylistEntry>,
    navigateToTabByPlaylistEntryId: (entryId: Int) -> Unit,
    onReorder: (src: TabWithPlaylistEntry, dest: TabWithPlaylistEntry, moveAfter: Boolean) -> Unit
) {
    // Use remember to create a MutableState object with a mutable collection type
    var reorderedSongsForDisplay by remember { mutableStateOf(songs) }
    var currentSongs by remember { mutableStateOf(songs) }

    // Observe changes in songs and update currentSongs accordingly
    DisposableEffect(songs) {
        reorderedSongsForDisplay = songs.toMutableList()
        currentSongs = songs.toMutableList()
        onDispose { }    }

    // handle reorder state
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val reorderedSongList = reorderedSongsForDisplay.toMutableList().apply {
                if (reorderedSongsForDisplay.isNotEmpty()) {
                    add(to.index, removeAt(from.index))
                }
            }

            reorderedSongsForDisplay = reorderedSongList
        },
        onDragEnd = { startIndex, endIndex ->
            onReorder(currentSongs[startIndex], currentSongs[endIndex], startIndex < endIndex)
        }
    )

    LazyColumn(
        state = reorderState.listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .reorderable(reorderState)
            .detectReorderAfterLongPress(reorderState)
            .padding(top = 8.dp)
    ) {
        items(items = reorderedSongsForDisplay, key = {it}) { song ->
            ReorderableItem(state = reorderState, key = song) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)

                SongListItem(
                    song = song,
                    modifier = Modifier
                        .shadow(elevation.value),
                    prependItem = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drag to reorder",
                            modifier = Modifier
                                .detectReorder(reorderState)
                        )
                    },
                    onClick = { navigateToTabByPlaylistEntryId(song.entryId) }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(height = 24.dp))
        }
    }
}

@Composable @Preview
private fun PlaylistSongListPreview() {
    AppTheme {
        PlaylistSongList(songs = createListOfTabWithPlaylistEntry(20), navigateToTabByPlaylistEntryId = {}, onReorder = { _, _, _->})
    }
}

private fun createListOfTabWithPlaylistEntry(size: Int): List<TabWithPlaylistEntry> {
    val listOfEntries = mutableListOf<TabWithPlaylistEntry>()
    for (id in 0..size) {
        listOfEntries.add(TabWithPlaylistEntry(entryId = id, playlistId = 1, tabId = id * 20, nextEntryId = if(id<size) id+1 else null,
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
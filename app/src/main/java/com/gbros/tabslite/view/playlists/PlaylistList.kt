package com.gbros.tabslite.view.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.ui.theme.AppTheme
import com.gbros.tabslite.view.card.InfoCard

@Composable
fun PlaylistList(modifier: Modifier = Modifier, livePlaylists: LiveData<List<Playlist>>, navigateToPlaylistById: (Int) -> Unit, verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp)){
    val playlists by livePlaylists.observeAsState(listOf())

    if (playlists.isEmpty()) {
        // no playlists
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp)
        ) {
            InfoCard(text = "Create your first playlist by clicking the + button here, or find a song to start and then select the three dot menu at the top right of the screen")
        }
    } else {

        LazyColumn(
            verticalArrangement = verticalArrangement,
            modifier = modifier
        ) {
            item {
                Spacer(modifier = Modifier.height(height = 6.dp))
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets(
                    top = WindowInsets.safeDrawing.getTop(LocalDensity.current)
                )))
            }
            items(playlists) { playlist ->
                PlaylistListItem(playlist = playlist) {
                    navigateToPlaylistById(playlist.playlistId)
                }
            }
            item {
                Spacer(modifier = Modifier.height(height = 24.dp))
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets(
                    bottom = WindowInsets.safeDrawing.getBottom(LocalDensity.current)
                )))
            }
        }
    }
}

@Composable @Preview
private fun PlaylistListPreview() {
    val playlistForTest = Playlist(1, true, "My amazing playlist 1.0.1", 12345, 12345, "The playlist that I'm going to use to test this playlist entry item thing with lots of text.")
    val list = MutableLiveData(listOf(playlistForTest, playlistForTest, playlistForTest ,playlistForTest, playlistForTest))

    AppTheme {
        PlaylistList(livePlaylists = list, navigateToPlaylistById = {})
    }
}
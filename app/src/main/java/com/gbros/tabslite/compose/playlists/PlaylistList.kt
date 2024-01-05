package com.gbros.tabslite.compose.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.Playlist
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun PlaylistList(livePlaylists: LiveData<List<Playlist>>, navigateToPlaylistById: (Int) -> Unit, verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp), modifier: Modifier = Modifier){
    val playlists by livePlaylists.observeAsState(listOf())
    LazyColumn (
        verticalArrangement = verticalArrangement,
        modifier = modifier
    ) {
        items(playlists) { playlist ->
            PlaylistListItem(playlist = playlist) {
                navigateToPlaylistById(playlist.playlistId)
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
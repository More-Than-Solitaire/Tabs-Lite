package com.gbros.tabslite.compose.songlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.data.TabFullWithPlaylistEntry

@Composable
fun SongList(liveSongs: LiveData<List<IntTabFull>>, navigateToTabById: (Int) -> Unit, verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp), modifier: Modifier = Modifier){
    val songs = liveSongs.observeAsState(listOf())
    LazyColumn (
        verticalArrangement = verticalArrangement,
        modifier = modifier
    ) {
        items(songs.value) { song ->
            SongListItem(song = song, onClick = {navigateToTabById(song.tabId)})
        }
    }
}

@Composable @Preview
fun SongListPreview(){
    val tabForTest1 = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabForTest2 = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    val tabListForTest = MutableLiveData(listOf<IntTabFull>(tabForTest1, tabForTest2))

    SongList(liveSongs = tabListForTest, navigateToTabById = {})
}
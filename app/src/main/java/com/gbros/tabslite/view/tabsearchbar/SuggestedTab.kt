package com.gbros.tabslite.view.tabsearchbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.playlist.Playlist.Companion.FAVORITES_PLAYLIST_ID
import com.gbros.tabslite.data.playlist.Playlist.Companion.TOP_TABS_PLAYLIST_ID
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun SuggestedTab(
    modifier: Modifier = Modifier,
    tab: TabWithDataPlaylistEntry,
    onClick: () -> Unit,
) {
    Card(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val icon = when {
                tab.playlistId == FAVORITES_PLAYLIST_ID -> Icons.Default.Favorite
                tab.playlistId == TOP_TABS_PLAYLIST_ID -> Icons.Default.Person
                tab.entryId > 0 -> ImageVector.vectorResource(id = R.drawable.ic_playlist_play)
                else -> ImageVector.vectorResource(id = R.drawable.ic_search_activity)
            }
            Icon(
                modifier = Modifier
                    .padding(end = 4.dp),
                imageVector = icon,
                contentDescription = null
            )
            Text(
                text = tab.songName,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Spacer(modifier = Modifier.weight(1f, fill=true))

            Text(
                text = tab.artistName,
                fontStyle = FontStyle.Italic,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .padding(start = 6.dp)
            )
        }
    }
}

@Composable
@Preview
private fun SuggestedTabPreview() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        songName = "Three Little Birds",
        artistName = "Bob Marley"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SuggestedTabPreviewFavorite() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        playlistId = -1,
        songName = "Three Little Birds",
        artistName = "Bob Marley"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SuggestedTabPreviewPopular() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        playlistId = -2,
        songName = "Three Little Birds",
        artistName = "Bob Marley"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SuggestedTabPreviewPlaylist() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        entryId = 1,
        songName = "Three Little Birds",
        artistName = "Bob Marley"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SuggestedTabPreviewTextOverflow() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        songName = "Three Little Birds and a lot lot more long title",
        artistName = "Bob Marley with a long artist name as well"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SuggestedTabPreviewTextOverflowTitleOnly() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        songName = "Three Little Birds and a lot lot more long title",
        artistName = "Bob"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SuggestedTabPreviewTextOverflowArtistOnly() {
    val suggestion = TabWithDataPlaylistEntry(
        tabId = "0",
        songName = "Birds",
        artistName = "Bob with a very very long artist name that should overflow"
    )
    AppTheme {
        SuggestedTab(
            tab = suggestion,
            onClick = {}
        )
    }
}

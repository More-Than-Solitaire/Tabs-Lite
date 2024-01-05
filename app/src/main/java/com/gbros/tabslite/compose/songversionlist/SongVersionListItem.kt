package com.gbros.tabslite.compose.songversionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.data.IntTabFull
import com.gbros.tabslite.data.TabFullWithPlaylistEntry
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun SongVersionListItem(song: IntTabFull, onClick: () -> Unit){
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .focusable()
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(all = 5.dp)
        ){
            Text(
                text = stringResource(R.string.version_number, song.version),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
            )
            RatingIcon(rating = song.rating)
            Text(
                text = song.votes.toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 1.dp)
            )
        }
    }
}

@Composable
private fun RatingIcon(rating: Double){
    var filledStars = floor(rating).toInt()
    var unfilledStars = (5 - ceil(rating)).toInt()
    var halfStar = false
    val remainder = rating.rem(1)

    // round to the nearest half star
    if (remainder >= .8) filledStars++
    else if (remainder < .25) unfilledStars++
    else halfStar = true

    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ){
        repeat(filledStars) {
            Icon(imageVector = Icons.Default.Star, contentDescription = "Filled star", tint = MaterialTheme.colorScheme.primary)
        }

        if (halfStar) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_rating_star_left_half),
                contentDescription = "Half star",
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_rating_star_right_half),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background
            )
        }

        repeat(unfilledStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,

            )
        }
    }
}

@Composable @Preview
private fun SongVersionListItemPreview() {
    val tabForTest = TabFullWithPlaylistEntry(1, 1, 1, 1, 1, 1234, 0, 1, "Long Time Ago", "CoolGuyz", false, 5, "Chords", "", 1, 4, 3.6, 1234, "" , 123, "public", 1, "E A D G B E", "description", false, "asdf", "", ArrayList(), ArrayList(), 4, "expert", playlistDateCreated = 12345, playlistDateModified = 12345, playlistDescription = "Description of our awesome playlist", playlistTitle = "My Playlist", playlistUserCreated = true, capo = 2, contributorUserName = "Joe Blow")
    SongVersionListItem(song = tabForTest) {}
}
package com.gbros.tabslite.view.tabsearchbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.ui.theme.AppTheme

@Composable
fun SuggestedTab(modifier: Modifier = Modifier, tab: ITab, onClick: (tabId: Int) -> Unit) {
    Card(
        modifier = modifier
            .clickable(onClick = {onClick(tab.tabId)})
    ) {
        Row(
            modifier = Modifier
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .padding(end = 4.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_search_activity),
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
    val suggestion = Tab(
        tabId = 0,
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
    val suggestion = Tab(
        tabId = 0,
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
    val suggestion = Tab(
        tabId = 0,
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
    val suggestion = Tab(
        tabId = 0,
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

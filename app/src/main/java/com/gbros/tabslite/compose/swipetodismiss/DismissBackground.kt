package com.gbros.tabslite.compose.swipetodismiss

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


/**
 * The background for a swipe-to-dismiss element.  Thanks https://www.geeksforgeeks.org/android-jetpack-compose-swipe-to-dismiss-with-material-3/
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(
    dismissState: DismissState,
    colors: DismissBackgroundColors = DismissBackgroundColors(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError, MaterialTheme.colorScheme.onError),
    icons: DismissBackgroundIcons = DismissBackgroundIcons(Icons.Default.Delete, Icons.Default.Delete),
    contentDescriptions: DismissBackgroundContentDescriptions = DismissBackgroundContentDescriptions("Delete", "Delete")
) {
    val color = when (dismissState.dismissDirection) {
        DismissDirection.StartToEnd -> colors.startToEndBackgroundColor
        DismissDirection.EndToStart -> colors.endToStartBackgroundColor
        null -> Color.Transparent
    }
    val direction = dismissState.dismissDirection

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (direction == DismissDirection.StartToEnd) Icon(
            icons.startToEndIcon,
            tint = colors.startToEndIconColor,
            contentDescription = contentDescriptions.startToEndContentDescription
        )
        Spacer(modifier = Modifier)
        if (direction == DismissDirection.EndToStart) Icon(
            icons.endToStartIcon,
            tint = colors.endToStartIconColor,
            contentDescription = contentDescriptions.endToStartContentDescription
        )
    }
}

class DismissBackgroundColors(val startToEndBackgroundColor: Color, val endToStartBackgroundColor: Color, val startToEndIconColor: Color, val endToStartIconColor: Color)

class DismissBackgroundIcons(val startToEndIcon: ImageVector, val endToStartIcon: ImageVector)

class DismissBackgroundContentDescriptions(val startToEndContentDescription: String, val endToStartContentDescription: String)
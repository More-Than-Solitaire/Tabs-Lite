package com.gbros.tabslite.compose.swipetodismiss

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Composable representing swipe-to-dismiss functionality.  Thanks https://www.geeksforgeeks.org/android-jetpack-compose-swipe-to-dismiss-with-material-3/
 *
 * @param content The content to include in the SwipeToDismiss.
 * @param onRemove Callback invoked when the email item is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSwipeToDismiss(
    onRemove: (DismissDirection) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var show by remember { mutableStateOf(true) }
    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                show = false
                onRemove(if (it == DismissValue.DismissedToStart) DismissDirection.EndToStart else DismissDirection.StartToEnd)
                true
            } else false
        }, positionalThreshold = { 150.dp.toPx() }
    )
    AnimatedVisibility(
        show, exit = fadeOut(spring())
    ) {
        SwipeToDismiss(
            state = dismissState,
            modifier = Modifier,
            background = {
                DismissBackground(dismissState)
            },
            dismissContent = content
        )
    }
}
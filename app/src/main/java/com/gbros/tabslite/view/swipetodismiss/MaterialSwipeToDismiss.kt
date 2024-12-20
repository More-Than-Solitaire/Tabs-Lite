package com.gbros.tabslite.view.swipetodismiss

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gbros.tabslite.view.playlists.RemovePlaylistEntryConfirmationDialog

/**
 * Composable representing swipe-to-dismiss functionality.  Thanks https://www.geeksforgeeks.org/android-jetpack-compose-swipe-to-dismiss-with-material-3/
 *
 * @param content The content to include in the SwipeToDismiss.
 * @param onRemove Callback invoked when the email item is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSwipeToDismiss(
    onRemove: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var show by remember { mutableStateOf(true) }  // whether to show the row at all
    var resetEntryRemoval by remember { mutableStateOf(false) }  // trigger a reset of the removal state
    var showEntryConfirmationDialog by remember { mutableStateOf(false) }  // trigger the entry removal confirmation dialog
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                showEntryConfirmationDialog = true  // trigger entry removal confirmation dialog
                true
            } else false
        }
    )
    AnimatedVisibility(
        show, exit = fadeOut(spring())
    ) {
        setOf(SwipeToDismissBoxValue.EndToStart,
            SwipeToDismissBoxValue.StartToEnd
        )
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DismissBackground(dismissState)
            },
            modifier = Modifier,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            content = content
        )
    }

    // confirm entry removal
    if (showEntryConfirmationDialog) {
        RemovePlaylistEntryConfirmationDialog(
            onConfirm = { onRemove(); showEntryConfirmationDialog = false; show = false },
            onDismiss = { showEntryConfirmationDialog = false; resetEntryRemoval = true }
        )
    }

    // handle removal cancelled
    LaunchedEffect(key1 = resetEntryRemoval) {
        if(resetEntryRemoval) {
            dismissState.reset()  // undo a removal
            resetEntryRemoval = false;
        }
    }
}
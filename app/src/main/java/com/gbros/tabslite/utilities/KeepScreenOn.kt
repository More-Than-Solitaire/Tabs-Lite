package com.gbros.tabslite.utilities

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Keep screen on for the current view
 */
@OptIn(ExperimentalUuidApi::class)
@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    val myUserId = Uuid.random()  // the ID of *this* instance of this composable

    DisposableEffect(Unit) {
        ScreenOnHelper.screenOnUsers.putIfAbsent(currentView.id, mutableListOf())
        ScreenOnHelper.screenOnUsers[currentView.id]!!.add(myUserId)
        currentView.keepScreenOn = true
        Log.d(TAG, "enabled keepScreenOn for ${currentView.id}")

        onDispose {
            ScreenOnHelper.screenOnUsers[currentView.id]!!.remove(myUserId)
            if (ScreenOnHelper.screenOnUsers[currentView.id]!!.isEmpty()) {
                // we were the last ones needing this screen kept on; disable
                currentView.keepScreenOn = false
                Log.d(TAG, "disabling keepScreenOn for ${currentView.id}")
            }
        }
    }
}

/**
 * Singleton object to keep track across views which users need the screen on
 */
private object ScreenOnHelper {
    /**
     * A list of all the people currently requiring the screen to be kept on. When this list empties
     * the screenOn requirement is no longer needed
     *
     * This list represents <ViewId, <list of users for that view>>
     */
    @OptIn(ExperimentalUuidApi::class)
    val screenOnUsers: MutableMap<Int, MutableList<Uuid>> = mutableMapOf()
}
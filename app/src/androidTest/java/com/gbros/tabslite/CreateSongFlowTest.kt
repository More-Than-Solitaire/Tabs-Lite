package com.gbros.tabslite

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateSongFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<HomeActivity>()

    @Test
    fun searchFlowShowsCreateNewSongScreen() {
        // Step 1: Open AboutDialog via the leading icon button in the search bar
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("aboutButton").performClick()

        // Step 2: Click "Create New Tab" in the AboutDialog
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Create New Tab").performClick()

        // Step 3: Type a search query that won't match any song
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextInput("zzzzzz_no_match_123")
        composeRule.onNode(hasSetTextAction()).performImeAction()

        // Step 4: Verify "Song doesn't exist yet?" and click "Create a new song"
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Song doesn't exist yet?").assertIsDisplayed()
        composeRule.onNodeWithText("Create a new song").performClick()

        // Step 5: Verify the Create Song screen is shown
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Create Song").assertIsDisplayed()
    }
}

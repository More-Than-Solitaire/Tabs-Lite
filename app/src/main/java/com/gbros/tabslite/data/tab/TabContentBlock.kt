package com.gbros.tabslite.data.tab

import androidx.compose.ui.text.AnnotatedString

/**
 * Represents one line of chord content in a tab (or a block of non-chord text)
 */
data class TabContentBlock(
    /**
     * The textual content of the tab, along with chord or URL annotations
     */
    val content: AnnotatedString,

    /**
     * Whether the content was in a `[tab]` block. Informs content styling. If true, content will be double-spaced and chords will be raised above
     * the text content. This will usually be true for most tab content lines, false for introductory text and chord-only interludes.
     */
    val tab: Boolean
)

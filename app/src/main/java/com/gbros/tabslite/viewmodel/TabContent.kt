package com.gbros.tabslite.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.gbros.tabslite.data.tab.TabContentBlock
import com.gbros.tabslite.utilities.TAG

class TabContent(private val urlHandler: (String) -> Unit, content: String){
    val contentBlocks: List<TabContentBlock>
    val chords: Set<String>

    init {
        val processedResult = processTabContent(content)
        this.contentBlocks = processedResult.first
        this.chords = processedResult.second
    }

    //region Process Tab Content

    /**
     * Word wrap, style, and annotate a given tab.  Does not add click functionality, but adds an annotation around
     * every chord with tag "chord"
     */
    @OptIn(ExperimentalTextApi::class)
    private fun processTabContent(tabContent: String): Pair<List<TabContentBlock>, Set<String>> {
        val tagPattern = Regex("\\[tab](.*?)\\[/tab]", RegexOption.DOT_MATCHES_ALL)
        val chordsSet = mutableSetOf<String>()
        val contentBlocks = mutableListOf<TabContentBlock>()

        var lastIndex = 0
        tagPattern.findAll(tabContent).forEach { matchResult ->
            // Process text before the match (outside [tab] block)
            val beforeText = tabContent.substring(lastIndex, matchResult.range.first).trim()
            if (beforeText.isNotEmpty()) {
                val beforeBlock = buildContentBlock(beforeText, chordsSet, inline = true)
                contentBlocks.add(TabContentBlock(beforeBlock, tab = false))
            }

            // Process the content inside the [tab] block
            val tabBlockContent = matchResult.groupValues[1].trim()
            if (tabBlockContent.isNotEmpty()) {
                val tabBlock = buildContentBlock(tabBlockContent, chordsSet, inline = false)
                contentBlocks.add(TabContentBlock(tabBlock, tab = true))
            }

            lastIndex = matchResult.range.last + 1
        }

        // Process any remaining text after the last match (outside [tab] block)
        if (lastIndex < tabContent.length) {
            val remainingText = tabContent.substring(lastIndex).trim()
            if (remainingText.isNotEmpty()) {
                val remainingBlock = buildContentBlock(remainingText, chordsSet, inline = true)
                contentBlocks.add(TabContentBlock(remainingBlock, tab = false))
            }
        }

        return Pair(contentBlocks, chordsSet)
    }

    /**
     * Build an AnnotatedString block with chord annotations and hyperlinks
     */
    @OptIn(ExperimentalTextApi::class)
    private fun buildContentBlock(content: String, chordsSet: MutableSet<String>, inline: Boolean): AnnotatedString {
        return buildAnnotatedString {
            appendChordContent(content, this, chordsSet, inline)

            // Add active hyperlinks
            val hyperlinks = getHyperLinks(this.toAnnotatedString().text)
            for (hyperlink in hyperlinks) {
                this.addLink(
                    LinkAnnotation.Url(hyperlink.value, linkInteractionListener = {
                        val url = (it as LinkAnnotation.Url).url.trim()
                        urlHandler(url)
                    }),
                    hyperlink.range.first,
                    hyperlink.range.last + 1
                )
            }
        }
    }
    /**
     * Represents a found chord match, including its start and end indices in the original text,
     * and the extracted chord name.
     */
    private data class ChordMatch(val start: Int, val end: Int, val chordName: String)

    /**
     * Finds the next occurrence of a chord tag (either [ch]...[/ch] or {ch:...}) starting from
     * a given index.
     * @param text The text to search within.
     * @param startIndex The index to start the search from.
     * @return A [ChordMatch] object if a chord is found, or null otherwise.
     */
    private fun findNextChordMatch(text: CharSequence, startIndex: Int): ChordMatch? {
        val newStartTag = "{ch:"
        val newEndTag = "}"

        val nextNewStart = text.indexOf(newStartTag, startIndex)

        // If no chords found at all
        if (nextNewStart == -1) {
            return null
        }

        // New format is first or only one
        val firstIndex = nextNewStart
        val endIndex = text.indexOf(newEndTag, firstIndex + newStartTag.length)
        if (endIndex == -1) {
            Log.w(TAG, "Couldn't find closing $newEndTag tag for chord starting at position $firstIndex")
            return null // Malformed tag, skip this one
        }
        val chordName = text.subSequence(firstIndex + newStartTag.length, endIndex).toString()
        return ChordMatch(firstIndex, endIndex + newEndTag.length, chordName)
    }

    /**
     * Annotate, style, and append a line with chords to the given annotated string builder
     */
    @OptIn(ExperimentalTextApi::class)
    private fun appendChordContent(content: CharSequence, builder: AnnotatedString.Builder, chords: MutableSet<String>, inline: Boolean = false) {
        var currentIndex = 0 // the index of the last already-consumed character in text

        while (true) {
            val chordMatch = findNextChordMatch(content, currentIndex) ?: break // No more chords found in the line

            // Append any non-chord text before the current chord
            builder.append(content.subSequence(currentIndex, chordMatch.start))

            currentIndex = chordMatch.end
            chords.add(chordMatch.chordName)

            // get the next character after the chord tag. This is what we will attach the chord annotation to
            var nextContentCharacter = content.elementAtOrNull(currentIndex)
            if (inline || nextContentCharacter == null || nextContentCharacter == '{') {
                nextContentCharacter = ' ' // chord tags should be ignored; use a space as the character to put the chord annotation on
            } else {
                currentIndex++ // consume the next character
            }

            // Append the next character, annotated with the chord
            val annotationPrefix = if (inline) ("{il}") else ""
            builder.withAnnotation("chord", annotationPrefix + chordMatch.chordName) {
                if (inline) {
                    // leave content for the button to cover so the spacing isn't weird
                    withStyle(SpanStyle().copy(color = Color.Transparent)) {
                        append(chordMatch.chordName)
                    }
                    append('\u00A0')  // non-breaking space to give room for button padding
                } else {
                    append(nextContentCharacter)
                }
            }
        }

        // Append any remaining non-chords after the last chord (or the entire line if no chords were found)
        builder.append(content.substring(currentIndex))
    }

    private fun getHyperLinks(s: String): Sequence<MatchResult> {
        val urlPattern = Regex(
            "(?:^|\\W)((ht|f)tp(s?)://|www\\.)"
                    + "(([\\w\\-]+\\.)+([\\w\\-.~]+/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
            setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )

        return urlPattern.findAll(s)
    }

    //endregion Process Tab Content
}
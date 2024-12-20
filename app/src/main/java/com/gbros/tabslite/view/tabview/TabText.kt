package com.gbros.tabslite.view.tabview

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.gbros.tabslite.R
import com.gbros.tabslite.view.chorddisplay.ChordModalBottomSheet
import com.gbros.tabslite.ui.theme.AppTheme
import com.smarttoolfactory.gesture.detectTransformGestures
import kotlin.math.floor


private const val LOG_NAME = "tabslite.TabText    "

// font size constraints, measured in sp
private const val MIN_FONT_SIZE_SP = 2f
private const val MAX_FONT_SIZE_SP = 36f
private const val FALLBACK_FONT_SIZE_SP = 14f  // fall back to a font size of 14.sp if the system font size can't be read

// To calculate the aspect ratio of a ttf font, run this in python (after pip install fonttools):
// aspect_ratio = ttLib.TTFont(r'path\to\font.ttf')['hmtx']['space'][0] / ttLib.TTFont(r'path\to\font.ttf')['head'].unitsPerEm
private const val ROBOTO_ASPECT_RATIO = 0.60009765625  // the width-to-height ratio of roboto mono Regular.

@Composable
fun TabText(modifier: Modifier = Modifier, text: String, onChordClick: (String) -> Unit) {
    // default the font size to whatever the user default font size is.  This respects system font settings.
    val defaultFontSize = MaterialTheme.typography.bodyMedium.fontSize
    val localDensity = LocalDensity.current
    val defaultFontSizeInSp = remember {
        if (defaultFontSize.isSp) {
            defaultFontSize.value
        } else if (defaultFontSize.isEm) {
            defaultFontSize.value / localDensity.density
        } else {
            FALLBACK_FONT_SIZE_SP
        }
    }
    var fontSize by remember { mutableFloatStateOf(defaultFontSizeInSp) }

    // click handler for URLs and chords
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val urlInteractionListener = remember {
        LinkInteractionListener { link: LinkAnnotation ->
            try {
                uriHandler.openUri((link as LinkAnnotation.Url).url)
            } catch (ex: ActivityNotFoundException) {
                Log.w(LOG_NAME, "Couldn't launch URL, copying to clipboard instead")
                clipboardManager.setText(AnnotatedString((link as LinkAnnotation.Url).url))
            }
        }
    }
    val chordInteractionListener = remember {
        LinkInteractionListener { chord: LinkAnnotation ->
            onChordClick((chord as LinkAnnotation.Clickable).tag)
        }
    }

    // dynamic variables
    val characterHeightInPixels = with(localDensity) { fontSize.sp.toPx() }  // this changes when font size changes
    val characterWidthInPixels = characterHeightInPixels * ROBOTO_ASPECT_RATIO  // this changes when font size changes
    var measuredWidth by remember { mutableIntStateOf(0) }
    val charsPerLine = floor(measuredWidth / characterWidthInPixels).toUInt()
    val colorScheme = MaterialTheme.colorScheme
    val annotatedTab by remember(key1 = text) { mutableStateOf(annotateTab(text, colorScheme, urlInteractionListener, chordInteractionListener)) }
    val wrappedAnnotatedTab by remember(key1 = annotatedTab, key2 = charsPerLine) { mutableStateOf(wrapTab(annotatedTab, charsPerLine)) }
    val font = remember { FontFamily(Font(R.font.roboto_mono_variable_weight)) }
    Text(
        text = wrappedAnnotatedTab,
        style = TextStyle(
            fontFamily = font,
            fontSize = TextUnit(fontSize, TextUnitType.Sp),
            color = MaterialTheme.colorScheme.onBackground
        ),
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures(consume = false, onGesture = { _, _, zoom, _, _, _ ->
                    if (zoom != 1.0f) {
                        // handle zooming by changing font size (within constraints)
                        fontSize = (fontSize * zoom).coerceIn(MIN_FONT_SIZE_SP, MAX_FONT_SIZE_SP)
                    }
                })
            }
            .onGloballyPositioned { layoutResult ->
                measuredWidth = layoutResult.size.width
            }
    )
}

@Composable @Preview
fun TabTextTestCase1() {
    AppTheme {
        val testCase1 = """
        [tab]     [ch]C[/ch]                   [ch]Am[/ch] 
        That David played and it pleased the Lord[/tab]
        https://tabslite.com/asdf
    """.trimIndent()
        var bottomSheetTrigger by remember { mutableStateOf(false) }
        var chordToShow by remember { mutableStateOf("Am") }

        TabText(
            text = testCase1,
            onChordClick = { chordName ->
                chordToShow = chordName
                bottomSheetTrigger = true
            }
        )

        if (bottomSheetTrigger) {
            ChordModalBottomSheet(chordToShow) {
                Log.d(LOG_NAME, "bottom sheet dismissed")
                bottomSheetTrigger = false
            }
        }
    }
}

@Composable @Preview
fun TabTextPreview() {
    val hallelujahTabForTest = """
        [Intro]
        [ch]C[/ch] [ch]Em[/ch] [ch]C[/ch] [ch]Em[/ch]
         
        [Verse]
        [tab][ch]C[/ch]                [ch]Em[/ch]
          Hey there Delilah, What’s it like in New York City?[/tab]
        [tab]      [ch]C[/ch]                                      [ch]Em[/ch]                                  [ch]Am[/ch]   [ch]G[/ch]
        I’m a thousand miles away, But girl tonight you look so pretty, Yes you do, [/tab]
        
        [tab]F                   [ch]G[/ch]                  [ch]Am[/ch]
          Time Square can’t shine as bright as you, [/tab]
        [tab]             [ch]G[/ch]
        I swear it’s true. [/tab]
        [tab][ch]C[/ch]
          Hey there Delilah, [/tab]
        [tab]          [ch]Em[/ch]
        Don’t you worry about the distance, [/tab]
        [tab]          [ch]C[/ch]
        I’m right there if you get lonely, [/tab]
        [tab]          [ch]Em[/ch]
        [ch]G[/ch]ive this song another listen, [/tab]
        [tab]           [ch]Am[/ch]     [ch]G[/ch]
        Close your eyes, [/tab]
        [tab]F              [ch]G[/ch]                [ch]Am[/ch]
          Listen to my voice it’s my disguise, [/tab]
        [tab]            [ch]G[/ch]
        I’m by your side.[/tab]    """.trimIndent()
    AppTheme {
        TabText(
            text = hallelujahTabForTest,
            onChordClick = {}
        )
    }
}

//region Process Tab Content

/**
 * Word wrap a given tab
 */
private fun wrapTab(content: AnnotatedString, availableWidthInChars: UInt): AnnotatedString {
    if (availableWidthInChars == 0u) {
        return content  // shortcut initial run with no available width
    }

    val wrappedTab = buildAnnotatedString {
        var indexOfEndOfTabBlock = 0
        while (content.indexOf("[tab]", indexOfEndOfTabBlock) != -1) {  // loop through each [tab] line representing lyrics and the chords to go with them
            val indexOfStartOfTabBlock = content.indexOf("[tab]", indexOfEndOfTabBlock)
            // any content before the [tab] block starts (and after the last [/tab] block ended) should be added without custom word-wrapping.  Default wrapping can take care of long lines here.
            append(content.subSequence(indexOfEndOfTabBlock, indexOfStartOfTabBlock))

            indexOfEndOfTabBlock = content.indexOf("[/tab]", indexOfStartOfTabBlock)+6
            if (indexOfEndOfTabBlock-6 == -1) indexOfEndOfTabBlock = content.length+6

            if (availableWidthInChars != 0u) {  // ignore [tab] block wrapping if availableWidth is 0
                // any content that *is* inside [tab] blocks should be custom word-wrapped (wrapped two lines at a time)
                val tabBlock = content.subSequence(indexOfStartOfTabBlock+5, indexOfEndOfTabBlock-6)
                appendDoubleWrapped(tabBlock, availableWidthInChars)
            }
        }

        // append anything after the last tab block
        if (indexOfEndOfTabBlock < content.length) {
            append(content.subSequence(indexOfEndOfTabBlock, content.length))
        }
    }

    return wrappedTab
}

/**
 * Processes and wraps the lines for the tab block, then appends to the annotated string builder.
 */
private fun AnnotatedString.Builder.appendDoubleWrapped(tabBlock: AnnotatedString, availableWidthInChars: UInt) {
    // get existing lines
    val lines = mutableListOf<AnnotatedString>()
    var lastLineEndIndex = 0
    var nextNewLineCharIndex = tabBlock.indexOf('\n')
    while (nextNewLineCharIndex != -1) {
        lines.add(tabBlock.subSequence(lastLineEndIndex, nextNewLineCharIndex))
        lastLineEndIndex = nextNewLineCharIndex
        nextNewLineCharIndex = tabBlock.indexOf('\n', lastLineEndIndex+1)
    }
    if (tabBlock.length > lastLineEndIndex) {
        lines.add(tabBlock.subSequence(lastLineEndIndex, tabBlock.length))
    }

    // double wrap the lines
    for (i in 0..< lines.count() step 2) {
        val line1 = lines[i]
        val line2: AnnotatedString? = if (i+1 < lines.count()) lines[i+1] else null
        val wrappedLines = wrapLinePair(line1, line2, availableWidthInChars)

        for(wrappedLine in wrappedLines) {
            append(wrappedLine)
        }
    }
}


/**
 * Creates an AnnotatedString with any hyperlinks converted to LinkAnnotation.Url and chords to LinkAnnotation.Clickable. Does not perform word wrapping
 */
private fun annotateTab(text: CharSequence, colorScheme: ColorScheme, urlInteractionListener: LinkInteractionListener, chordInteractionListener: LinkInteractionListener): AnnotatedString {
    return buildAnnotatedString {
        val links = getHyperLinks(text)
        //val links = emptyList<MatchResult>()
        var lastUnusedCharacterIndex = 0
        for (link in links) {
            // append the non-link text
            withChords(
                text.subSequence(lastUnusedCharacterIndex, link.range.first), colorScheme, chordInteractionListener
            )

            // append the link
            withLink(
                LinkAnnotation.Url(
                    url = link.value.trim(), styles = TextLinkStyles(
                        SpanStyle(
                            color = colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ), linkInteractionListener = urlInteractionListener
                )
            ) {
                append(link.value)
            }
            lastUnusedCharacterIndex = link.range.last + 1
        }

        // add any remaining text to sequence
        if (lastUnusedCharacterIndex <= text.length) {
            withChords(
                text.subSequence(lastUnusedCharacterIndex until text.length),
                colorScheme,
                chordInteractionListener
            )
        }
    }
}

/**
 * Appends text to the builder, adding LinkAnnotation.Clickable for chords
 */
private fun AnnotatedString.Builder.withChords(text: CharSequence, colorScheme: ColorScheme, chordInteractionListener: LinkInteractionListener) {
    var lastIndex = 0

    while (text.indexOf("[ch]", lastIndex) != -1) {  // while chords in text
        val firstIndex = text.indexOf("[ch]", lastIndex)  // index of start of [ch]
        append(text.subSequence(lastIndex, firstIndex))  // append any non-chords
        lastIndex = text.indexOf("[/ch]", firstIndex)+5  // index of end of [/ch]

        // handle error case where we can't find a closing tag for this chord.  Handle gracefully and log warning
        if (lastIndex-5 == -1) {
            Log.w(LOG_NAME, "Couldn't find closing [/ch] tag for chord starting at position $firstIndex")
            lastIndex = firstIndex+4  // start the next loop after that [ch] tag
            continue // skip this chord
        }

        val chordName = text.subSequence(firstIndex+4 until lastIndex-5)

        // append the chord link
        withLink(
            LinkAnnotation.Clickable(tag = "$chordName", styles = TextLinkStyles(SpanStyle(
                color = colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                background = colorScheme.primaryContainer
            )), linkInteractionListener = chordInteractionListener)
        ) {
            append(chordName)
        }
    }

    // append any remaining non-chords
    append(text.subSequence(lastIndex until text.length))
}

/**
 * Take a pair of lines and return a list of lines shorter than the available width, wrapped as a pair.
 */
private fun wrapLinePair(line1: AnnotatedString, line2: AnnotatedString?, availableWidthInChars: UInt): List<AnnotatedString> {
    val wrappedLines = mutableListOf<AnnotatedString>()
    if (line2 != null) {
        var remainingLine1: AnnotatedString = line1
        var remainingLine2: AnnotatedString = line2

        // append two lines as long as EITHER is not empty
        while (remainingLine1.isNotEmpty() || remainingLine2.isNotEmpty()) {
            val wordBreakLocation = findDoubleLineWordBreakIndex(availableWidthInChars.toInt(), remainingLine1, remainingLine2)

            if (wordBreakLocation < remainingLine1.length) {
                wrappedLines.add(remainingLine1.subSequence(0, wordBreakLocation))
                remainingLine1 = remainingLine1.subSequence(wordBreakLocation, remainingLine1.length)
            } else {
                wrappedLines.add(remainingLine1)
                remainingLine1 = AnnotatedString("")
            }

            if (wordBreakLocation < remainingLine2.length) {
                wrappedLines.add(remainingLine2.subSequence(0, wordBreakLocation))
                remainingLine2 = remainingLine2.subSequence(wordBreakLocation, remainingLine2.length)
            } else {
                wrappedLines.add(remainingLine2)
                remainingLine2 = AnnotatedString("")
            }
        }
    } else {
        // just line1; append
        wrappedLines.add(line1)
    }
    return wrappedLines
}

/**
 * Finds a "nice" spot to break both lines. Does not ignore \[ch] tags. To be used after annotating tab. Might give a line break number that's bigger than string length for one of the strings.
 */
private fun findDoubleLineWordBreakIndex(availableWidthInChars: Int, line1: CharSequence, line2: CharSequence): Int {
    val breakingChars = "‐–〜゠= \t\r\n"  // all the chars that we'll break a line at

    // loop back from availableWidthInChars until we find a suitable breakpoint
    for (i in availableWidthInChars downTo 1) {
        val goodBreakPointForLine1 = line1.length <= i || breakingChars.contains(line1[i])
        val goodBreakPointForLine2 = line2.length <= i || breakingChars.contains(line2[i])
        if (goodBreakPointForLine1 && goodBreakPointForLine2) {
            return i;
        }
    }

    // if no suitable breakpoint was found, break at availableWidthInChars
    return availableWidthInChars
}

private fun getHyperLinks(s: CharSequence): Sequence<MatchResult> {
    val urlPattern = Regex(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
    )

    return urlPattern.findAll(s)
}

//endregion

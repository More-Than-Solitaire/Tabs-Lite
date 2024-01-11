package com.gbros.tabslite.compose.tabview

import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.gbros.tabslite.R
import com.gbros.tabslite.compose.chorddisplay.ChordModalBottomSheet
import com.gbros.tabslite.ui.theme.AppTheme
import com.smarttoolfactory.gesture.detectTransformGestures
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val LOG_NAME = "tabslite.TabText    "

// font size constraints, measured in sp
private const val MIN_FONT_SIZE_SP = 2f
private const val MAX_FONT_SIZE_SP = 36f
private const val FALLBACK_FONT_SIZE_SP = 14f  // fall back to a font size of 14.sp if the system font size can't be read

// To calculate the aspect ratio of a ttf font, run this in python (after pip install fonttools):
// aspect_ratio = ttLib.TTFont(r'path\to\font.ttf')['hmtx']['space'][0] / ttLib.TTFont(r'path\to\font.ttf')['head'].unitsPerEm
private const val ROBOTO_ASPECT_RATIO = 0.60009765625  // the width-to-height ratio of roboto mono Regular.

@OptIn(ExperimentalTextApi::class)
@Composable
fun TabText(modifier: Modifier = Modifier, text: String, onChordClick: (String) -> Unit){
    // default the font size to whatever the user default font size is.  This respects system font settings.
    val defaultFontSize = MaterialTheme.typography.bodyMedium.fontSize
    val defaultFontSizeInSp = if (defaultFontSize.isSp) {
        defaultFontSize.value
    } else if (defaultFontSize.isEm) {
        defaultFontSize.value / LocalDensity.current.density
    } else {
        FALLBACK_FONT_SIZE_SP
    }
    var fontSize by remember {
        mutableFloatStateOf(defaultFontSizeInSp)
    }

    // dynamic variables
    val dynamicText = remember { mutableStateOf(AnnotatedString("")) }
    var measuredWidth by remember { mutableIntStateOf(0) }

    val characterHeightInPixels = with (LocalDensity.current) { fontSize.sp.toPx() }
    val characterWidthInPixels = characterHeightInPixels * ROBOTO_ASPECT_RATIO
    val charsPerLine = floor(measuredWidth / characterWidthInPixels).toUInt()
    dynamicText.value = processTabContent(text, charsPerLine, MaterialTheme.colorScheme)

    val font = remember { FontFamily(Font(R.font.roboto_mono_variable_weight)) }
    val uriHandler = LocalUriHandler.current
    ClickableText(
        text = dynamicText.value,
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
                        fontSize *= zoom
                        // Add checks for maximum and minimum font size
                        fontSize = max(MIN_FONT_SIZE_SP, min(MAX_FONT_SIZE_SP, fontSize))

                    }
                })
            }
            .onGloballyPositioned { layoutResult ->
                measuredWidth = layoutResult.size.width
            }
    ) { clickLocation ->  // handle chord clicks
        val lineEndChars = "\r\n\t"
        val clickedChar = dynamicText.value.getOrNull(clickLocation)
        val clickedOnNewline = clickedChar == null || lineEndChars.contains(clickedChar, true)
        var start = clickLocation
        var end = clickLocation
        if (!clickedOnNewline)
            start--; end++

        dynamicText.value.getStringAnnotations(tag = "chord", start = start, end = end)
            .firstOrNull()?.item?.let { chord -> onChordClick(chord) }

        // handle link clicks
        dynamicText.value.getUrlAnnotations(clickLocation, clickLocation).firstOrNull()?.item?.let {
                urlAnnotation ->
            uriHandler.openUri(urlAnnotation.url)
        }
    }
}

@Composable @Preview
fun TabTextTestCase1() {
    AppTheme {
        val testCase1 = """
        [tab]     [ch]C[/ch]                   [ch]Am[/ch] 
        That David played and it pleased the Lord[/tab]
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
 * Word wrap, style, and annotate a given tab.  Does not add click functionality, but adds an annotation around
 * every chord with tag "chord"
 */
@OptIn(ExperimentalTextApi::class)
private fun processTabContent(content: String, availableWidthInChars: UInt, colorScheme: ColorScheme): AnnotatedString {
    val processedTab = buildAnnotatedString {
        var indexOfEndOfTabBlock = 0
        while (content.indexOf("[tab]", indexOfEndOfTabBlock) != -1) {  // loop through each [tab] line representing lyrics and the chords to go with them
            val indexOfStartOfTabBlock = content.indexOf("[tab]", indexOfEndOfTabBlock)
            // any content before the [tab] block starts (and after the last [/tab] block ended) should be added without custom word-wrapping.  Default wrapping can take care of long lines here.
            appendChordLine(content.subSequence(indexOfEndOfTabBlock, indexOfStartOfTabBlock), this, colorScheme)
            indexOfEndOfTabBlock = content.indexOf("[/tab]", indexOfStartOfTabBlock)+6
            if (indexOfEndOfTabBlock-6 == -1) indexOfEndOfTabBlock = content.length+6

            if (availableWidthInChars != 0u) {  // ignore [tab] block wrapping if availableWidth is 0
                // any content that *is* inside [tab] blocks should be custom word-wrapped (wrapped two lines at a time)
                val tabBlock = content.subSequence(indexOfStartOfTabBlock+5, indexOfEndOfTabBlock-6)
                appendTabBlock(tabBlock, availableWidthInChars, this, colorScheme)
            }
        }
        // append anything after the last tab block
        if (indexOfEndOfTabBlock < content.length) {
            appendChordLine(content.subSequence(indexOfEndOfTabBlock, content.length), this, colorScheme)
        }

        // add active hyperlinks
        val hyperlinks = getHyperLinks(this.toAnnotatedString().text)
        for (hyperlink in hyperlinks) {
            addUrlAnnotation(
                UrlAnnotation(hyperlink.value),
                hyperlink.range.first,
                hyperlink.range.last+1
            )
            addStyle(
                SpanStyle(
                    color = colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ), hyperlink.range.first, hyperlink.range.last+1
            )
        }
    }

    return processedTab
}

/**
 * Processes and wraps the lines for the tab block, then appends to the annotated string builder.
 */
private fun appendTabBlock(tabBlock: CharSequence, availableWidthInChars: UInt, builder: AnnotatedString.Builder, colorScheme: ColorScheme) {
    val lines = tabBlock.split("\n")

    for (i in 0..< lines.count() step 2) {
        val line1 = lines[i]
        val line2: String? = if (i+1 < lines.count()) lines[i+1] else null
        val wrappedLines = wrapLinePair(line1, line2, availableWidthInChars)

        for(wrappedLine in wrappedLines) {
            appendChordLine(wrappedLine, builder, colorScheme)
        }
    }
}

/**
 * Annotate, style, and append a line with chords to the given annotated string builder
 */
@OptIn(ExperimentalTextApi::class)
private fun appendChordLine(line: CharSequence, builder: AnnotatedString.Builder, colorScheme: ColorScheme) {
    val text = line.trimEnd()
    var lastIndex = 0

    while (text.indexOf("[ch]", lastIndex) != -1) {
        val firstIndex = text.indexOf("[ch]", lastIndex)  // index of start of [ch]
        builder.append(text.subSequence(lastIndex, firstIndex))  // append any non-chords

        lastIndex = text.indexOf("[/ch]", firstIndex)+5  // index of end of [/ch]
        if (lastIndex-5 == -1) {
            // couldn't find a closing tag for this chord.  Handle gracefully and log warning
            Log.w(LOG_NAME, "Couldn't find closing [/ch] tag for chord starting at position $firstIndex")
            lastIndex = firstIndex+4  // start the next loop after that [ch] tag
            continue // skip this chord
        }
        val chordName = text.subSequence(firstIndex+4 until lastIndex-5)

        // append an annotated styled chord
        builder
            .withStyle(SpanStyle(
                color = colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                background = colorScheme.primaryContainer
            )
            ) {
                withAnnotation("chord", chordName.toString()) {
                    append(chordName)
                }
            }
    }

    // append any remaining non-chords
    builder.append(text.subSequence(lastIndex until text.length).trimEnd())
    builder.append("\n")
}

/**
 * Take a pair of lines and return a list of lines shorter than the available width, wrapped as a pair.
 */
private fun wrapLinePair(line1: String, line2: String?, availableWidthInChars: UInt): List<String> {
    val wrappedLines = mutableListOf<String>()
    if (line2 != null) {
        var remainingLine1 = line1
        var remainingLine2 = line2

        // append two lines
        while (remainingLine1 != "" || remainingLine2 != "") {
            val wordBreakLocation = findMultipleLineWordBreakIndex(availableWidthInChars, remainingLine1, remainingLine2!!)

            remainingLine1 = if (wordBreakLocation.first < remainingLine1.length) {
                wrappedLines.add(remainingLine1.substring(0, wordBreakLocation.first))
                remainingLine1.substring(wordBreakLocation.first until remainingLine1.length)
            } else {
                wrappedLines.add(remainingLine1.trimEnd())
                ""
            }

            remainingLine2 = if (wordBreakLocation.second < remainingLine2.length) {
                wrappedLines.add(remainingLine2.substring(0, wordBreakLocation.second))
                remainingLine2.substring(wordBreakLocation.second until remainingLine2.length)
            } else {
                wrappedLines.add(remainingLine2.trimEnd())
                ""
            }
        }
    } else {
        // just line1; append
        wrappedLines.add(line1)
    }
    return wrappedLines
}

/**
 * Finds a "nice" spot to break both lines.  Ignores \[ch] and \[/ch] tags.  To be used prior to processing chords.
 */
private fun findMultipleLineWordBreakIndex(availableWidthInChars: UInt, line1: String, line2: String): Pair<Int, Int> {
    // thanks @Andro https://stackoverflow.com/a/11498125
    val breakingChars = "‐–〜゠= \t\r\n"  // all the chars that we'll break a line at
    // Log.d(LOG_NAME, "Find word break index; available width: $availableWidthInChars chars.  Lengths: ${line1.length}/${line2.length}")
    // Log.d(LOG_NAME, "line1: $line1")
    // Log.d(LOG_NAME, "line2: $line2")

    // track fallback line break locations outside of chords (any character but a chord is included)
    var fallbackLineBreak = Pair(0,0)
    var currentlyInChordLine1 = false
    var currentlyInChordLine2 = false

    // start from the start of the line and find each shared word break until the line's too long
    var sharedWordBreakLocation = Pair(0,0)  // track shared location separately to include ignored characters up to breakpoint but not past shared breakpoint
    var line1IgnoredCharacters = 0  // tags (e.g. [ch][/ch]) will be ignored in character counts since they'll be removed in processing.
    var line2IgnoredCharacters = 0
    for (i in 1 ..< availableWidthInChars.toInt()) {
        // loop through each character and note shared word break locations

        // ignore any [ch] or [/ch] tags
        if (line1.length > i+line1IgnoredCharacters) {
            if (line1[(i+line1IgnoredCharacters)] == '[') {
                if (line1.length >= (i+line1IgnoredCharacters+4) && line1.subSequence((i+line1IgnoredCharacters), (i+line1IgnoredCharacters+4)) == "[ch]") {
                    // Log.d(LOG_NAME, "1: ignoring 4 starting at position $i + $line1IgnoredCharacters")
                    line1IgnoredCharacters += 4
                    currentlyInChordLine1 = true
                }
                if (line1.length >= (i+line1IgnoredCharacters+5) && line1.subSequence((i+line1IgnoredCharacters), (i+line1IgnoredCharacters+5)) == "[/ch]") {
                    // Log.d(LOG_NAME, "1: ignoring 5 starting at position $i + $line1IgnoredCharacters")
                    line1IgnoredCharacters += 5
                    currentlyInChordLine1 = false
                }
            }
        }

        if (line2.length > (i+line2IgnoredCharacters)) {
            if (line2[(i+line2IgnoredCharacters)] == '[') {
                if (line2.length >= (i+line2IgnoredCharacters+4) && line2.subSequence((i+line2IgnoredCharacters), (i+line2IgnoredCharacters+4)) == "[ch]") {
                    // Log.d(LOG_NAME, "2: ignoring 4 starting at position $i + $line2IgnoredCharacters")
                    line2IgnoredCharacters += 4
                    currentlyInChordLine2 = true
                }
                if (line2.length >= (i+line2IgnoredCharacters+5) && line2.subSequence((i+line2IgnoredCharacters), (i+line2IgnoredCharacters+5)) == "[/ch]") {
                    // Log.d(LOG_NAME, "2: ignoring 5 starting at position $i + $line2IgnoredCharacters")
                    line2IgnoredCharacters += 5
                    currentlyInChordLine2 = false
                }
            }
        }
        if (!currentlyInChordLine1 && !currentlyInChordLine2)
            fallbackLineBreak = Pair(i+line1IgnoredCharacters, i+line2IgnoredCharacters)  // any character outside of a chord is a fallback linebreak location

        if ((line1.length <= i+line1IgnoredCharacters || breakingChars.contains(line1[i+line1IgnoredCharacters]))
            && (line2.length <= i+line2IgnoredCharacters || breakingChars.contains(line2[(i+line2IgnoredCharacters)]))) {
            sharedWordBreakLocation = Pair(i + line1IgnoredCharacters, i + line2IgnoredCharacters)
            // Log.d(LOG_NAME, "break at $i plus $line1IgnoredCharacters/$line2IgnoredCharacters. Line1 end: ${line1.length <= i+line1IgnoredCharacters}.  Line2 end: ${line2.length <= i+line2IgnoredCharacters}")
        }
    }

    // if no good word break location exists
    if (sharedWordBreakLocation.first < 1 && sharedWordBreakLocation.second < 1) {
        // try to handle nicely by breaking at the last spot outside of a chord
        sharedWordBreakLocation = if (fallbackLineBreak.first > 0 && fallbackLineBreak.second > 0){
            fallbackLineBreak
        } else{
            // welp we tried.  Just force the line break at the end of the line.  [ch][/ch] artifacts will show up.
            Pair(availableWidthInChars.toInt(), availableWidthInChars.toInt())
        }
    }

    // Log.d(LOG_NAME, "Return value: ${sharedWordBreakLocation.first}, ${sharedWordBreakLocation.second}")
    return sharedWordBreakLocation // give the actual character place the user can break at, prior to processing
}

private fun getHyperLinks(s: String): Sequence<MatchResult> {
    val urlPattern = Regex(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
    )

    return urlPattern.findAll(s)
}

//endregion

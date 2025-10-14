package com.gbros.tabslite.viewmodel

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.gbros.tabslite.LoadingState
import com.gbros.tabslite.R
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.data.Preference
import com.gbros.tabslite.data.chord.Chord
import com.gbros.tabslite.data.chord.ChordVariation
import com.gbros.tabslite.data.chord.Instrument
import com.gbros.tabslite.data.playlist.Playlist
import com.gbros.tabslite.data.tab.ITab
import com.gbros.tabslite.data.tab.Tab
import com.gbros.tabslite.data.tab.TabWithDataPlaylistEntry
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.UgApi
import com.gbros.tabslite.utilities.combine
import com.gbros.tabslite.view.tabview.ITabViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.floor

// font size constraints, measured in sp
private const val MIN_FONT_SIZE_SP = 2f
private const val MAX_FONT_SIZE_SP = 36f

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = TabViewModel.TabViewModelFactory::class)
class TabViewModel
@AssistedInject constructor(
    @Assisted private val id: Int,
    @Assisted private val idIsPlaylistEntryId: Boolean,
    @Assisted defaultFontSize: Float,
    @Assisted private val dataAccess: DataAccess,
    @Assisted private val onNavigateToPlaylistEntry: (Int) -> Unit
) : ViewModel(), ITabViewState {
    //#region dependency injection factory

    @AssistedFactory
    interface TabViewModelFactory {
        fun create(id: Int, idIsPlaylistEntryId: Boolean, defaultFontSize: Float, dataAccess: DataAccess, navigateToPlaylistEntryById: (Int) -> Unit): TabViewModel
    }

    //#endregion

    //#region private methods

    /**
     * Handle transposition by a set number of steps. Transposes the tab content, saves the updated
     * transpose value to the playlist if the tab is favorited or in a playlist, and ensures all the
     * new chords are downloaded for quick access
     */
    private fun transpose(numHalfSteps: Int) {
        val currentTab = tab.value
        val currentTranspose = transpose.value
        if (currentTab != null && currentTranspose != null) {

            val newTranspose = currentTranspose + numHalfSteps  // update tab.transpose variable

            // if tab is in a playlist or favorite, save the transposition preference to the db
            CoroutineScope(Dispatchers.IO).launch {
                if (currentTab is TabWithDataPlaylistEntry) {
                    dataAccess.updateEntryTransposition(currentTab.entryId, newTranspose)
                } else if (dataAccess.tabExistsInFavorites(currentTab.tabId)) {
                    dataAccess.updateFavoriteTabTransposition(
                        currentTab.tabId,
                        newTranspose
                    )
                }

                // backup transposition in case this tab isn't in a playlist or favorited. This is only used when the tab.transpose value from the database is null
                nonPlaylistTranspose.postValue(newTranspose)
            }

            // preload all the new chords
            CoroutineScope(Dispatchers.IO).launch {
                fetchAllChords()
            }
        } else {
            Log.e(TAG, "Transpose button clicked while tab was null.")
        }
    }

    private suspend fun fetchAllChords() {
        val chordsUsedInThisTab = tab.value?.getAllChordNames()
        val instrument = chordInstrument.value ?: Instrument.Guitar
        if (!chordsUsedInThisTab.isNullOrEmpty()) {
            Chord.ensureAllChordsDownloaded(chordsUsedInThisTab, instrument, dataAccess)
        }
    }

    /**
     * Autoscroll slider midpoint (default starting speed). Should be between [minDelay] and [maxDelay]
     */
    private val middleDelay: Float = 15f
    /**
     * Autoscroll shortest delay between 1px scrolls (fastest speed)
     */
    private val minDelay: Float = 1f  // fastest speed
    /**
     * Autoscroll longest delay between 1px scrolls (slowest speed)
     */
    private val maxDelay: Float = 75f // slowest speed
    /**
     * Maps the autoscroll slider value to the delay between 1px scrolls for autoscroll
     */
    private val mapAutoscrollSliderToScrollDelay = getValueMapperFunction(minOutput = minDelay, middleOutput = maxDelay - middleDelay, maxOutput = maxDelay)

    /**
     * Creates a quadratic function that maps 0f..1f to [minOutput]..[maxOutput] where 0.5f maps to [middleOutput]
     */
    private fun getValueMapperFunction(minOutput: Float, middleOutput: Float, maxOutput: Float): (x: Float) -> Float {
        val coefficients = findQuadraticCoefficients(y1 = minOutput, y2 = middleOutput, y3 = maxOutput)

        val (a, b, c) = coefficients
        return {
                x: Float ->
            val returnVal = (a * (x * x)) + (b * x) + c
            (maxOutput - returnVal).coerceIn(minimumValue = minOutput, maximumValue = maxOutput)
        }
    }
    private fun findQuadraticCoefficients(y1: Float, y2: Float, y3: Float): Triple<Float, Float, Float> {
        val b = 4 * (y2 - y1) - y3
        val a = (2*y3) - (4 * (y2 - y1)) - (2*y1)
        val c = y1

        return Triple(a, b, c)
    }

    private fun load(forceReload: Boolean = false) {
        _state.postValue(LoadingState.Loading)
        val reloadJob = CoroutineScope(Dispatchers.IO).async {
            var currentTab = tab.value
            if (!tab.isInitialized || currentTab == null) {
                // tab hasn't loaded yet. try to load the tab via the passed ID
                if (!idIsPlaylistEntryId) {
                    currentTab = Tab(id)
                } else {
                    val tabId = dataAccess.getEntryById(id)?.tabId
                    if (tabId == null) {
                        Log.e(TAG, "Couldn't get tab from playlist entry $id")
                        _state.postValue(LoadingState.Error(R.string.message_tab_load_from_playlist_unexpected_error))
                    } else {
                        currentTab = Tab(tabId)
                    }
                }
            }

            currentTab?.load(dataAccess, forceInternetFetch = forceReload)
        }
        reloadJob.invokeOnCompletion { ex ->
            when (ex) {
                null -> {
                    // success
                    _state.postValue(LoadingState.Success)
                }
                is UgApi.NoInternetException -> {
                    Log.i(TAG, "No internet while fetching tab $id (playlistEntryId: $idIsPlaylistEntryId)", ex)
                    _state.postValue(LoadingState.Error(R.string.message_tab_load_no_internet))
                }
                is UgApi.UnavailableForLegalReasonsException -> {
                    Log.i(TAG, "Tab ${tab.value?.songName} (${tab.value?.tabId}) unavailable for legal reasons.")
                    _state.postValue(LoadingState.Error(R.string.message_tab_unavailable_for_legal_reasons))
                }
                is NotFoundException -> {
                    // this shouldn't happen. We only get to this page through the app; it's strange to have a tab ID somewhere else, but not found here.
                    Log.e(TAG, "Tab $id (playlistEntry: $idIsPlaylistEntryId) not found.", ex)
                    if (idIsPlaylistEntryId) {
                        _state.postValue(LoadingState.Error(R.string.message_tab_playlist_entry_not_found))
                    } else {
                        _state.postValue(LoadingState.Error(R.string.message_tab_not_found))
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected error loading tab $id (playlistEntryId: $idIsPlaylistEntryId): ${ex.message}", ex)
                    _state.postValue(LoadingState.Error(R.string.message_tab_load_unexpected_error))
                }
            }
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
                appendWrappedChordLine(content.subSequence(indexOfEndOfTabBlock, indexOfStartOfTabBlock), availableWidthInChars, this, colorScheme)
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
                appendWrappedChordLine(content.subSequence(indexOfEndOfTabBlock, content.length), availableWidthInChars, this, colorScheme)
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
     * Processes and wraps the lines for the chord block, then appends to the annotated string builder.
     */
    private fun appendWrappedChordLine(line: CharSequence, availableWidthInChars: UInt, builder: AnnotatedString.Builder, colorScheme: ColorScheme) {
        val wrappedLines = wrapLine(line.toString(), availableWidthInChars)
        for (wrappedLine in wrappedLines) {
            appendChordLine(wrappedLine, builder, colorScheme)
        }
    }

    /**
     * Annotate, style, and append a line with chords to the given annotated string builder
     */
    @OptIn(ExperimentalTextApi::class)
    private fun appendChordLine(line: CharSequence, builder: AnnotatedString.Builder, colorScheme: ColorScheme?) {
        val text = line.trimEnd()
        var lastIndex = 0

        while (text.indexOf("[ch]", lastIndex) != -1) {
            val firstIndex = text.indexOf("[ch]", lastIndex)  // index of start of [ch]
            builder.append(text.subSequence(lastIndex, firstIndex))  // append any non-chords

            lastIndex = text.indexOf("[/ch]", firstIndex)+5  // index of end of [/ch]
            if (lastIndex-5 == -1) {
                // couldn't find a closing tag for this chord.  Handle gracefully and log warning
                Log.w(TAG, "Couldn't find closing [/ch] tag for chord starting at position $firstIndex for tab ${tab.value?.tabId}")
                lastIndex = firstIndex+4  // start the next loop after that [ch] tag
                continue // skip this chord
            }
            val chordName = text.subSequence(firstIndex+4 until lastIndex-5)

            // append an annotated styled chord
            if (colorScheme == null) {
                builder.withAnnotation("chord", chordName.toString()) {
                    append(chordName)
                }
            }
            else {
                builder
                    .withStyle(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            SpanStyle(
                                // Only Android 8 and up support variable weight fonts
                                color = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                background = colorScheme.primaryContainer
                            )
                        } else {
                            SpanStyle(
                                color = colorScheme.onPrimaryContainer,
                                fontFamily = FontFamily(Font(R.font.roboto_mono_bold)),
                                background = colorScheme.primaryContainer
                            )
                        }
                    ) {
                        withAnnotation("chord", chordName.toString()) {
                            append(chordName)
                        }
                    }
            }
        }

        // append any remaining non-chords
        builder.append(text.subSequence(lastIndex until text.length).trimEnd())
        builder.append("\n")
    }

    /**
     * Take a line and return a list of lines shorter than the available width
     */
    private fun wrapLine(line: String, availableWidthInChars: UInt): List<String> {
        val wrappedLines = mutableListOf<String>()
        var remainingLine = line

        while (remainingLine != "") {
            val wordBreakLocation = findSingleLineWordBreakIndex(availableWidthInChars, remainingLine)
            remainingLine = if (wordBreakLocation < remainingLine.length) {
                wrappedLines.add(remainingLine.substring(0, wordBreakLocation))
                remainingLine.substring(wordBreakLocation until remainingLine.length)
            } else {
                wrappedLines.add(remainingLine.trimEnd())
                ""
            }
        }
        return wrappedLines
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
     * Finds a "nice" spot to break a single line.  Ignores \[ch] and \[/ch] tags.  To be used prior to processing chords.
     *
     * @param line The line to break.
     * @param availableWidthInChars The available width in characters.
     * @return The index of the character to break at.
     */
    private fun findSingleLineWordBreakIndex(availableWidthInChars: UInt, line: String): Int {
        // thanks @Andro https://stackoverflow.com/a/11498125
        val breakingChars = "‐–〜゠= \t\r\n"  // all the chars that we'll break a line at

        // track fallback line break locations outside of chords (any character but a chord is included)
        var fallbackLineBreak = 0
        var currentlyInChord = false

        // start from the start of the line and find each nice word break until the line's too long
        var wordBreakLocation = 0  // track nice location separately to include ignored characters up to breakpoint but not past shared breakpoint
        var numIgnoredCharacters = 0  // tags (e.g. [ch][/ch]) will be ignored in character counts since they'll be removed in processing.
        for (i in 1 ..< availableWidthInChars.toInt()) {
            // loop through each character and note shared word break locations
            if (line.length <= i+numIgnoredCharacters) {
                break
            }

            // ignore any [ch] or [/ch] tags
            if (line.length > i+numIgnoredCharacters) {
                if (line[(i+numIgnoredCharacters)] == '[') {
                    if (line.length >= (i+numIgnoredCharacters+4) && line.subSequence((i+numIgnoredCharacters), (i+numIgnoredCharacters+4)) == "[ch]") {
                        numIgnoredCharacters += 4
                        currentlyInChord = true
                    }
                    if (line.length >= (i+numIgnoredCharacters+5) && line.subSequence((i+numIgnoredCharacters), (i+numIgnoredCharacters+5)) == "[/ch]") {
                        numIgnoredCharacters += 5
                        currentlyInChord = false
                    }
                }
            }

            if (!currentlyInChord)
                fallbackLineBreak = i+numIgnoredCharacters  // any character outside of a chord is a fallback linebreak location

            if ((line.length > i+numIgnoredCharacters && breakingChars.contains(line[i+numIgnoredCharacters]))) {
                wordBreakLocation =i + numIgnoredCharacters
            }
        }

        // if no good word break location exists
        if (wordBreakLocation < 1) {
            // try to handle nicely by breaking at the last spot outside of a chord
            wordBreakLocation = if (fallbackLineBreak > 0) {
                fallbackLineBreak
            } else {
                // welp we tried.  Just force the line break at the end of the line.  [ch][/ch] artifacts will show up.
                availableWidthInChars.toInt()
            }
        }

        return wordBreakLocation // give the actual character place the user can break at, prior to processing
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
            "(?:^|\\W)((ht|f)tp(s?)://|www\\.)"
                    + "(([\\w\\-]+\\.)+([\\w\\-.~]+/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
            setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )

        return urlPattern.findAll(s)
    }

    /**
     * Create a PDF document from the current tab
     */
    private fun createPdf(): PdfDocument {
        val currentColors = currentTheme.value ?: return PdfDocument()
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size

        // Margins
        val leftMargin = 40f
        val rightMargin = 40f
        val topMargin = 50f
        val bottomMargin = 50f

        var currentPageNumber = 1

        fun startNewPage(pageNumber: Int): PdfDocument.Page {
            val newPageInfo = PdfDocument.PageInfo.Builder(pageInfo.pageWidth, pageInfo.pageHeight, pageNumber).create()
            return doc.startPage(newPageInfo)
        }

        var page = startNewPage(currentPageNumber)
        var canvas = page.canvas
        var currentY = topMargin

        // draw title
        val titlePaint = android.graphics.Paint().apply {
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textSize = 18f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val titleX = pageInfo.pageWidth / 2f // center the title on the page
        val title = "${songName.value} - ${artist.value}"
        canvas.drawText(title, titleX, currentY, titlePaint)
        currentY += titlePaint.fontSpacing * 2 // Add some vertical space after the title

        // wrap content
        val contentPaint = android.graphics.Paint().apply {
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
            textSize = 12f
        }
        val availableWidthInPt = pageInfo.pageWidth - leftMargin - rightMargin
        val ptPerChar = contentPaint.measureText("A")
        val availableWidthInChars = (availableWidthInPt / ptPerChar).toUInt()
        val wrappedContent: AnnotatedString = processTabContent(unformattedContent.value ?: "", availableWidthInChars, currentColors)

        // draw content
        val chordTextPaint = android.graphics.Paint().apply {
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            textSize = 12f
            color = android.graphics.Color.BLACK // Chord text color is black
        }
        val chordBackgroundPaint = android.graphics.Paint().apply {
            color = "#FFDEA0".toColorInt() // Use theme color for highlight, or yellow as a fallback
            style = android.graphics.Paint.Style.FILL
        }
        val lineRegex = Regex(".*\\R?") // Regex to match a full line including its newline characters
        lineRegex.findAll(wrappedContent.text).forEach { lineMatchResult ->
            val line = lineMatchResult.value.trimEnd() // The actual line content without trailing newline
            val lineStartOffset = lineMatchResult.range.first

            if (currentY + contentPaint.fontSpacing > pageInfo.pageHeight - bottomMargin) {
                // Finish current page and start a new one
                doc.finishPage(page)
                currentPageNumber++
                page = startNewPage(currentPageNumber)
                canvas = page.canvas
                currentY = topMargin
            }

            var currentX = leftMargin
            val lineEndOffset = lineStartOffset + line.length

            // Get chord annotations for the current line
            val lineAnnotations = wrappedContent.getStringAnnotations("chord", lineStartOffset, lineEndOffset)

            var lastCharIndexInLine = 0
            for (annotation in lineAnnotations) {
                // Calculate the start and end of the annotation relative to the current line
                val annotationStartInLine = annotation.start - lineStartOffset
                val annotationEndInLine = annotation.end - lineStartOffset

                // Draw text before the chord
                if (annotationStartInLine > lastCharIndexInLine) {
                    val textBefore = line.substring(lastCharIndexInLine, annotationStartInLine)
                    canvas.drawText(textBefore, currentX, currentY, contentPaint)
                    currentX += contentPaint.measureText(textBefore)
                }

                // Draw the chord using the annotation's item
                val chordText = annotation.item
                val chordWidth = contentPaint.measureText(chordText) // Use contentPaint to measure for correct monospaced width

                // Draw the highlight background
                val backgroundRect = android.graphics.RectF(currentX - chordTextPaint.letterSpacing, currentY - chordTextPaint.textSize, currentX + chordWidth + chordTextPaint.letterSpacing, currentY + chordTextPaint.descent())
                canvas.drawRect(backgroundRect, chordBackgroundPaint)

                // Draw the chord text over the highlight
                canvas.drawText(chordText, currentX, currentY, chordTextPaint)
                currentX += chordWidth // Advance currentX by the width of the chord

                lastCharIndexInLine = annotationEndInLine
            }

            // Draw any remaining text after the last annotation
            if (lastCharIndexInLine < line.length) {
                val remainingText = line.substring(lastCharIndexInLine)
                canvas.drawText(remainingText, currentX, currentY, contentPaint)
            }

            currentY += contentPaint.fontSpacing
        }

        doc.finishPage(page)
        return doc
    }

    /**
     * Calculates the number of characters that can fit in the screen.
     *
     * @param availableWidthInPx The width of the screen in pixels
     * @param fontSizeSp The font size in sp
     * @param currentDensity The current density of the screen
     *
     * @return The number of characters that can fit in the screen
     */
    private fun getAvailableWidthInChars(availableWidthInPx: Int, fontSizeSp: Float, currentDensity: Density):UInt {
        val characterHeightInPixels = with (currentDensity) { fontSizeSp.sp.toPx() }
        val characterWidthInPixels = characterHeightInPixels * ROBOTO_ASPECT_RATIO
        val charsPerLine = floor(availableWidthInPx / characterWidthInPixels).toUInt()
        return charsPerLine
    }

//endregion

//#endregion

    //#region private data

    private val tab: LiveData<out ITab?> = if (idIsPlaylistEntryId) dataAccess.getTabFromPlaylistEntryId(id) else dataAccess.getTab(id)

    /**
     * The chord name to look up in the database and display
     */
    private var currentChordToDisplay: MutableLiveData<String> = MutableLiveData("")

    override val chordInstrument: LiveData<Instrument> = dataAccess.getLivePreference(Preference.INSTRUMENT).map { p -> if (p != null) Instrument.valueOf(p.value) else Instrument.Guitar }

    private val currentChordInstrumentCombo: LiveData<Pair<String?, Instrument?>> =
        currentChordToDisplay.combine(chordInstrument) { chord, instrument ->
            Pair(chord, instrument)
        } as MutableLiveData<Pair<String?, Instrument?>>

    /**
     * To calculate the aspect ratio of a ttf font, run this in python (after pip install fonttools):
     * aspect_ratio = ttLib.TTFont(r'path\to\font.ttf')['hmtx']['space'][0] / ttLib.TTFont(r'path\to\font.ttf')['head'].unitsPerEm
     */
    private val ROBOTO_ASPECT_RATIO = 0.60009765625  // the empirical width-to-height ratio of roboto mono Regular.

    private val screenDensity: MutableLiveData<Density> = MutableLiveData()

    /**
     * The last measured screen width in pixels, used for calculating how many characters can fit in the screen for custom word wrapping
     */
    private val screenWidthInPx: MutableLiveData<Int> = MutableLiveData()

    override val fontSizeSp: MutableLiveData<Float> = MutableLiveData(defaultFontSize)

    private val availableWidthInChars: LiveData<UInt> = screenWidthInPx.combine(fontSizeSp, screenDensity) { currentWidthPx, currentFontSizeSp, currentDensity ->
        if (currentWidthPx == null || currentFontSizeSp == null || currentDensity == null) {
            return@combine 0u
        }
        return@combine getAvailableWidthInChars(currentWidthPx, currentFontSizeSp, currentDensity)
    }

    private val currentTheme: MutableLiveData<ColorScheme> = MutableLiveData()

    override val allPlaylists: LiveData<List<Playlist>> = dataAccess.getLivePlaylists()

    private val _addToPlaylistDialogSelectedPlaylist: MutableLiveData<Playlist> = MutableLiveData()
    private var addToPlaylistDialogSelectedPlaylist: LiveData<Playlist?> = _addToPlaylistDialogSelectedPlaylist.combine(allPlaylists) { currentSelection, playlistList ->
        currentSelection  // use the current selection if there is one
            ?: if (!playlistList.isNullOrEmpty()) {
                playlistList.first()  // default to the first element in the list of playlists if the list of playlists is populated
            } else {
                null  // fallback to a null selection to let the UI handle the nothing-is-selected case
            }
    }

    //#endregion

    //#region view state

    override val artistId: LiveData<Int> = tab.map { t -> t.artistId }

    override val useFlats: LiveData<Boolean> = dataAccess.getLivePreference(Preference.USE_FLATS).map { p -> p?.value?.toBoolean() == true }

    override val songName: LiveData<String> = tab.map { t -> t?.songName ?: "" }

    override val version: LiveData<Int> = tab.map { t -> t.version }

    override val songVersions: LiveData<List<ITab>> = tab.switchMap { t -> dataAccess.getTabsBySongId(t.songId).map { t -> t } }

    override val isFavorite: LiveData<Boolean> = if (idIsPlaylistEntryId) dataAccess.playlistEntryExistsInFavorites(id) else dataAccess.tabExistsInFavoritesLive(id)

    /**
     * Whether to display the playlist navigation bar
     */
    override val isPlaylistEntry: Boolean
        get() {
            val t = tab.value
            return t is TabWithDataPlaylistEntry && t.playlistId > 0  // return false if this is the favorites or popular tabs playlists (<=0)
        }

    override val playlistTitle: LiveData<String> = tab.map { t ->
        if (t is TabWithDataPlaylistEntry && t.playlistTitle != null) t.playlistTitle!! else ""
    }

    override val playlistNextSongButtonEnabled = tab.map { t -> t is TabWithDataPlaylistEntry && t.nextEntryId != null }

    override val playlistPreviousSongButtonEnabled = tab.map { t -> t is TabWithDataPlaylistEntry && t.prevEntryId != null }

    override val difficulty: LiveData<String> = tab.map { t -> t?.difficulty ?: "" }

    override val tuning: LiveData<String> = tab.map { t -> t?.tuning ?: "" }

    override fun getCapoText(context: Context): LiveData<String> = tab.map { t -> t?.getCapoText(context) ?: "" }

    override val key: LiveData<String> = tab.map { t -> t?.tonalityName ?: "" }
    
    override val author: LiveData<String> = tab.map { t -> t?.contributorUserName ?: "" }

    override val artist: LiveData<String> = tab.map { t -> t?.artistName ?: "" }

    /**
     * Fallback method of saving transposition while this tab is open, if this tab is not in a playlist or favorites. This is only used when the tab.transpose value from the database is null
     */
    private val nonPlaylistTranspose: MutableLiveData<Int> = MutableLiveData(0)
    override val transpose: LiveData<Int> = tab.combine(nonPlaylistTranspose) { t, nonPlaylistTranspose ->
        t?.transpose ?: nonPlaylistTranspose ?: 0
    }

    private val unformattedContent: LiveData<String> = tab.combine(transpose, useFlats) { t, tr, f ->
        val currentDbContent = t?.content ?: ""
        val currentTranspose = tr ?: 0
        val useFlats = f == true

        val chordPattern = Regex("\\[ch](.*?)\\[/ch]")
        val transposedContent = chordPattern.replace(currentDbContent) {
            val chord = it.groupValues[1]
            "[ch]" + Chord.transposeChord(chord, currentTranspose, useFlats) + "[/ch]"
        }

        return@combine transposedContent
    }

    override val plainTextContent: LiveData<String> = unformattedContent.map { txt ->
        txt.replace("[tab]", "").replace("[/tab]", "").replace("[ch]", "").replace("[/ch]", "")
    }

    override val content: LiveData<AnnotatedString> = unformattedContent.combine(availableWidthInChars, currentTheme) { unformatted, availableWidth, theme ->
        if (unformatted != null && availableWidth != null && availableWidth > 0u && theme != null) {
            processTabContent(unformatted, availableWidth, theme)
        } else {
            Log.d(TAG, "No content yet")
            AnnotatedString("")
        }
    }

    private val _state: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.Loading)
    override val state: LiveData<LoadingState> = content.combine(_state) { c, _ ->
        // check for an update in status if we're still in Loading (or Failure) state before returning
        if (c != null) {
            if (_state.value != LoadingState.Success && c.isNotEmpty()) {
                _state.postValue(LoadingState.Success)  // content successfully loaded and formatted
            }
        }
        _state.value ?: LoadingState.Loading
    }

    /**
     * Whether we're currently autoscrolling
     */
    private val _autoscrollPaused: MutableLiveData<Boolean> = MutableLiveData(true)
    override val autoscrollPaused: LiveData<Boolean> = _autoscrollPaused

    private var _autoscrollSpeedSliderPosition: MutableLiveData<Float> = MutableLiveData(.5f)
    override val autoScrollSpeedSliderPosition: LiveData<Float> = _autoscrollSpeedSliderPosition

    /**
     * Whether to display the chord fingerings for the current chord
     */
    private val _chordDetailsActive: MutableLiveData<Boolean> = MutableLiveData(false)
    override val chordDetailsActive: LiveData<Boolean> = _chordDetailsActive

    /**
     * The title for the chord details section (usually the name of the active chord being displayed)
     */
    override val chordDetailsTitle: LiveData<String> = currentChordToDisplay

    /**
     * A list of chord fingerings to be displayed in the chord details section
     */
    override val chordDetailsVariations: LiveData<List<ChordVariation>> = currentChordInstrumentCombo.switchMap { (chord, instrument) ->
        if (chord == null || instrument == null) {
            MutableLiveData(listOf())
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                // double check that the chord is downloaded
                Chord.getChord(chord, instrument, dataAccess)
            }
            dataAccess.chordVariations(chord, instrument)
        }
    }

    /**
     * The state of the chord details section (loading until the details have been fetched successfully)
     */
    private val _chordDetailsState: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.Loading)
    override val chordDetailsState: LiveData<LoadingState> = chordDetailsVariations.switchMap { c ->
        // check for an update in status if we're still in Loading (or Failure) state before returning
        if (_chordDetailsState.value != LoadingState.Success && c.isNotEmpty()) {
            _chordDetailsState.postValue(LoadingState.Success)  // chords successfully loaded
        }
        _chordDetailsState
    }

    override val autoscrollDelay: LiveData<Float> = autoScrollSpeedSliderPosition.map { sliderPosition -> mapAutoscrollSliderToScrollDelay(sliderPosition) }

    override val shareUrl: LiveData<String> = tab.map { t -> "https://tabslite.com/tab/${t?.tabId}" }

    override fun getShareTitle(context: Context): LiveData<String> = tab.map { t -> t?.let { String.format(format = context.getString(R.string.tab_title), t.songName, it.artistName) } ?: "" }

    override val addToPlaylistDialogSelectedPlaylistTitle: LiveData<String?> = addToPlaylistDialogSelectedPlaylist.map { p -> p?.title }

    override val addToPlaylistDialogConfirmButtonEnabled: LiveData<Boolean> = addToPlaylistDialogSelectedPlaylist.map { p -> p != null }

    //#endregion

    //#region event handling

    fun onExportToPdfClick(exportFile: Uri, contentResolver: ContentResolver) {
        val exportJob = CoroutineScope(Dispatchers.IO).async {
            val pdfDoc = createPdf()
            contentResolver.openOutputStream(exportFile).use { outputStream ->
                pdfDoc.writeTo(outputStream)
                pdfDoc.close()
                outputStream?.flush()
            }
        }

        exportJob.invokeOnCompletion { ex ->
            if (ex != null) {
                Log.e(TAG, "Unexpected error during playlist export: ${ex.message}")
            }
        }
    }

    fun onPlaylistNextSongClick() {
        val currentTab = tab.value
        if (currentTab != null && currentTab is TabWithDataPlaylistEntry) {
            val entryIdToNavigateTo = currentTab.nextEntryId
            if (entryIdToNavigateTo != null) {
                onNavigateToPlaylistEntry(entryIdToNavigateTo)
            } else {
                Log.w(TAG, "Playlist next song click event triggered while next entry id is null")
            }
        } else {
            Log.w(TAG, "Playlist next song clicked while tab (id: $id, playlist: $idIsPlaylistEntryId) is null or not playlist entry: ${tab.value?.toString()}")
        }
    }

    fun onPlaylistPreviousSongClick() {
        val currentTab = tab.value
        if (currentTab != null && currentTab is TabWithDataPlaylistEntry) {
            val entryIdToNavigateTo = currentTab.prevEntryId
            if (entryIdToNavigateTo != null) {
                onNavigateToPlaylistEntry(entryIdToNavigateTo)
            } else {
                Log.w(TAG, "Playlist previous song click event triggered while previous entry id is null")
            }
        } else {
            Log.w(TAG, "Playlist previous song clicked while tab (id: $id, playlist: $idIsPlaylistEntryId) is null or not playlist entry: ${tab.value?.toString()}")
        }
    }

    fun onTransposeResetClick() {
        transpose(-(transpose.value ?: 0))
    }

    fun onTransposeUpClick() {
        transpose(1)
    }

    fun onTransposeDownClick() {
        transpose(-1)
    }

    /**
     * Callback to be called when the user triggers a tab refresh. Tries to retrieve a tab ID if the
     * initial load failed, and re-fetches the tab from the internet
     */
    fun onReload() {
        load(true)
    }

    /**
     * Callback for when a chord is clicked, to display the chord fingering diagram
     */
    @OptIn(ExperimentalTextApi::class)
    fun onContentClick(clickLocation: Int, uriHandler: UriHandler, clipboardManager: ClipboardManager) {
        val lineEndChars = "\r\n\t"
        val clickedChar = content.value?.getOrNull(clickLocation)
        val clickedOnNewline = clickedChar == null || lineEndChars.contains(clickedChar, true)
        var start = clickLocation
        var end = clickLocation
        if (!clickedOnNewline)
            start--; end++

        content.value?.getStringAnnotations(tag = "chord", start = start, end = end)
            ?.firstOrNull()?.item?.let { chord ->
                _chordDetailsState.postValue(LoadingState.Loading)
                _chordDetailsActive.postValue(true)
                currentChordToDisplay.postValue(chord)
            }

        // handle link clicks
        content.value?.getUrlAnnotations(clickLocation, clickLocation)?.firstOrNull()?.item?.let {
                urlAnnotation ->
            try {
                uriHandler.openUri(urlAnnotation.url.trim())
            } catch (ex: ActivityNotFoundException) {
                Log.i(TAG, "Couldn't launch URL, copying to clipboard instead")
                clipboardManager.nativeClipboard.setPrimaryClip(ClipData.newPlainText(urlAnnotation.url.trim(), urlAnnotation.url.trim()))
            }
        }
    }

    fun onChordDetailsDismiss() {
        _chordDetailsActive.postValue(false)
    }

    fun onAutoscrollSliderValueChange(newValue: Float) {
         _autoscrollSpeedSliderPosition.postValue(newValue)
    }

    /**
     * Callback for when the user lifts their finger after adjusting the autoscroll speed. Save the new
     * autoscroll slider position to the database here rather than in [onAutoscrollSliderValueChange]
     * to prevent too many database calls
     */
    fun onAutoscrollSliderValueChangeFinished() {
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.upsert(Preference(Preference.AUTOSCROLL_DELAY, autoScrollSpeedSliderPosition.value.toString()))
        }
    }

    /**
     * Callback for when the Play/Pause button is pressed, to enabled or disable autoscroll
     */
    fun onAutoscrollButtonClick() {
        _autoscrollPaused.postValue(autoscrollPaused.value == false)  // toggle the pause state of autoscroll
    }

    fun onFavoriteButtonClick() {
        CoroutineScope(Dispatchers.IO).launch {
            if(isFavorite.value == true) {
                tab.value?.let { dataAccess.deleteTabFromFavorites(it.tabId) }
            } else {
                val transpose = transpose.value ?: 0
                tab.value?.let { dataAccess.insertToFavorites(it.tabId, transpose) }
            }
        }
    }

    fun onAddPlaylistDialogPlaylistSelected(selection: Playlist) {
        _addToPlaylistDialogSelectedPlaylist.postValue(selection)
    }

    /**
     * Callback for when the AddToPlaylist dialog Confirm button is pressed. Add the current tab
     * to the selected playlist
     */
    fun onAddToPlaylist() {
        val selectedPlaylist = addToPlaylistDialogSelectedPlaylist.value
        val currentTab = tab.value
        val currentTranspose = transpose.value

        if (selectedPlaylist != null && currentTab != null && currentTranspose != null) {
            CoroutineScope(Dispatchers.IO).launch {
                dataAccess.appendToPlaylist(
                    selectedPlaylist.playlistId,
                    currentTab.tabId,
                    currentTranspose
                )
            }
        } else {
            Log.e(TAG, "Couldn't add the requested tab $currentTab to playlist ${selectedPlaylist?.playlistId} at transpose $currentTranspose. All of the values need to be non-null.")
        }
    }

    fun onCreatePlaylist(title: String, description: String) {
        val playlistToSave = Playlist(playlistId = 0, userCreated = true, title = title, description = description, dateCreated = System.currentTimeMillis(), dateModified = System.currentTimeMillis())
        CoroutineScope(Dispatchers.IO).launch {
            val newPlaylistId = dataAccess.upsert(playlistToSave)
            val newPlaylist = dataAccess.getPlaylist(newPlaylistId.toInt())
            _addToPlaylistDialogSelectedPlaylist.postValue(newPlaylist)
        }
    }

    /**
     * Save the current screen details to enable custom wrapping
     */
    fun onScreenMeasured(screenWidth: Int, localDensity: Density, colorScheme: ColorScheme) {
        if (screenDensity.value != localDensity){
            screenDensity.value = localDensity
        }

        if (screenWidthInPx.value != screenWidth) {
            screenWidthInPx.value = screenWidth
        }

        if (currentTheme.value != colorScheme) {
            currentTheme.value = colorScheme
        }
    }

    /**
     * Handle user zooming in and out
     */
    fun onZoom(zoomFactor: Float) {
        val currentFontSize = fontSizeSp.value
        if (currentFontSize != null) {
            fontSizeSp.value =
                currentFontSize.times(zoomFactor).coerceIn(  // Add checks for maximum and minimum font size
                    MIN_FONT_SIZE_SP,
                    MAX_FONT_SIZE_SP
                )
        }
    }

    /**
     * Handle user selecting a different instrument to display tabs for
     */
    fun onInstrumentSelected(instrument: Instrument) {
        _chordDetailsState.value = LoadingState.Loading
        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.upsert(Preference(Preference.INSTRUMENT, instrument.name))
            fetchAllChords()
            _chordDetailsState.postValue(LoadingState.Success)
        }
    }

    /**
     * Handle user toggling between flats and sharps by converting all chords to use flats or sharps
     * depending on the passed parameter
     */
    fun onUseFlatsToggled(useFlats: Boolean) {
        _chordDetailsState.value = LoadingState.Loading
        // use the transpose function to force the correct flat/sharp
        currentChordToDisplay.value = Chord.transposeChord(currentChordToDisplay.value.toString(), 0, useFlats)

        CoroutineScope(Dispatchers.IO).launch {
            dataAccess.upsert(Preference(Preference.USE_FLATS, useFlats.toString()))
            fetchAllChords()
        }
    }

    //#endregion

    //#region init

    init {
        // load the tab content from the database (or the internet if no cached database value)
        val scope = CoroutineScope(Dispatchers.IO)
        load()

        // set our initial autoscroll slider position to the user preference value
        val autoscrollPreferenceJob = scope.async {
            return@async dataAccess.getPreferenceValue(Preference.AUTOSCROLL_DELAY)?.toFloat()
        }
        autoscrollPreferenceJob.invokeOnCompletion { err ->
            if (err != null) {
                Log.e(TAG, "Couldn't load autoscroll user preference: ${err.message}", err)
                _autoscrollSpeedSliderPosition.postValue(0.5f) // have a fallback value in case of exception or database errors
            } else {
                val result = autoscrollPreferenceJob.getCompleted()
                _autoscrollSpeedSliderPosition.postValue(result ?: 0.5f)
            }
        }

        // preload all chords for fast access on click
        scope.launch {
            fetchAllChords()
        }
    }

    //#endregion
}
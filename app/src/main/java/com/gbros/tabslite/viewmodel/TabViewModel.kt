package com.gbros.tabslite.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
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
import com.gbros.tabslite.utilities.BackendConnection
import com.gbros.tabslite.utilities.combine
import com.gbros.tabslite.view.tabview.ITabViewState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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
private const val MIN_FONT_SIZE_SP = 6f
private const val MAX_FONT_SIZE_SP = 42f

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = TabViewModel.TabViewModelFactory::class)
class TabViewModel
@AssistedInject constructor(
    @Assisted private val providedTabId: String? = null,
    @Assisted private val entryId: Int? = null,
    @Assisted defaultFontSize: Float,
    @Assisted private val dataAccess: DataAccess,
    @Assisted private val urlHandler: UriHandler,
    @Assisted private val onNavigateToPlaylistEntry: (Int) -> Unit
) : ViewModel(), ITabViewState {

    val db = Firebase.firestore

    //#region dependency injection factory

    @AssistedFactory
    interface TabViewModelFactory {
        fun create(tabId: String? = null, entryId: Int? = null, defaultFontSize: Float, dataAccess: DataAccess, urlHandler: UriHandler, navigateToPlaylistEntryById: (Int) -> Unit): TabViewModel
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

            val newTranspose = (currentTranspose + numHalfSteps) % 12  // update tab.transpose variable

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

                // backup transposition in case this tab's not in a playlist or favorited. This is only used when the tab.transpose value from the database is null
                nonPlaylistTranspose.postValue(newTranspose)
            }
        } else {
            Log.e(TAG, "Transpose button clicked while tab was null.")
        }
    }

    //#region autoscroll

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

    //#endregion autoscroll

    private fun load(forceReload: Boolean = false) {
        _state.postValue(LoadingState.Loading)
        val reloadJob = CoroutineScope(Dispatchers.IO).async {
            var currentTab = tab.value
            if (!tab.isInitialized || currentTab == null) {
                // tab hasn't loaded yet. try to load the tab via the passed ID
                if (entryId != null) {
                    val tabId = dataAccess.getEntryById(entryId)?.tabId
                    if (tabId == null) {
                        Log.e(TAG, "Couldn't get tab from playlist entry ${this@TabViewModel.providedTabId}")
                        _state.postValue(LoadingState.Error(R.string.message_tab_load_from_playlist_unexpected_error))
                    } else {
                        currentTab = Tab(tabId)
                    }
                }
                else if (providedTabId != null)
                    currentTab = Tab(providedTabId)
                } else {
                    Log.e(TAG, "Couldn't load tab in TabViewModel. Both tabId and entryId were null.")
                    _state.postValue(LoadingState.Error(R.string.message_tab_load_unexpected_error, "tabId and entryId both null"))
                }

            currentTab?.load(dataAccess = dataAccess, forceInternetFetch = forceReload)
        }
        reloadJob.invokeOnCompletion { ex ->
            when (ex) {
                null -> {
                    // success
                    _state.postValue(LoadingState.Success)
                }
                is BackendConnection.NoInternetException -> {
                    Log.i(TAG, "No internet while fetching tab $providedTabId / playlist entry $entryId", ex)
                    _state.postValue(LoadingState.Error(R.string.message_tab_load_no_internet))
                }
                is BackendConnection.UnavailableForLegalReasonsException -> {
                    Log.i(TAG, "Tab ${tab.value?.songName} (${tab.value?.tabId}) unavailable for legal reasons.")
                    _state.postValue(LoadingState.Error(R.string.message_tab_unavailable_for_legal_reasons))
                }
                is NotFoundException -> {
                    // this shouldn't happen. We only get to this page through the app; it's strange to have a tab ID somewhere else, but not found here.
                    Log.e(TAG, "Tab $providedTabId / playlist entry $entryId not found.", ex)
                    if (entryId != null) {
                        _state.postValue(LoadingState.Error(R.string.message_tab_playlist_entry_not_found))
                    } else {
                        _state.postValue(LoadingState.Error(R.string.message_tab_not_found))
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected error loading tab $providedTabId / playlist entry $entryId: ${ex.message}", ex)
                    _state.postValue(LoadingState.Error(R.string.message_tab_load_unexpected_error, ex.message.toString()))
                }
            }
        }
    }

    /**
     * Create a PDF document from the current tab
     */
    private fun createPdf(): PdfDocument {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size

        // Margins
        val leftMargin = 40f
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
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.BLACK // Explicitly set text color
        }
        val titleX = pageInfo.pageWidth / 2f // center the title on the page
        val title = "${songName.value} - ${artist.value}"
        canvas.drawText(title, titleX, currentY, titlePaint)
        currentY += titlePaint.fontSpacing * 2 // Add some vertical space after the title

        // setup paints for content
        val contentPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
        val chordTextPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = android.graphics.Color.BLACK
        }

        val annotatedContent: AnnotatedString = TabContent(urlHandler::openUri, transposedContentString.value ?: "").content
        val lineRegex = Regex(".*\\R?") // Regex to match a full line including its newline characters

        lineRegex.findAll(annotatedContent.text).forEach { lineMatchResult ->
            val line = lineMatchResult.value.trimEnd() // The actual line content without trailing newline
            val lineStartOffset = lineMatchResult.range.first

            // Check if there's enough space for both the lyrics and the chords above them, otherwise start a new page
            val requiredSpace = contentPaint.fontSpacing + chordTextPaint.fontSpacing
            if (currentY + requiredSpace > pageInfo.pageHeight - bottomMargin) {
                // Finish current page and start a new one
                doc.finishPage(page)
                page = startNewPage(++currentPageNumber)
                canvas = page.canvas
                currentY = topMargin
            }

            var currentX = leftMargin
            val lineEndOffset = lineStartOffset + line.length

            // Get all chord annotations for the current line
            val lineAnnotations = annotatedContent.getStringAnnotations("chord", lineStartOffset, lineEndOffset)

            var lastCharIndexInLine = 0
            for (annotation in lineAnnotations) {
                val annotationStartInLine = annotation.start - lineStartOffset
                val annotationEndInLine = annotation.end - lineStartOffset

                // Draw text before this annotation
                if (annotationStartInLine > lastCharIndexInLine) {
                    val textBefore = line.substring(lastCharIndexInLine, annotationStartInLine)
                    canvas.drawText(textBefore, currentX, currentY, contentPaint)
                    currentX += contentPaint.measureText(textBefore)
                }

                // The character in the main text that the chord is attached to
                val annotatedChar = line.substring(annotationStartInLine, annotationEndInLine)
                val annotatedCharWidth = contentPaint.measureText(annotatedChar)

                // Extract chord name and check if it's an inline chord
                var chordText = annotation.item
                val isInline = chordText.startsWith("{il}")
                if (isInline) {
                    chordText = chordText.substring(4)
                }

                // Draw the chord text above the current position
                if (!isInline) {
                    val chordY = currentY - contentPaint.textSize // Position above the main text line
                    canvas.drawText(chordText, currentX, chordY, chordTextPaint)

                    // Draw the annotated character(s) itself
                    canvas.drawText(annotatedChar, currentX, currentY, contentPaint)
                    currentX += annotatedCharWidth // Advance currentX
                } else {
                    // If it's an inline chord, just draw the chord, not the annotated character
                    canvas.drawText(chordText, currentX, currentY, chordTextPaint)
                    currentX += chordTextPaint.measureText(chordText)
                }

                lastCharIndexInLine = annotationEndInLine
            }

            // Draw any remaining text after the last annotation
            if (lastCharIndexInLine < line.length) {
                val remainingText = line.substring(lastCharIndexInLine)
                canvas.drawText(remainingText, currentX, currentY, contentPaint)
            }

            // Move to the next line position
            currentY += contentPaint.fontSpacing * 2f // Add extra spacing for lines with chords
        }

        doc.finishPage(page)
        return doc
    }

    /**
     * Calculates the number of characters that can fit in the screen.
     *\
     * @param availableWidthInPx The width of the screen in pixels
     * @param fontSizeSp The font size in sp
     * @param currentDensity The current density of the screen
     *\
     * @return The number of characters that can fit in the screen
     */
    private fun getAvailableWidthInChars(availableWidthInPx: Int, fontSizeSp: Float, currentDensity: Density):UInt {
        val characterHeightInPixels = with (currentDensity) { fontSizeSp.sp.toPx() }
        val characterWidthInPixels = characterHeightInPixels * ROBOTO_ASPECT_RATIO
        val charsPerLine = floor(availableWidthInPx / characterWidthInPixels).toUInt()
        return charsPerLine
    }

    //#endregion

    //#region private data

    private val tab: LiveData<out ITab?> = if (entryId != null) dataAccess.getTabFromPlaylistEntryId(entryId) else if (providedTabId != null) dataAccess.getTab(providedTabId) else MutableLiveData(null)

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
     * aspect_ratio = ttLib.TTFont(r'path\to\font.ttf')['hmtx']['space'][0] / ttLib.TTFont(r'path\to/font.ttf')['head'].unitsPerEm
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

    override val tabId: LiveData<String> = tab.map { t -> t?.tabId ?: "" }

    override val songId: LiveData<String> = tab.map { t -> t?.songId ?: "" }

    override val artistId: LiveData<String?> = tab.map { t -> t?.artistId }

    override val useFlats: LiveData<Boolean> = dataAccess.getLivePreference(Preference.USE_FLATS).map { p -> p?.value?.toBoolean() == true }

    private val _chordsPinned: MutableLiveData<Boolean> = MutableLiveData(false)
    override val chordsPinned: LiveData<Boolean> = _chordsPinned

    override val songName: LiveData<String> = tab.map { t -> t?.songName ?: "" }

    override val version: LiveData<Int> = tab.map { t -> t?.version ?: 0 }

    override val songVersions: LiveData<List<ITab>> = tab.switchMap { t -> if(t == null) MutableLiveData(listOf()) else dataAccess.getTabsBySongId(t.songId.toString()).map { t -> t } }

    override val isFavorite: LiveData<Boolean> = if (entryId != null) dataAccess.playlistEntryExistsInFavorites(entryId) else if (providedTabId != null) dataAccess.tabExistsInFavoritesLive(providedTabId) else MutableLiveData(false)

    /**
     * Whether to display the playlist navigation bar
     */
    override val isPlaylistEntry: LiveData<Boolean> = tab.map { t -> t is TabWithDataPlaylistEntry && t.playlistId > 0 }

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

    // tab content after transposition and useFlats
    private val transposedContentString: LiveData<String> = tab.combine(transpose, useFlats) { t, tr, f ->
        val currentDbContent = t?.content ?: ""
        val currentTranspose = tr ?: 0
        val useFlats = f == true

        val chordPattern = Regex("\\{ch:([^}]+?)\\}")
        val transposedContent = chordPattern.replace(currentDbContent) {
            val chord = it.groupValues[1]
            "{ch:" + Chord.transposeChord(chord, currentTranspose, useFlats) + "}"
        }

        return@combine transposedContent
    }

    private val tabContent: LiveData<TabContent> = transposedContentString.map { transposed -> TabContent(urlHandler::openUri, transposed) }
    // transposed content converted to an annotated string (tags are stripped and chords are annotations not text)
    override val content: LiveData<AnnotatedString> = tabContent.map { tc ->
        tc.content
    }

    override val pinnedChordVariations: LiveData<List<ChordVariation>> = tabContent
        .map{c -> c.chords.toList() }
        .combine(chordInstrument) { chords, instrument ->
            if (chords != null && instrument != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    // anytime chords or instrument are updated, download the new chords
                    Chord.ensureAllChordsDownloaded(chords, instrument, dataAccess)
                }
            }
            Pair(chords, instrument)
        }
        .switchMap { (chords, instrument) ->
            if (chords == null || instrument == null) null
            else dataAccess.getFirstChordVariations(chords, instrument)
        }
    
    private val _state: MutableLiveData<LoadingState> = MutableLiveData(LoadingState.Loading)
    override val state: LiveData<LoadingState> = transposedContentString.combine(_state) { c, _ ->
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
        if (c != null) {
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
            Log.w(TAG, "Playlist next song clicked while tab (id: $providedTabId, playlistEntryId: $entryId) is null or not playlist entry: ${tab.value?.toString()}")
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
            Log.w(TAG, "Playlist previous song clicked while tab (tabId: $providedTabId, playlistEntryId: $entryId) is null or not playlist entry: ${tab.value?.toString()}")
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
    fun onChordClick(chord: String) {
        _chordDetailsState.value = LoadingState.Loading
        _chordDetailsActive.postValue(true)
        currentChordToDisplay.postValue(chord)

        // todo: handle link clicks
//        content.value?.getUrlAnnotations(clickLocation, clickLocation)?.firstOrNull()?.item?.let {
//                urlAnnotation ->
//            try {
//                uriHandler.openUri(urlAnnotation.url.trim())
//            } catch (ex: ActivityNotFoundException) {
//                Log.i(TAG, "Couldn't launch URL, copying to clipboard instead")
//                clipboardManager.nativeClipboard.setPrimaryClip(ClipData.newPlainText(urlAnnotation.url.trim(), urlAnnotation.url.trim()))
//            }
//        }
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
        }
    }

    /**
     * Handle user toggling the pinned chords display
     */
    fun onChordsPinnedToggled() {
        _chordsPinned.postValue(_chordsPinned.value != true)
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
                Log.e(TAG, "Couldn\'t load autoscroll user preference: ${err.message}", err)
                _autoscrollSpeedSliderPosition.postValue(0.5f) // have a fallback value in case of exception or database errors
            } else {
                val result = autoscrollPreferenceJob.getCompleted()
                _autoscrollSpeedSliderPosition.postValue(result ?: 0.5f)
            }
        }
    }

    //#endregion
}

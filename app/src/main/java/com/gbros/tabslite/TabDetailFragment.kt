package com.gbros.tabslite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isGone
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import com.gbros.tabslite.data.TabFull
import com.gbros.tabslite.databinding.FragmentTabDetailBinding
import com.gbros.tabslite.utilities.InjectorUtils
import com.gbros.tabslite.viewmodels.TabDetailViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min


/**
 * A fragment representing a single Tab detail screen
 */
class TabDetailFragment : Fragment() {

    private val args: TabDetailFragmentArgs by navArgs()
    private val timerHandler = Handler()
    private var isScrolling: Boolean = false
    private var scrollDelayMs: Long = 20  // default scroll speed (smaller is faster)

    //private lateinit var tab: TabFull
    private lateinit var viewModel: TabDetailViewModel
    private lateinit var binding: FragmentTabDetailBinding
    private lateinit var optionsMenu: Menu
    private var spannableText: SpannableStringBuilder = SpannableStringBuilder()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_detail, container, false)

            setHasOptionsMenu(true)

            binding.apply {
                lifecycleOwner = viewLifecycleOwner

                // autoscroll
                val timerRunnable: Runnable = object : Runnable {
                    override fun run() {
                        // todo: make this time (the 20) adjustable
                        tabDetailScrollview.smoothScrollBy(0, 1) // 5 is how many pixels you want it to scroll vertically by
                        timerHandler.postDelayed(this, scrollDelayMs) // 10 is how many milliseconds you want this thread to run
                    }
                }
                callback = object : Callback {
                    override fun scrollButtonClicked() {
                        if (isScrolling) {
                            // stop scrolling
                            timerHandler.removeCallbacks(timerRunnable)
                            fab.setImageResource(R.drawable.ic_fab_autoscroll)
                            autoscrollSpeed.isGone = true
                            (activity as AppCompatActivity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            binding.appbar.setExpanded(true, true)  // thanks https://stackoverflow.com/a/32137264/3437608
                        } else {
                            // start scrolling
                            timerHandler.postDelayed(timerRunnable, 0)
                            fab.setImageResource(R.drawable.ic_fab_pause_autoscroll)
                            autoscrollSpeed.isGone = false
                            (activity as AppCompatActivity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            binding.appbar.setExpanded(false, true)
                        }
                        isScrolling = !isScrolling
                    }
                }


                // create toolbar scroll change worker
                var isToolbarShown = false
                val scrollChangeListener = NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > binding.toolbar.height

                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar

                        // Use shadow animator to add elevation if toolbar is shown
                        binding.appbar.isActivated = shouldShowToolbar

                        // Show the plant name if toolbar is shown
                        // hacking this using the Activity title.  It seems to show whenever title isn't enabled
                        // and our normal title won't show so I'm just using reverse psychology here
                        binding.toolbarLayout.isTitleEnabled = !shouldShowToolbar
                    }
                }
                // scroll change listener begins at Y = 0 when image is fully collapsed
                tabDetailScrollview.setOnScrollChangeListener(scrollChangeListener)

                // title bar
                (activity as AppCompatActivity).apply {
                    setSupportActionBar(binding.toolbar)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.setDisplayShowHomeEnabled(true)
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                }

                // transpose
                transposeUp.setOnClickListener { _ -> transpose(true) }
                transposeDown.setOnClickListener { _ -> transpose(false) }

                // autoscroll speed seek bar
                autoscrollSpeed.clipToOutline = true  // not really needed since the background is enough bigger
                autoscrollSpeed.setOnSeekBarChangeListener(seekBarChangeListener)
                autoscrollSpeed.isGone = true

                textSizeIncrease.setOnClickListener { changeTextSize(2F) }
                textSizeDecrease.setOnClickListener { changeTextSize(-2F) }
            }

            binding.cancelTranspose.setOnClickListener {
                val currentTransposeAmt = viewModel.tab!!.transposed
                viewModel.tab!!.transposed = 0
                transpose(-currentTransposeAmt)
            }
            return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (activity is SearchResultsActivity && (activity as SearchResultsActivity).getVersions != null) {
            (activity as SearchResultsActivity).getVersions!!.invokeOnCompletion(onDataStored())
        } else {
            val getDataJob = GlobalScope.async { (activity as ISearchHelper).searchHelper?.fetchTab(args.tabId) }
            getDataJob.invokeOnCompletion(onDataStored())
        }
    }

    private fun changeTextSize(howMuch: Float){
        binding.tabContent.setTextSize(0, binding.tabContent.textSize + howMuch)
        processTabContent(viewModel.tab!!.content)
        binding.tabContent.setTabContent(spannableText)
    }

    private var seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // updated continuously as the user slides the thumb
            //convert progress to delay between 1px updates
            var myDelay = (100 - progress) / 100.0  // delay on a scale of 0 to 1
            myDelay *= 34                           // delay on a scale of 0 to 34
            myDelay += 2                            // delay on a scale of 2 to 36
            scrollDelayMs = (myDelay).toLong()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}  // called when the user first touches the SeekBar
        override fun onStopTrackingTouch(seekBar: SeekBar) {}  // called after the user finishes moving the SeekBar
    }


    private fun chordClicked(chordName: CharSequence, noUpdate: Boolean = false){
        val api = (activity as ISearchHelper).searchHelper?.api ?: return  // return if api null
        val input = ArrayList<CharSequence>()
        input.add(chordName)

        val getChordsJob = GlobalScope.async { api.getChordVariations(chordName) }
        getChordsJob.invokeOnCompletion { cause ->
            if (cause != null) {
                Log.w(javaClass.simpleName, "Getting chords from db didn't work.", cause.cause)
                Unit
            } else {
                val chordVars = getChordsJob.getCompleted()
                if (chordVars.isEmpty()) {
                    if(!noUpdate) {
                        // get from the internet
                        val updateJob = GlobalScope.async { api.updateChordVariations(input) }
                        view?.let { Snackbar.make(it, "Loading chord $chordName...", Snackbar.LENGTH_SHORT).show() }
                        updateJob.invokeOnCompletion { cause ->
                            if (cause != null) {
                                Log.w(javaClass.simpleName, "Chord update didn't work.", cause.cause)
                            }
                            // try again
                            chordClicked(chordName, true)
                        }
                    } else {
                        // we already tried and failed from the internet.  Just show an explanation
                        (activity as AppCompatActivity).runOnUiThread {
                            view?.let { Snackbar.make(it, "Chord could not be loaded.  Check your internet connection.", Snackbar.LENGTH_SHORT).show() }
                        }
                    }
                } else {
                    (activity as AppCompatActivity).runOnUiThread {
                        ChordBottomSheetDialogFragment.newInstance(chordVars).show(
                                (activity as AppCompatActivity).supportFragmentManager, null)
                    }
                }
            }
        }
    }

    private fun transpose(howMuch: Int){
        if (howMuch != 0) {
            val numSteps = howMuch.absoluteValue
            val currentSpans = spannableText.getSpans(0, spannableText.length, ClickableSpan::class.java)

            for (span in currentSpans) {
                val startIndex = spannableText.getSpanStart(span)
                val endIndex = spannableText.getSpanEnd(span)
                val currentText = span.toString()
                spannableText.removeSpan(span)

                var newText = currentText
                if (howMuch > 0) {
                    // transpose up
                    for (i in 0 until numSteps) {
                        newText = transposeUp(newText)
                    }
                } else {
                    // transpose down
                    for (i in 0 until numSteps) {
                        newText = transposeDown(newText)
                    }
                }


                spannableText.replace(startIndex, endIndex, newText)  // edit the text
                spannableText.setSpan(makeSpan(newText), startIndex, startIndex + newText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // add a new span
            }
            binding.transposeAmt.text = viewModel.tab!!.transposed.toString()
            GlobalScope.launch{(activity as ISearchHelper).searchHelper?.updateTabTransposeLevel(viewModel.tab!!.tabId, viewModel.tab!!.transposed)}
        }

        binding.tabContent.setTabContent(spannableText)
    }

    private fun transpose(up: Boolean){
        val howMuch = if(up) 1 else -1
        viewModel.tab!!.transposed += howMuch

        //13 half steps in an octave (both sides inclusive)
        if(viewModel.tab!!.transposed >= 12) {
            viewModel.tab!!.transposed -= 12
        } else if (viewModel.tab!!.transposed <= -12) {
            viewModel.tab!!.transposed += 12
        }

        transpose(howMuch)
    }
    private fun transposeUp(text: String): String {
        return when {
            text.startsWith("A#", true) -> "B" + text.substring(2)
            text.startsWith("Ab", true) -> "A" + text.substring(2)
            text.startsWith("A", true) -> "A#" + text.substring(1)
            text.startsWith("Bb", true) -> "B" + text.substring(2)
            text.startsWith("B", true) -> "C" + text.substring(1)
            text.startsWith("C#", true) -> "D" + text.substring(2)
            text.startsWith("C", true) -> "C#" + text.substring(1)
            text.startsWith("D#", true) -> "E" + text.substring(2)
            text.startsWith("Db", true) -> "D" + text.substring(2)
            text.startsWith("D", true) -> "D#" + text.substring(1)
            text.startsWith("Eb", true) -> "E" + text.substring(2)
            text.startsWith("E", true) -> "F" + text.substring(1)
            text.startsWith("F#", true) -> "G" + text.substring(2)
            text.startsWith("F", true) -> "F#" + text.substring(1)
            text.startsWith("G#", true) -> "A" + text.substring(2)
            text.startsWith("Gb", true) -> "G" + text.substring(2)
            text.startsWith("G", true) -> "G#" + text.substring(1)
            else -> {
                Log.e(javaClass.simpleName, "Weird Chord not transposed: $text")
                text
            }
        }
    }
    private fun transposeDown(text: String): String {
        return when {
            text.startsWith("A#", true) -> "A" + text.substring(2)
            text.startsWith("Ab", true) -> "G" + text.substring(2)
            text.startsWith("A", true) -> "G#" + text.substring(1)
            text.startsWith("Bb", true) -> "A" + text.substring(2)
            text.startsWith("B", true) -> "A#" + text.substring(1)
            text.startsWith("C#", true) -> "C" + text.substring(2)
            text.startsWith("C", true) -> "B" + text.substring(1)
            text.startsWith("D#", true) -> "D" + text.substring(2)
            text.startsWith("Db", true) -> "C" + text.substring(2)
            text.startsWith("D", true) -> "C#" + text.substring(1)
            text.startsWith("Eb", true) -> "D" + text.substring(2)
            text.startsWith("E", true) -> "D#" + text.substring(1)
            text.startsWith("F#", true) -> "F" + text.substring(2)
            text.startsWith("F", true) -> "E" + text.substring(1)
            text.startsWith("G#", true) -> "G" + text.substring(2)
            text.startsWith("Gb", true) -> "F" + text.substring(2)
            text.startsWith("G", true) -> "F#" + text.substring(1)
            else -> {
                Log.e(javaClass.simpleName, "Weird Chord not transposed: $text")
                text
            }
        }
    }

    private fun onDataStored() = { cause: Throwable? ->
        if(cause != null) {
            //oh no; something happened and it failed.  whoops.
            Log.w(javaClass.simpleName, "Error fetching and storing tab data from online source on the async thread.  Internet connection likely not available.")
            requireActivity().runOnUiThread {
                binding.progressBar2.isGone = true
                view?.let { Snackbar.make(it, "This tab is not available offline.", Snackbar.LENGTH_INDEFINITE).show() }
            }
            Unit
        } else {
            startGetData()
            Unit
        }
    }

    //starts here coming from the favorite tabs page; assumes data is already in db
    private fun startGetData() {
        val tabDetailViewModel: TabDetailViewModel by viewModels {
            InjectorUtils.provideTabDetailViewModelFactory(requireActivity(), args.tabId)
        }
        viewModel = tabDetailViewModel
        viewModel.getTabJob.invokeOnCompletion(onDataReceived())
    }

    // app will currently crash if the database actually doesn't have the data (tab = null).  Shouldn't happen irl, but happened in development
    private fun onDataReceived() =  { cause: Throwable? ->
        if(cause != null) {
            //oh no; something happened and it failed.  whoops.
            Log.e(javaClass.simpleName, "Error fetching tab data from database.")
            Unit
        } else {
            var reloaded = false
            var favorite = false
            val scrollSpeed = scrollDelayMs
            var transposed = 0
            if(viewModel.tab != null) {
                reloaded = true
                favorite = viewModel.tab!!.favorite  //reloading would reset favorite status, so save that
                //todo: when scroll speed is a database field, we'll need to save it here
                transposed = viewModel.tab!!.transposed
            }

            viewModel.tab = viewModel.getTabJob.getCompleted()  // actually get the data

            if (reloaded) {
                viewModel.tab!!.favorite = favorite
                viewModel.setFavorite(favorite)

                scrollDelayMs = scrollSpeed
                //todo: save scroll speed to db

                viewModel.tab!!.transposed = transposed
            }

            // thanks https://cheesecakelabs.com/blog/understanding-android-views-dimensions-set/
            binding.tabContent.doOnLayout {
                processTabContent(viewModel.tab!!.content)

                activity?.runOnUiThread {
                    binding.tab = viewModel.tab  // set view data
                    setHeartInitialState()  // set initial state of "save" heart
                    (activity as AppCompatActivity).title = viewModel.tab.toString()  // toolbar title


                    binding.progressBar2.isGone = true
                    binding.transposeAmt.text = viewModel.tab!!.transposed.toString()
                    transpose(viewModel.tab!!.transposed)  // calls binding.tabContent.setTabContent(spannableText)
                }
            }

            Unit
        }
    }

    private fun setHeartInitialState(){
        if(::viewModel.isInitialized && viewModel.tab != null && viewModel.tab!!.favorite && this::optionsMenu.isInitialized) {
            val heart = optionsMenu.findItem(R.id.action_favorite)
            heart.isChecked = true
            heart.setIcon(R.drawable.ic_favorite)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_tab_detail, menu)
        optionsMenu = menu
        setHeartInitialState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // share menu item and favorite menu item
        return when (item.itemId) {
            R.id.action_share -> {
                createShareIntent()
                true
            }
            R.id.action_favorite -> {
                if(viewModel.tab != null) {
                    item.isChecked = !item.isChecked
                    if (item.isChecked) {
                        // now it's a favorite
                        item.setIcon(R.drawable.ic_favorite)
                        viewModel.tab!!.favorite = true
                    } else {
                        item.setIcon(R.drawable.ic_unfavorite)
                        viewModel.tab!!.favorite = false
                    }
                    viewModel.setFavorite(item.isChecked)
                }
                true
            }
            R.id.action_reload -> {  // reload button clicked (refresh page)
                viewModel.getTabJob = viewModel.viewModelScope.async { viewModel.tabRepository.getTab(args.tabId) }
                val wasFavorite = viewModel.tab?.favorite

                binding.progressBar2.isGone = false
                val searchJob = GlobalScope.async {
                    (activity as ISearchHelper).searchHelper?.fetchTab(tabId = args.tabId, force = true)
                }
                searchJob.start()
                searchJob.invokeOnCompletion(onDataStored())
                true
            }
            else -> false
        }
    }

    // Helper function for calling a share functionality.
    private fun createShareIntent() {
        val shareText = viewModel.tab.let { tab ->
            if (viewModel.tab == null) {
                ""
            } else {
                getString(R.string.share_text_plant, tab.toString(), viewModel.tab!!.urlWeb)
            }
        }

        val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
                .setText(shareText)
                .setType("text/plain")
                .createChooserIntent()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }

    interface Callback {
        fun scrollButtonClicked()
    }

    private fun findMultipleLineWordBreak(lines: List<CharSequence>, paint: TextPaint, availableWidth: Float): Int{
        val breakingChars = "‐–〜゠= \t\r\n"  // all the chars that we'll break a line at
        var totalCharsToFit: Int = 0

        // find max number of chars that will fit on a line
        for (line in lines) {
            totalCharsToFit = max(totalCharsToFit, paint.breakText(line, 0, line.length,
                    true, availableWidth, null))
        }
        var wordCharsToFit = totalCharsToFit

        // go back from max until we hit a word break
        var allContainWordBreakChar: Boolean
        do {
            allContainWordBreakChar = true
            for (line in lines) {
                allContainWordBreakChar = allContainWordBreakChar
                        && (line.length <= wordCharsToFit || breakingChars.contains(line[wordCharsToFit]))
            }
        } while (!allContainWordBreakChar && --wordCharsToFit > 0)

        // if we had a super long word, just break at the end of the line
        if (wordCharsToFit < 1){
            wordCharsToFit = totalCharsToFit
        }

        return wordCharsToFit
    }


    // thanks @Hein https://stackoverflow.com/a/60886609
    private fun processLyricLine(singleLyric: CharSequence, appendTo: SpannableStringBuilder): SpannableStringBuilder {
        val indexOfLineBreak = singleLyric.indexOf("\n")
        var chords: CharSequence = singleLyric.subSequence(0, indexOfLineBreak).trimEnd()
        var lyrics: CharSequence = singleLyric.subSequence(indexOfLineBreak + 1, singleLyric.length).trimEnd()
        var startLength = appendTo.length
        var result = appendTo

        // break lines ahead of time
        // thanks @Andro https://stackoverflow.com/a/11498125
        val availableWidth = binding.tabContent.width.toFloat() //- binding.tabContent.textSize / resources.displayMetrics.scaledDensity

        while (lyrics.isNotEmpty() || chords.isNotEmpty()) {
            // find good word break spot at end
            val plainChords = chords.replace(Regex("\\[/?ch]"), "")
            val wordCharsToFit = findMultipleLineWordBreak(listOf(plainChords, lyrics), binding.tabContent.paint, availableWidth)

            // make chord substring
            var i = 0
            while (i < min(wordCharsToFit, chords.length)) {
                if (i+3 < chords.length && chords.subSequence(i .. i+3) == "[ch]"){
                    //we found a chord; add it.
                    chords = chords.removeRange(i .. i+3)        // remove [ch]
                    val start = i

                    while(chords.subSequence(i .. i+4) != "[/ch]"){
                        // find end
                        i++
                    }
                    // i is now 1 past the end of the chord name
                    chords = chords.removeRange(i .. i+4)        // remove [/ch]

                    result = result.append(chords.subSequence(start until i))

                    //make a clickable span
                    val chordName = chords.subSequence(start until i)
                    val clickableSpan = makeSpan(chordName)
                    result.setSpan(clickableSpan, startLength+start, startLength+i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    result = result.append(chords[i])
                    i++
                }
            }
            result = result.append("\r\n")

            // make lyric substring
            val thisLine = lyrics.subSequence(0, min(wordCharsToFit, lyrics.length))
            result = result.append(thisLine).append("\r\n")

            // update for next pass through
            chords = chords.subSequence(i, chords.length)
            lyrics = lyrics.subSequence(thisLine.length, lyrics.length)
            startLength = result.length
        }

        return result
    }

    private fun lonelyChordProcessor(subString: CharSequence, spannableString: SpannableStringBuilder){
        var lastIndex = 0
        var chords = subString
        while (chords.indexOf("[ch]", 0) != -1 ) {
            val firstIndex = chords.indexOf("[ch]", 0)
            chords = chords.replaceRange(firstIndex, firstIndex+4, "")

            spannableString.append(chords.subSequence(lastIndex, firstIndex))

            lastIndex = chords.indexOf("[/ch]", lastIndex)
            chords = chords.replaceRange(lastIndex, lastIndex+5, "")
            spannableString.append(chords.subSequence(firstIndex, lastIndex))

            val chordName = chords.subSequence(firstIndex until lastIndex)
            val clickableSpan = makeSpan(chordName)

            spannableString.setSpan(clickableSpan, spannableString.length-(lastIndex-firstIndex),
                    spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        spannableString.append((chords.subSequence(lastIndex until chords.length)))
    }

    private fun lonelyChordProcessor(spannableString: SpannableStringBuilder){
        while(spannableString.indexOf("[ch]") != -1) {
            val firstIndex = spannableString.indexOf("[ch]")
            spannableString.delete(firstIndex, firstIndex+4)
            val lastIndex = spannableString.indexOf("[/ch]")
            spannableString.delete(lastIndex, lastIndex+5)

            val chordName = spannableString.subSequence(firstIndex until lastIndex)
            val clickableSpan = makeSpan(chordName)
            spannableString.setSpan(clickableSpan, firstIndex, lastIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun processTabContent(text: CharSequence): SpannableStringBuilder{
        var text = text
        var spannableString = SpannableStringBuilder()

        //wrap tabs as a group
        var lastIndex = 0
        while (text.indexOf("[tab]", lastIndex) != -1) {
            val firstIndex = text.indexOf("[tab]", 0)     // remove start tag
            text = text.replaceRange(firstIndex, firstIndex + 5, "")

            lonelyChordProcessor(text.subSequence(lastIndex, firstIndex), spannableString) // add all the non-[tab] text

            lastIndex = text.indexOf("[/tab]", firstIndex)    // remove end tag
            text = text.replaceRange(lastIndex, lastIndex + 6, "")

            val next = processLyricLine(text.subSequence(firstIndex, lastIndex), spannableString)
            spannableString = next
        }
        lonelyChordProcessor(text.subSequence(lastIndex, text.length), spannableString) // add all the non-[tab] text
        lonelyChordProcessor(spannableString) // a final once-over to check for any missed chords (usually a tab author's mistake)

        spannableText = spannableString
        return spannableString
    }

    private fun TextView.setTabContent(spannableString: SpannableStringBuilder) {
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableText, TextView.BufferType.SPANNABLE)
    }

    //thanks https://stackoverflow.com/a/51561533/3437608
    fun Context.getColorFromAttr( @AttrRes attrColor: Int,
            typedValue: TypedValue = TypedValue(),
            resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    private fun makeSpan(chordName: CharSequence): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                chordClicked(chordName.toString())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                if(context != null) {
                    ds.color = context!!.getColorFromAttr(R.attr.colorOnSecondary)
                    ds.bgColor = context!!.getColorFromAttr(R.attr.colorPrimarySurface)
                }
            }

            override fun toString(): String {
                return chordName.toString()
            }
        }
    }
}

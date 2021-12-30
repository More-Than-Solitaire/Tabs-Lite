package com.gbros.tabslite

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import com.gbros.tabslite.data.TabLine
import com.gbros.tabslite.data.TabLineDouble
import com.gbros.tabslite.data.TabLineSingle
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


private const val LOG_NAME = "tabslite.TabTextView"

class TabTextView(context: Context, attributeSet: AttributeSet): androidx.appcompat.widget.AppCompatTextView(context, attributeSet) {
    private val tabLines = ArrayList<TabLine>()
    private val mScaleDetector = object : ScaleGestureDetector(context, ScaleListener()) {
        override fun onTouchEvent(event: MotionEvent?): Boolean {

            if(event != null && event.pointerCount == 2){
                // for two finger touches, no intercepts so we can zoom
                parent.requestDisallowInterceptTouchEvent(true)
            } else {
                parent.requestDisallowInterceptTouchEvent(false)
            }
            return super.onTouchEvent(event)
        }
    }
    private lateinit var callback: Callback

    override fun onFinishInflate() {
        super.onFinishInflate()

        if(monoBold == null) {
            monoBold = Typeface.createFromAsset(context?.assets, "font/RobotoMono-Bold.ttf")
        }
        if(monoRegular == null) {
            monoRegular = Typeface.createFromAsset(context?.assets, "font/RobotoMono-Light.ttf")
        }
        typeface = monoRegular
    }
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        super.dispatchTouchEvent(event)
        return mScaleDetector.onTouchEvent(event)
    }

    fun setCallback(callback: Callback){
        this.callback = callback
    }

    // takes entire content for tab, and displays it.  Handles [tab][/tab] blocks as tab/chords dual-
    // lined blocks, and handles any other content as plain text (but still searches for [ch][/ch] blocks
    // to replace with the appropriate chords links
    fun setTabContent(content: CharSequence) {
        Log.v(LOG_NAME, "Setting tab content")
        var t = content

        tabLines.clear()  // remove any previous content

        var indexOfEndOfTabBlock = 0
        Log.v(LOG_NAME, "Breaking single lines into chords/lyrics")
        while (t.indexOf("[tab]", indexOfEndOfTabBlock) != -1) {
            val indexOfStartOfTabBlock = t.indexOf("[tab]", 0)     // remove start tag
            t = t.replaceRange(indexOfStartOfTabBlock, indexOfStartOfTabBlock + 5, "")

            // any content that isn't inside [tab] blocks should be added without custom word-wrapping.  Default wrapping can take care of long lines here.
            insertContentToTabLines(t.subSequence(indexOfEndOfTabBlock, indexOfStartOfTabBlock))

            indexOfEndOfTabBlock = t.indexOf("[/tab]", indexOfStartOfTabBlock)    // remove end tag
            t = t.replaceRange(indexOfEndOfTabBlock, indexOfEndOfTabBlock + 6, "")

            // any content that *is* inside [tab] blocks should be custom word-wrapped (wrapped two lines at a time)
            linify(t.subSequence(indexOfStartOfTabBlock, indexOfEndOfTabBlock))
        }
        insertContentToTabLines(t.subSequence(indexOfEndOfTabBlock, t.length))
        wrapTextIntoView()
    }

    // takes a [tab] and breaks it into two lines (chord and lyric).  Adds chord processing and
    // puts it in `tabLines`.
    private fun linify(singleLyric: CharSequence){
        var indexOfExistingLineBreak = singleLyric.indexOf("\n")
        if (indexOfExistingLineBreak == -1)  // in case of lines ending without a newline after.
            indexOfExistingLineBreak = singleLyric.length
        val chords: CharSequence = singleLyric.subSequence(0, indexOfExistingLineBreak).trimEnd()
        var lyrics: CharSequence = ""
        if (indexOfExistingLineBreak < singleLyric.length) {
            lyrics = singleLyric.subSequence(indexOfExistingLineBreak + 1, singleLyric.length).trimEnd()
        }
        tabLines.add(TabLineDouble(processChords(chords), processChords(lyrics)))
    }
    // takes a string and replaces all [ch]'s with clickable spans
    // takes non-[tab] content, finds the [ch]'s, processes those, and adds the whole thing to tabLines
    private fun insertContentToTabLines(content: CharSequence){
        tabLines.add(TabLineSingle(processChords(content)))
    }
    private fun processChords(content: CharSequence): SpannableStringBuilder {
        val result = SpannableStringBuilder()
        var text = content.trimEnd()
        var lastIndex = 0

        while (text.indexOf("[ch]", 0) != -1 ) {
            val firstIndex = text.indexOf("[ch]", 0)
            text = text.replaceRange(firstIndex, firstIndex + 4, "")  // remove "[ch]"
            result.append(text.subSequence(lastIndex, firstIndex))

            lastIndex = text.indexOf("[/ch]", lastIndex)
            text = text.replaceRange(lastIndex, lastIndex + 5, "")  // remove "[/ch]"
            result.append(text.subSequence(firstIndex, lastIndex))

            val chordName = text.subSequence(firstIndex until lastIndex)
            val clickableSpan = makeSpan(chordName)

            result.setSpan(clickableSpan, result.length - (lastIndex - firstIndex),
                    result.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        result.append((text.subSequence(lastIndex until text.length)))

        return result
    }
    private fun makeSpan(chordName: CharSequence): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                callback.chordClicked(chordName.toString())
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                context?.apply {
                    ds.color = getColorFromAttr(R.attr.colorOnSecondary)
                    ds.bgColor = getColorFromAttr(R.attr.colorPrimarySurface)
                }
                ds.typeface = monoBold
                ds.isUnderlineText = false  // no underlines
            }

            override fun toString(): String {
                return chordName.toString()
            }
        }
    }
    // word wraps the tabLines object into the textview
    private fun wrapTextIntoView() {
        Log.v(LOG_NAME, "Wrapping finished text into TextView")
        val availableWidth = width.toFloat() - textSize / resources.displayMetrics.scaledDensity
        val spannableText = SpannableStringBuilder()
        for (tabLine in tabLines) {
            when (tabLine) {
                is TabLineSingle -> spannableText.append(tabLine.line).append("\r\n")  // no need for custom wrapping
                is TabLineDouble -> {
                    var lyricStart = 0
                    var chordStart = 0

                    while(chordStart < tabLine.chordsLine.length || lyricStart < tabLine.lyricsLine.length) {
                        val wrapIndex = findMultipleLineWordBreakIndex(availableWidth, listOf(tabLine.chordsLine.subSequence(chordStart, tabLine.chordsLine.length),
                            tabLine.lyricsLine.subSequence(lyricStart, tabLine.lyricsLine.length)))

                        // make chord substring
                        val singleLineChordsSubstring = tabLine.chordsLine.subSequence(chordStart, min(chordStart + wrapIndex, tabLine.chordsLine.length))
                        spannableText.append(singleLineChordsSubstring.trimEnd())
                        if (singleLineChordsSubstring.trimEnd().length < availableWidth) {
                            spannableText.append(' ') // if we leave the chord at the end, the empty space following will become clickable.
                        }
                        spannableText.append("\r\n")

                        // make lyric substring
                        val singleLineLyricsSubstring = tabLine.lyricsLine.subSequence(lyricStart, min(lyricStart + wrapIndex, tabLine.lyricsLine.length))
                        spannableText.append(singleLineLyricsSubstring.trimEnd()).append("\r\n")

                        // update for next pass through
                        chordStart += singleLineChordsSubstring.length
                        lyricStart += singleLineLyricsSubstring.length
                    }
                }
                else -> Log.w(LOG_NAME, "wrapTextIntoView() ran into an unrecognized type of TabLine.  Did you add functionality?")
            }
        }

        movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        setText(spannableText, BufferType.SPANNABLE)
        if(!isInLayout) {
            requestLayout()
        } else {
            Log.v(LOG_NAME, "Could not call requestLayout() (isInLayout = true). Posting runnable instead")
            post(Runnable { requestLayout() })
        }
    }
    private fun findMultipleLineWordBreakIndex(availableWidth: Float, lines: List<CharSequence>): Int {
        // thanks @Andro https://stackoverflow.com/a/11498125
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

    fun transpose(howMuch: Int){
        if (howMuch != 0) {
            for (tabLine in tabLines) {
                when (tabLine) {
                    is TabLineSingle -> transposeLine(howMuch, tabLine.line)
                    is TabLineDouble -> {
                        transposeLine(howMuch, tabLine.chordsLine)
                        transposeLine(howMuch, tabLine.lyricsLine)
                    }
                    else -> Log.w(LOG_NAME, "transpose() encountered an unexpected TabLine type.  Did you add new functionality?")
                }
            }
        }
        wrapTextIntoView()
    }

    // transpose one SpannableStringBuilder
    private fun transposeLine(howMuch: Int, line: SpannableStringBuilder) {
        val currentSpans = line.getSpans(0, line.length, ClickableSpan::class.java)

        for (span in currentSpans) {
            val startIndex = line.getSpanStart(span)
            val endIndex = line.getSpanEnd(span)
            val currentText = span.toString()
            line.removeSpan(span)

            val newText = transposeChord(currentText, howMuch)

            line.replace(startIndex, endIndex, newText)  // edit the text
            line.setSpan(makeSpan(newText), startIndex, startIndex + newText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // add a new span
        }
    }

    interface Callback {
        fun chordClicked(chordName: CharSequence)
    }
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Handle the scale..
            var mScaleFactor = 1f
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f))

            this@TabTextView.apply {
                setTextSize(0, textSize * mScaleFactor)
                wrapTextIntoView()
            }
            return true
        }
    }
    companion object {
        var monoRegular: Typeface? = null
        var monoBold: Typeface? = null

        fun transposeChord(chord: CharSequence, howMuch: Int): String {
            val numSteps = abs(howMuch)
            val up = howMuch > 0
            var newChord = chord.toString()

            if (newChord != "") {
                if (up) {
                    // transpose up
                    for (i in 0 until numSteps) {
                        newChord = transposeUp(newChord)
                    }
                } else {
                    // transpose down
                    for (i in 0 until numSteps) {
                        newChord = transposeDown(newChord)
                    }
                }
            }

            return newChord
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
                    Log.e(LOG_NAME, "Weird Chord not transposed: $text")
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
                    Log.e(LOG_NAME, "Weird Chord not transposed: $text")
                    text
                }
            }
        }

    }

    //thanks https://stackoverflow.com/a/51561533/3437608
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}
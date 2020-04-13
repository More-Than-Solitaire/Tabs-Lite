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
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

private const val LOG_NAME = "tabslite.TabTextView"

class TabTextView(context: Context, attributeSet: AttributeSet): androidx.appcompat.widget.AppCompatTextView(context, attributeSet) {
    private val tabLines = ArrayList<Pair<SpannableStringBuilder, SpannableStringBuilder>>()
    private var spannableText = SpannableStringBuilder()  // global so we can transpose without redoing the whole thing
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

    fun setTabContent(content: CharSequence) {
        var t = content

        var lastIndex = 0
        while (t.indexOf("[tab]", lastIndex) != -1) {
            val firstIndex = t.indexOf("[tab]", 0)     // remove start tag
            t = t.replaceRange(firstIndex, firstIndex + 5, "")
            insertContentToTabLines(t.subSequence(lastIndex, firstIndex))
            lastIndex = t.indexOf("[/tab]", firstIndex)    // remove end tag
            t = t.replaceRange(lastIndex, lastIndex + 6, "")
            linify(t.subSequence(firstIndex, lastIndex))
        }
        insertContentToTabLines(t.subSequence(lastIndex, t.length))
        wrapTextIntoView()
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

    // takes a [tab] and breaks it into two lines (chord and lyric).  Adds chord processing and
    // puts it in `tabLines`.
    private fun linify(singleLyric: CharSequence){
        val indexOfLineBreak = singleLyric.indexOf("\n")
        val chords: CharSequence = singleLyric.subSequence(0, indexOfLineBreak).trimEnd()
        val lyrics: CharSequence = singleLyric.subSequence(indexOfLineBreak + 1, singleLyric.length).trimEnd()
        tabLines.add(Pair(processChords(chords), processChords(lyrics)))
    }
    // takes a string and replaces all [ch]'s with clickable spans
    private fun processChords(content: CharSequence): SpannableStringBuilder {
        val result = SpannableStringBuilder()
        var text = content.trimEnd()
        var lastIndex = 0

        while (text.indexOf("[ch]", 0) != -1 ) {
            val firstIndex = text.indexOf("[ch]", 0)
            text = text.replaceRange(firstIndex, firstIndex+4, "")
            result.append(text.subSequence(lastIndex, firstIndex))

            lastIndex = text.indexOf("[/ch]", lastIndex)
            text = text.replaceRange(lastIndex, lastIndex+5, "")
            result.append(text.subSequence(firstIndex, lastIndex))

            val chordName = text.subSequence(firstIndex until lastIndex)
            val clickableSpan = makeSpan(chordName)

            result.setSpan(clickableSpan, result.length-(lastIndex-firstIndex),
                    result.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        result.append((text.subSequence(lastIndex until text.length)))

        return result
    }
    // takes non-[tab] content, finds the [ch]'s, processes those, and adds the whole thing to tabLines
    private fun insertContentToTabLines(content: CharSequence){
        tabLines.add(Pair(processChords(content), SpannableStringBuilder()))
    }
    // word wraps the tabLines object into the textview
    private fun wrapTextIntoView() {
        spannableText = SpannableStringBuilder()
        for (linePair in tabLines) {
            val chord = linePair.first
            val lyric = linePair.second

            var lyricStart = 0
            var chordStart = 0
            while(chordStart < chord.length || lyricStart < lyric.length) {
                val wrapIndex = findMultipleLineWordBreakIndex(listOf(linePair.first.subSequence(chordStart, chord.length),
                        linePair.second.subSequence(lyricStart, lyric.length)))

                // make chord substring
                val chordLine = chord.subSequence(chordStart, min(chordStart + wrapIndex, chord.length))
                spannableText.append(chordLine).append("\r\n")

                // make lyric substring
                val lyricLine = lyric.subSequence(lyricStart, min(lyricStart + wrapIndex, lyric.length))
                if(lyricLine.isNotEmpty()) {
                    spannableText.append(lyricLine).append("\r\n")
                }

                // update for next pass through
                chordStart += chordLine.length
                lyricStart += lyricLine.length
            }
        }

        movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        setText(spannableText, BufferType.SPANNABLE)
    }
    private fun findMultipleLineWordBreakIndex(lines: List<CharSequence>): Int {
        // thanks @Andro https://stackoverflow.com/a/11498125
        val availableWidth = width.toFloat() - textSize / resources.displayMetrics.scaledDensity
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


    //todo: transpose tabLines instead of spannable text
    fun transpose(howMuch: Int){
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
        }

        movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        setText(spannableText, BufferType.SPANNABLE)
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
    companion object Font {
        var monoRegular: Typeface? = null
        var monoBold: Typeface? = null
    }
    //thanks https://stackoverflow.com/a/51561533/3437608
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}
package com.chrynan.chords.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.chrynan.chords.R
import com.chrynan.chords.model.Chord
import com.chrynan.chords.model.ChordChart
import com.chrynan.chords.model.StringLabelState
import com.chrynan.chords.util.getTypeface
import com.chrynan.chords.view.ChordView
import com.chrynan.chords.view.ChordView.Companion.DEFAULT_FIT_TO_HEIGHT
import com.chrynan.chords.view.ChordView.Companion.DEFAULT_MUTED_TEXT
import com.chrynan.chords.view.ChordView.Companion.DEFAULT_OPEN_TEXT
import com.chrynan.chords.view.ChordView.Companion.DEFAULT_SHOW_FINGER_NUMBERS
import com.chrynan.chords.view.ChordView.Companion.DEFAULT_SHOW_FRET_NUMBERS
import com.chrynan.chords.view.ChordView.Companion.DEFAULT_STRING_LABEL_STATE
import kotlin.math.min
import kotlin.math.round

/*
 * Copyright 2020 chRyNaN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A View class to display guitar (or other stringed fretted instruments) chords as a chart.
 *
 * <> TABS LITE: We're using this version rather than the published version due to a bug in v2.1.0,
 * which is fixed here.  See https://github.com/chRyNaN/chords/issues/1.
 */
class MyChordWidget : View,
        ChordView {

    override var chord: Chord? = null
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    override var chart: ChordChart = ChordChart(fretStart = 1, fretEnd = 3, stringCount = 6)
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    override var fitToHeight: Boolean = DEFAULT_FIT_TO_HEIGHT
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    override var showFretNumbers = DEFAULT_SHOW_FRET_NUMBERS
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    override var showFingerNumbers = DEFAULT_SHOW_FINGER_NUMBERS
        set(value) {
            field = value
            invalidate()
        }
    override var stringLabelState: StringLabelState = DEFAULT_STRING_LABEL_STATE
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    override var mutedStringText: String = DEFAULT_MUTED_TEXT
        set(value) {
            field = value
            invalidate()
        }
    override var openStringText: String = DEFAULT_OPEN_TEXT
        set(value) {
            field = value
            invalidate()
        }

    override var fretColor = DEFAULT_COLOR
        set(value) {
            field = value
            fretPaint.color = value
            invalidate()
        }
    override var fretLabelTextColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            fretLabelTextPaint.color = value
            invalidate()
        }
    override var stringColor = DEFAULT_COLOR
        set(value) {
            field = value
            stringPaint.color = value
            invalidate()
        }
    override var stringLabelTextColor = DEFAULT_COLOR
        set(value) {
            field = value
            stringLabelTextPaint.color = value
            invalidate()
        }
    override var noteColor = DEFAULT_COLOR
        set(value) {
            field = value
            notePaint.color = value
            barLinePaint.color = value
            invalidate()
        }
    override var noteLabelTextColor = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            noteLabelTextPaint.color = value
            invalidate()
        }

    var typeface: Typeface = Typeface.DEFAULT
        set(value) {
            field = value
            fretLabelTextPaint.typeface = value
            noteLabelTextPaint.typeface = value
            stringLabelTextPaint.typeface = value
            invalidate()
        }

    private val fretPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fretLabelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val stringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val stringLabelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val notePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val noteLabelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val barLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val drawingBounds = RectF()
    private val chartBounds = RectF()
    private val stringTopLabelBounds = RectF()
    private val stringBottomLabelBounds = RectF()
    private val fretSideLabelBounds = RectF()

    private val fretCount: Int
        get() = chart.fretEnd - chart.fretStart + 1

    private val showBottomStringLabels: Boolean
        get() = stringLabelState != StringLabelState.HIDE && chart.stringLabels.isNotEmpty()

    private val fretLineRects = mutableListOf<RectF>()
    private val stringLineRects = mutableListOf<RectF>()
    private val fretNumberPoints = mutableListOf<PointF>()
    private val notePositions = mutableListOf<NotePosition>()
    private val barLinePaths = mutableListOf<BarPosition>()
    private val stringBottomLabelPositions = mutableListOf<StringPosition>()
    private val stringTopMarkerPositions = mutableListOf<StringPosition>()

    private var fretSize = 0f //y value = 0
    private var stringDistance = 0f //x value = 0f

    private var fretMarkerSize = 0f
        set(value) {
            field = value
            fretPaint.strokeWidth = value
        }
    private var fretLabelTextSize = 0f
        set(value) {
            field = value
            fretLabelTextPaint.textSize = value
        }
    private var stringSize = 0f
        set(value) {
            field = value
            stringPaint.strokeWidth = value
            barLinePaint.strokeWidth = 2 * value
        }
    private var stringLabelTextSize = 0f
        set(value) {
            field = value
            stringLabelTextPaint.textSize = value
        }
    private var noteSize = 0f
        set(value) {
            field = value
            notePaint.strokeWidth = value
        }
    private var noteLabelTextSize = 0f
        set(value) {
            field = value
            noteLabelTextPaint.textSize = value
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (!isInEditMode) {
            if (attrs != null) {
                val a = getContext().theme.obtainStyledAttributes(attrs, R.styleable.ChordWidget, defStyleAttr, 0)

                try {
                    fitToHeight = a.getBoolean(R.styleable.ChordWidget_fitToHeight, DEFAULT_FIT_TO_HEIGHT)

                    fretColor = a.getColor(R.styleable.ChordWidget_fretColor, DEFAULT_COLOR)
                    stringColor = a.getColor(R.styleable.ChordWidget_stringColor, DEFAULT_COLOR)
                    fretLabelTextColor = a.getColor(R.styleable.ChordWidget_fretLabelTextColor, DEFAULT_COLOR)
                    stringLabelTextColor = a.getColor(R.styleable.ChordWidget_stringLabelTextColor, DEFAULT_COLOR)
                    noteColor = a.getColor(R.styleable.ChordWidget_noteColor, DEFAULT_COLOR)
                    noteLabelTextColor = a.getColor(R.styleable.ChordWidget_noteLabelTextColor, DEFAULT_TEXT_COLOR)

                    mutedStringText = a.getString(R.styleable.ChordWidget_mutedStringText)
                            ?: DEFAULT_MUTED_TEXT
                    openStringText = a.getString(R.styleable.ChordWidget_openStringText)
                            ?: DEFAULT_OPEN_TEXT

                    stringLabelState = when (a.getInt(R.styleable.ChordWidget_stringLabelState, 0)) {
                        0 -> StringLabelState.SHOW_NUMBER
                        1 -> StringLabelState.SHOW_LABEL
                        else -> StringLabelState.HIDE
                    }

                    showFingerNumbers = a.getBoolean(R.styleable.ChordWidget_showFingerNumbers, DEFAULT_SHOW_FINGER_NUMBERS)
                    showFretNumbers = a.getBoolean(R.styleable.ChordWidget_showFretNumbers, DEFAULT_SHOW_FRET_NUMBERS)

                    a.getTypeface(context, R.styleable.ChordWidget_typeface)?.let { typeface = it }
                } finally {
                    a.recycle()
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        calculateSize()
        calculateBarLinePositions()
        calculateFretNumberPositions()
        calculateFretPositions()
        calculateNotePositions()
        calculateStringMarkers()
        calculateStringPositions()
    }

    override fun onDraw(canvas: Canvas) {
        // First draw the strings and fret markers
        fretLineRects.forEach { canvas.drawLine(it, fretPaint) }
        stringLineRects.forEach { canvas.drawLine(it, stringPaint) }

        // Next draw the fret numbers and string markers
        drawFretNumbers(canvas)
        drawStringMarkers(canvas)

        // Finally, draw all the notes and the note text
        drawBars(canvas)
        drawNotes(canvas)
    }

    private fun calculateSize() {
        val absoluteWidth = measuredWidth - (paddingLeft + paddingRight).toFloat()
        val absoluteHeight = measuredHeight - (paddingTop + paddingBottom).toFloat()
        val minSideSize = min(absoluteWidth, absoluteHeight)
        val actualWidth = if (fitToHeight) minSideSize * (2f / 3f) else absoluteWidth
        val actualHeight = if (fitToHeight) minSideSize else absoluteHeight

        // Center everything
        drawingBounds.set(
                (absoluteWidth - actualWidth) / 2,
                (absoluteHeight - actualHeight) / 2,
                (absoluteWidth - actualWidth) / 2 + actualWidth,
                (absoluteHeight - actualHeight) / 2 + actualHeight)

        // Give some space for the labels
        val horizontalExtraCount = if (showFretNumbers) 1 else 0
        val verticalExtraCount = if (showBottomStringLabels) 2 else 1

        // We add 1 to make room for two halves of notes displayed on the first and last strings. Otherwise, they'll be cut-off.
        noteSize = min((actualWidth / (chart.stringCount + 1 + horizontalExtraCount)), (actualHeight / (fretCount + 1 + verticalExtraCount)))

        val textSize = noteSize * .75f
        fretLabelTextSize = textSize
        stringLabelTextSize = textSize
        noteLabelTextSize = textSize

        stringDistance = noteSize

        stringSize = (stringDistance / chart.stringCount).coerceAtLeast(1f)
        fretMarkerSize = stringSize

        fretSize = round((actualHeight - (noteSize * verticalExtraCount) - (fretCount + 1) * fretMarkerSize) / fretCount)

        // The actual chart bounds
        chartBounds.set(
                drawingBounds.left + (noteSize * horizontalExtraCount) + (noteSize * .5f),
                drawingBounds.top + noteSize,
                drawingBounds.right - (noteSize * .5f),
                drawingBounds.bottom - (if (showBottomStringLabels) noteSize else 0f))

        // The open/closed labels for the String above the chart
        stringTopLabelBounds.set(
                chartBounds.left,
                drawingBounds.top,
                chartBounds.right,
                drawingBounds.top + noteSize)

        // The number/note labels for the String below the chart
        stringBottomLabelBounds.set(
                chartBounds.left,
                chartBounds.bottom,
                chartBounds.right,
                chartBounds.bottom + noteSize)

        // The fret number labels on the side of the chart
        fretSideLabelBounds.set(
                drawingBounds.left,
                chartBounds.top,
                drawingBounds.left + noteSize,
                drawingBounds.bottom)
    }

    private fun calculateFretPositions() {
        fretLineRects.clear()

        for (i in 0..fretCount) {
            fretLineRects.add(RectF(
                    chartBounds.left,
                    chartBounds.top + i * fretSize + i * fretMarkerSize,
                    chartBounds.right - stringSize,
                    chartBounds.top + i * fretSize + i * fretMarkerSize))
        }
    }

    private fun calculateStringPositions() {
        stringLineRects.clear()

        for (i in 0 until chart.stringCount) {
            stringLineRects.add(RectF(
                    chartBounds.left + i * stringDistance + i * stringSize,
                    chartBounds.top,
                    chartBounds.left + i * stringDistance + i * stringSize,
                    chartBounds.top + fretCount * fretSize + fretCount * fretMarkerSize))
        }
    }

    private fun calculateFretNumberPositions() {
        fretNumberPoints.clear()

        for (i in 0 until fretCount) { //edit: move the fret numbers to the beginning of the layout
            fretNumberPoints.add(PointF(
                    drawingBounds.left + fretSideLabelBounds.width() / 2,
                    getVerticalCenterTextPosition(stringTopLabelBounds.bottom + i * fretMarkerSize + i * fretSize + fretSize / 2, (i + 1).toString(), fretLabelTextPaint)))
        }
    }

    private fun calculateBarLinePositions() {
        barLinePaths.clear()

        chord?.bars?.forEach { bar ->
            if (bar.fret.number in chart.fretStart..chart.fretEnd && bar.endString.number < chart.stringCount + 1) {
                val relativeHeight = bar.fret.number - (chart.fretStart - 1)  // edit:
                val left = (chartBounds.left + (chart.stringCount - bar.endString.number) * stringDistance +
                        (chart.stringCount - bar.endString.number) * stringSize) - noteSize / 2
                val top = chartBounds.top + (relativeHeight * fretSize + relativeHeight * fretMarkerSize - fretSize / 2) - (noteSize / 2)  // edit:
                val right = (chartBounds.left + (chart.stringCount - bar.startString.number) * stringDistance +
                        (chart.stringCount - bar.startString.number) * stringSize) + (noteSize / 2)
                val bottom = top + noteSize
                val textX = left + (right - left) / 2
                val textY = getVerticalCenterTextPosition(top + (bottom - top) / 2, bar.finger.name, noteLabelTextPaint)

                barLinePaths.add(
                        BarPosition(
                                text = bar.finger.position.toString(),
                                textX = textX,
                                textY = textY,
                                left = left,
                                top = top,
                                right = right,
                                bottom = bottom))
            }
        }
    }

    private fun calculateNotePositions() {
        notePositions.clear()

        chord?.notes?.forEach { note ->
            if (note.fret.number in chart.fretStart..chart.fretEnd && note.string.number < chart.stringCount + 1) {
                val relativeHeight = note.fret.number - (chart.fretStart - 1) //edit:
                val startCenterX = chartBounds.left + (chart.stringCount - note.string.number) * stringDistance + (chart.stringCount - note.string.number) * stringSize
                val startCenterY = chartBounds.top + (relativeHeight * fretSize + relativeHeight * fretMarkerSize - fretSize / 2)  // edit: move each node up by chord.fretStart amount

                notePositions.add(
                        NotePosition(
                                text = note.finger.toString(),
                                circleX = startCenterX,
                                circleY = startCenterY,
                                textX = startCenterX,
                                textY = getVerticalCenterTextPosition(startCenterY, note.finger.toString(), noteLabelTextPaint)))
            }
        }
    }

    private fun calculateStringMarkers() {
        stringBottomLabelPositions.clear()
        stringTopMarkerPositions.clear()

        // Top string mute labels
        chord?.mutes?.forEach { muted ->
            if (muted.string.number < chart.stringCount + 1) {
                val x = chartBounds.left + (chart.stringCount - muted.string.number) * stringDistance + (chart.stringCount - muted.string.number) * stringSize
                val y = getVerticalCenterTextPosition(drawingBounds.top + stringTopLabelBounds.height() / 2, mutedStringText, stringLabelTextPaint)

                stringTopMarkerPositions.add(
                        StringPosition(
                                text = mutedStringText,
                                textX = x,
                                textY = y))
            }
        }

        // Top string open labels
        chord?.opens?.forEach { open ->
            if (open.string.number < chart.stringCount + 1) {
                val x = chartBounds.left + (chart.stringCount - open.string.number) * stringDistance + (chart.stringCount - open.string.number) * stringSize
                val y = getVerticalCenterTextPosition(drawingBounds.top + stringTopLabelBounds.height() / 2, openStringText, stringLabelTextPaint)

                stringTopMarkerPositions.add(
                        StringPosition(
                                text = openStringText,
                                textX = x,
                                textY = y))
            }
        }

        if (showBottomStringLabels) {
            chart.stringLabels.forEach { stringLabel ->
                if (stringLabel.string < chart.stringCount + 1) {
                    val label = if (stringLabelState == StringLabelState.SHOW_NUMBER) stringLabel.string.toString() else stringLabel.label

                    if (label != null) {
                        val x = chartBounds.left + (chart.stringCount - stringLabel.string) * stringDistance + (chart.stringCount - stringLabel.string) * stringSize
                        val y = getVerticalCenterTextPosition(chartBounds.bottom + stringBottomLabelBounds.height() / 2, label, stringLabelTextPaint)

                        stringBottomLabelPositions.add(
                                StringPosition(
                                        text = label,
                                        textX = x,
                                        textY = y))
                    }
                }
            }
        }
    }

    private fun drawFretNumbers(canvas: Canvas) {
        // Fret numbers; check if we are showing them or not
        if (showFretNumbers) {
            fretNumberPoints.forEachIndexed { index, point ->
                canvas.drawText((chart.fretStart + index).toString(), point.x, point.y, fretLabelTextPaint)
            }
        }
    }

    private fun drawStringMarkers(canvas: Canvas) {
        // Top String markers (open/muted)
        stringTopMarkerPositions.forEach { canvas.drawText(it.text, it.textX, it.textY, stringLabelTextPaint) }

        // Bottom String labels (number/note)
        stringBottomLabelPositions.forEach { canvas.drawText(it.text, it.textX, it.textY, stringLabelTextPaint) }
    }

    private fun drawBars(canvas: Canvas) {
        // Bars
        barLinePaths.forEach {
            // Draw Bar
            canvas.drawRoundRect(it.left, it.top, it.right, it.bottom, (it.bottom - it.top), (it.bottom - it.top), barLinePaint)

            // Text
            if (showFingerNumbers) {
                canvas.drawText(it.text, it.textX, it.textY, noteLabelTextPaint)
            }
        }
    }

    private fun drawNotes(canvas: Canvas) {
        //Individual notes
        notePositions.forEach {
            canvas.drawCircle(it.circleX, it.circleY, noteSize / 2f, notePaint)

            if (showFingerNumbers) {
                canvas.drawText(it.text, it.textX, it.textY, noteLabelTextPaint)
            }
        }
    }

    private fun getVerticalCenterTextPosition(originalYPosition: Float, text: String?, textPaint: Paint): Float {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text?.length ?: 0, bounds)

        return originalYPosition + bounds.height() / 2
    }

    private fun Canvas.drawLine(rectF: RectF, paint: Paint) =
            drawLine(rectF.left, rectF.top, rectF.right, rectF.bottom, paint)

    companion object {

        const val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_TEXT_COLOR = Color.WHITE
    }

    private data class NotePosition(
            val text: String,
            val circleX: Float,
            val circleY: Float,
            val textX: Float,
            val textY: Float
    )

    private data class StringPosition(
            val text: String,
            val textX: Float,
            val textY: Float
    )

    private data class BarPosition(
            val text: String,
            val left: Float,
            val top: Float,
            val right: Float,
            val bottom: Float,
            val textX: Float,
            val textY: Float
    )
}
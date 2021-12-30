package com.gbros.tabslite.data

import android.text.SpannableStringBuilder

class TabLineDouble(chordsLine: SpannableStringBuilder, lyricsLine: SpannableStringBuilder): TabLine {
    override val lineType = TabLine.LineType.SINGLE
    val chordsLine = chordsLine
    val lyricsLine = lyricsLine
}
package com.gbros.tabslite.data.tabcontent

import android.text.SpannableStringBuilder

class TabLineDouble(val chordsLine: SpannableStringBuilder, val lyricsLine: SpannableStringBuilder):
    TabLine {
    override val lineType = TabLine.LineType.SINGLE
}
package com.gbros.tabslite.data

import android.text.SpannableStringBuilder

class TabLineSingle(line: SpannableStringBuilder): TabLine {
    override val lineType = TabLine.LineType.SINGLE
    val line = line
}
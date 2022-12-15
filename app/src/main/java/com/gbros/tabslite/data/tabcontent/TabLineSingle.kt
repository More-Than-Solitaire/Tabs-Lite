package com.gbros.tabslite.data.tabcontent

import android.text.SpannableStringBuilder

class TabLineSingle(val line: SpannableStringBuilder): TabLine {
    override val lineType = TabLine.LineType.SINGLE
}
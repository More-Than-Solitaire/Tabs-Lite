package com.gbros.tabslite.data.tabcontent

// wrapper class to indicate whether a given line is a single line that can be word-wrapped and processed
// by the system normally, or if it's a double line (chords and lyrics) that we'll have to wrap separately
interface TabLine {
    val lineType: LineType

    enum class LineType {
        SINGLE, DOUBLE
    }
}
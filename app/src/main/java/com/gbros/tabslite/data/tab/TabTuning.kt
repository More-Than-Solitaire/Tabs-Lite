package com.gbros.tabslite.data.tab

enum class TabTuning {
    Standard,
    DropD,
    Celtic,
    OpenG,
    OpenD,
    HalfStepDown,
    DoubleDropD,
    OpenC;

    /**
     * Returns the string representation of the tuning, e.g. "E A D G B E" for [Standard].
     * Not URL safe (some strings contain `#`).
     */
    override fun toString(): String {
        return when (this) {
            Standard -> "E A D G B E"
            DropD -> "D A D G B E"
            Celtic -> "D A D G A D"
            OpenG -> "D G D G B D"
            OpenD -> "D A D F# A D"
            HalfStepDown -> "D# G# C# F# A# D#"
            DoubleDropD -> "D A D G B D"
            OpenC -> "C G C G C E"
        }
    }
}
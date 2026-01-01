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

    companion object {
        /**
         * Converts a string representation of a tuning to a [TabTuning].
         *
         * @param tuning The string representation of the tuning.
         * @return The [TabTuning] corresponding to the string representation.
         * @throws IllegalArgumentException If the string representation is invalid.
         */
        fun fromString(tuning: String): TabTuning {
            return when (tuning) {
                "Standard" -> Standard
                "E A D G B E" -> Standard
                "DropD" -> DropD
                "D A D G B E" -> DropD
                "Celtic" -> Celtic
                "D A D G A D" -> Celtic
                "OpenG" -> OpenG
                "D G D G B D" -> OpenG
                "OpenD" -> OpenD
                "D A D F# A D" -> OpenD
                "HalfStepDown" -> HalfStepDown
                "D# G# C# F# A# D#" -> HalfStepDown
                "DoubleDropD" -> DoubleDropD
                "D A D G B D" -> DoubleDropD
                "OpenC" -> OpenC
                "C G C G C E" -> OpenC
                else -> throw IllegalArgumentException("Invalid tuning string: $tuning")
            }
        }
    }
}
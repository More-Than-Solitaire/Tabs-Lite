package com.gbros.tabslite.data.chord

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs

private const val LOG_NAME = "tabslite.ICompleteChord"

abstract class ICompleteChord(val chordName: String, initialValue: List<ChordVariation> = emptyList()) : LiveData<List<ChordVariation>>() {
    companion object {
        // static methods
        // region public methods

        fun transposeChord(chord: CharSequence, halfSteps: Int): String {
            val numSteps = abs(halfSteps)
            val up = halfSteps > 0

            val chordParts = chord.split('/').toTypedArray()  // handle chords with a base note like G/B
            for (i in chordParts.indices) {
                if (chordParts[i] != "") {
                    if (up) {
                        // transpose up
                        for (j in 0 until numSteps) {
                            chordParts[i] = transposeUp(chordParts[i])
                        }
                    } else {
                        // transpose down
                        for (j in 0 until numSteps) {
                            chordParts[i] = transposeDown(chordParts[i])
                        }
                    }
                }
            }

            return chordParts.joinToString("/")
        }

        // endregion

        // region private methods

        /**
         * Helper function to transpose a chord name up by one half step
         *
         * @param text: The chord name (e.g. A#m7) to transpose (e.g. Bm7)
         */
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

        /**
         * Helper function to transpose a chord name down by one half step
         *
         * @param text: The chord name (e.g. A#m7) to transpose (e.g. Am7)
         */
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

        // endregion

    }

    // region public data

    /**
     * Tracks the progress of this chord's loading.  If it's being loaded from the internet, will be
     * false until the loading is complete.  Once it's true, if the value is still empty, no such
     * chord could be loaded
     */
    val loadingComplete: MutableLiveData<Boolean> = MutableLiveData(false)

    // endregion

    // region init

    init {
        if (initialValue.isNotEmpty()) {
            value = initialValue
        }
    }

    // endregion

}
package com.gbros.tabslite.data.chord

import android.util.Log
import com.gbros.tabslite.data.DataAccess
import com.gbros.tabslite.utilities.TAG
import com.gbros.tabslite.utilities.UgApi
import kotlin.math.abs

object Chord {
    // region public methods

    suspend fun ensureAllChordsDownloaded(chords: List<String>, dataAccess: DataAccess) {
        // find chords that aren't in the database
        val alreadyDownloadedChords = dataAccess.findAll(chords)
        val chordsToDownload = chords.filter { usedChord -> !alreadyDownloadedChords.contains(usedChord) }

        // download
        if (chordsToDownload.isNotEmpty()) {
            UgApi.updateChordVariations(chordsToDownload, dataAccess)
        }
    }

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

    suspend fun getChord(chord: String, dataAccess: DataAccess) {
        dataAccess.getChordVariations(chord).ifEmpty {
            UgApi.updateChordVariations(listOf(chord), dataAccess).getOrDefault(chord, listOf())
        }
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
                Log.e(TAG, "Weird Chord not transposed: $text")
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
                Log.e(TAG, "Weird Chord not transposed: $text")
                text
            }
        }
    }

    // endregion
}
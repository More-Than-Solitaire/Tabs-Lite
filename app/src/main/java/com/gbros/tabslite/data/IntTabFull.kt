package com.gbros.tabslite.data

import android.os.Parcelable
import android.util.Log
import kotlin.math.abs

private const val LOG_NAME = "tabslite.IntTabFull    "

interface IntTabFull: Parcelable {
    val tabId: Int
    val type: String
    val part: String
    val version: Int
    val votes: Int
    val rating: Double
    val date: Int
    val status: String
    val presetId: Int
    val tabAccessType: String
    val tpVersion: Int
    var tonalityName: String
    val versionDescription: String

    val songId: Int
    val songName: String
    val artistName: String
    val isVerified: Boolean
    val numVersions: Int

    // in JSON these are in a separate sublevel "recording"
    val recordingIsAcoustic: Boolean
    val recordingTonalityName: String
    val recordingPerformance: String
    val recordingArtists: ArrayList<String>

    var recommended: ArrayList<String>
    var userRating: Int
    var difficulty: String
    var tuning: String
    var capo: Int
    var urlWeb: String
    var strumming: ArrayList<String>
    var videosCount: Int
    var proBrother: Int
    var contributorUserId: Int
    var contributorUserName: String
    var content: String

    fun getCapoText(): String {
        return when {
            capo == 0 -> "None"
            capo in 11..13 -> "${capo}th Fret" // 11th, 12th, 13th are exceptions
            capo % 10 == 1 -> "${capo}st Fret"
            capo % 10 == 2 -> "${capo}nd Fret"
            capo % 10 == 3 -> "${capo}rd Fret"
            else -> "${capo}th Fret"
        }
    }

    fun getUrl(): String{
        // only allowed chars are alphanumeric and dash.
        var url = "https://tabslite.com/tab/"
        url += tabId.toString()
        return url
    }

    fun transpose(halfSteps: Int) {
        tonalityName = transposeChord(tonalityName, halfSteps)
        val chordPattern = Regex("\\[ch](.*?)\\[/ch]")
        val transposedContent = chordPattern.replace(this.content) {
            val chord = it.groupValues[1]
            "[ch]" + transposeChord(chord, halfSteps) + "[/ch]"
        }
        Log.d(LOG_NAME, "finished transposing $halfSteps: $transposedContent")

        content = transposedContent
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
}
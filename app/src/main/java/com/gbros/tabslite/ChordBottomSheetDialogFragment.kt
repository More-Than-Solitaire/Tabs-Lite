package com.gbros.tabslite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chrynan.chords.model.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gbros.tabslite.adapters.ChordPagerAdapter
import com.gbros.tabslite.data.ChordVariation
import kotlinx.android.synthetic.main.fragment_chord_bottom_sheet.*


class ChordBottomSheetDialogFragment: BottomSheetDialogFragment() {
    companion object {

        private const val KEY_CHORD = "parcelableChordWrapperKey"
        private const val DEFAULT_PEEK_HEIGHT = 400

        fun newInstance(chordVars: List<ChordVariation>) = ChordBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArray(KEY_CHORD, chordVars.toTypedArray())
            }
        }
    }

    private val chordVars by lazy { arguments?.getParcelableArray(KEY_CHORD) as Array<ChordVariation> }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chord_bottom_sheet, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chords = chordVars.map { cv -> cv.toChord() }
        pager.adapter = ChordPagerAdapter(this, chords)
        val chordNameTextView = view.findViewById<TextView>(R.id.chordTitleTextView)
        chordNameTextView.text = chords[0].name
    }

    private fun ChordVariation.toChord(): Chord {
        val markerSet = HashSet<ChordMarker>()

        for ((string, fretNumber) in this.frets.withIndex()) {
            if(fingers[string].toFinger() != Finger.UNKNOWN) {
                markerSet.add(when {
                    fretNumber > 0 -> {
                        ChordMarker.Note(fret = FretNumber(fretNumber), string = StringNumber(string + 1), finger = fingers[string].toFinger())
                    }
                    fretNumber == 0 -> {
                        ChordMarker.Open(StringNumber(string + 1))
                    }  // open string
                    else -> {
                        ChordMarker.Muted(StringNumber(string + 1))
                    }            // muted string
                })
            }
        }

        for (bar in this.listCapos) {
            val myMarker = ChordMarker.Bar(fret = FretNumber(bar.fret), startString = StringNumber(bar.startString + 1),
                    endString = StringNumber(bar.lastString), finger = bar.finger.toFinger())
            markerSet.add(myMarker)
        }

        return Chord(this.chordId, markerSet)
    }
    private fun Int.toFinger(): Finger {
        return when(this){
            1 -> Finger.INDEX
            2 -> Finger.MIDDLE
            3 -> Finger.RING
            4 -> Finger.PINKY
            5 -> Finger.THUMB
            else -> Finger.UNKNOWN
        }
    }
}
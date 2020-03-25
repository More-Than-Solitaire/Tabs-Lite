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
        val chords = ArrayList<Chord>()

        for (chord in chordVars){
            val markerSet = HashSet<ChordMarker>()
            markerSet.addAll(chord.noteChordMarkers)
            markerSet.addAll(chord.openChordMarkers)
            markerSet.addAll(chord.mutedChordMarkers)
            markerSet.addAll(chord.barChordMarkers)

            chords.add(Chord(chord.chordId, markerSet))
        }

        pager.adapter = ChordPagerAdapter(this, chords)
        val chordNameTextView = view.findViewById<TextView>(R.id.chordTitleTextView)
        chordNameTextView.text = chords[0].name
    }
}
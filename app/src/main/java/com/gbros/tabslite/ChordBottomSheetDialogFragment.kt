package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chrynan.chords.model.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gbros.tabslite.adapters.ChordPagerAdapter
import com.gbros.tabslite.data.ChordVariation
import com.gbros.tabslite.databinding.FragmentChordBinding
import com.gbros.tabslite.databinding.FragmentChordBottomSheetBinding


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
        val binding = FragmentChordBottomSheetBinding.inflate(inflater, container, false)

        val chords = ArrayList<Chord>()

        for (chord in chordVars){
            val markerSet = HashSet<ChordMarker>()
            markerSet.addAll(chord.noteChordMarkers)
            markerSet.addAll(chord.openChordMarkers)
            markerSet.addAll(chord.mutedChordMarkers)
            markerSet.addAll(chord.barChordMarkers)

            chords.add(Chord(chord.chordId, markerSet))
        }

        binding.pager.adapter = ChordPagerAdapter(this, chords)

        val chordNameTextView = view?.findViewById<TextView>(R.id.chordTitleTextView)
        if(chords.size > 0) {
            if (chordNameTextView != null) {
                chordNameTextView.text = chords[0].name
            }
        } else {
            val chordVarsSize = chordVars.size
            Log.e(javaClass.simpleName, "Error starting chord view - chords size is 0.  chordVars size is $chordVarsSize ")
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
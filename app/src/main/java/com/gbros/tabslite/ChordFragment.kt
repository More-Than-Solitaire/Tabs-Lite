package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chrynan.chords.model.Chord
import com.chrynan.chords.model.ChordChart
import com.chrynan.chords.model.FretNumber
import com.chrynan.chords.util.getChord
import com.chrynan.chords.util.maxFret
import com.chrynan.chords.util.minFret
import com.chrynan.chords.util.putChord
import com.gbros.tabslite.databinding.FragmentChordBinding
import java.lang.StrictMath.max
import java.lang.StrictMath.min

private const val LOG_NAME = "tabslite.ChordFragment "

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val chord: Chord by lazy { arguments?.getChord(KEY_CHORD)!! }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.v(LOG_NAME, "Showing Chord $chord")

        val binding = FragmentChordBinding.inflate(inflater, container, false)
        binding.chordWidget.chord = chord   // set chord to show

        // set which frets are shown for this chord
        val defaultMaxFret = 3
        val defaultMinFret = 1
        val stdChart = ChordChart.STANDARD_TUNING_GUITAR_CHART
        val endFret = max(chord.maxFret, defaultMaxFret)                                 // last fret shown
        val startFret = min(chord.minFret, max(chord.maxFret - 2, defaultMinFret))    // first fret shown
        binding.chordWidget.chart = stdChart.copy(fretEnd = FretNumber(endFret), fretStart = FretNumber(startFret))

        return binding.root
    }

    companion object {
        private const val KEY_CHORD = "parcelableChordWrapperKey"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param chord Chord to display.
         * @return A new instance of fragment ChordFragment.
         */
        // TODO: maybe make this take a chord id?
        @JvmStatic
        fun newInstance(chord: Chord) =
                ChordFragment().apply {
                    arguments = Bundle().apply {
                        putChord(KEY_CHORD, chord)
                    }
                }
    }
}

package com.gbros.tabslite.adapters

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chrynan.chords.model.Chord
import com.gbros.tabslite.ChordFragment
import com.gbros.tabslite.data.ChordVariation
import com.gbros.tabslite.data.IntSong

class ChordPagerAdapter(fragment: Fragment, val chords: List<Chord>) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private fun getTabFragments(): Map<Int, () -> Fragment> {
        val map = HashMap<Int, ()->Fragment>()

        for((i, chord) in chords.withIndex()) {
            map[i] = { ChordFragment.newInstance(chord) }
        }

        return map
    }

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = getTabFragments()  // only call once

    override fun getItemCount() = chords.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
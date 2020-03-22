package com.gbros.tabslite.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gbros.tabslite.FavoriteTabsFragment

const val FAVORITE_TABS_PAGE_INDEX = 0
const val SUGGESTED_TABS_PAGE_INDEX = 1

class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        FAVORITE_TABS_PAGE_INDEX to { FavoriteTabsFragment() },
        SUGGESTED_TABS_PAGE_INDEX to { FavoriteTabsFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}
package com.gbros.tabslite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.gbros.tabslite.adapters.FAVORITE_TABS_PAGE_INDEX
import com.gbros.tabslite.adapters.SUGGESTED_TABS_PAGE_INDEX
import com.gbros.tabslite.adapters.PagerAdapter
import com.gbros.tabslite.databinding.FragmentViewPagerBinding

class HomeViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        viewPager.adapter = PagerAdapter(this)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            FAVORITE_TABS_PAGE_INDEX -> R.drawable.garden_tab_selector
            SUGGESTED_TABS_PAGE_INDEX -> R.drawable.suggested_tabs_tab_selector
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            FAVORITE_TABS_PAGE_INDEX -> getString(R.string.fav_tabs_title)
            SUGGESTED_TABS_PAGE_INDEX -> getString(R.string.suggested_tabs_title)
            else -> null
        }
    }


}
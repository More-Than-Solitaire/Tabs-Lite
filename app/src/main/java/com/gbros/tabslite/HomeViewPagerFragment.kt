package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.gbros.tabslite.adapters.FAVORITE_TABS_PAGE_INDEX
import com.gbros.tabslite.adapters.TOP_TABS_PAGE_INDEX
import com.gbros.tabslite.adapters.PLAYLISTS_PAGE_INDEX
import com.gbros.tabslite.adapters.PagerAdapter
import com.gbros.tabslite.databinding.FragmentViewPagerBinding
import com.gbros.tabslite.workers.SearchHelper

private const val LOG_NAME = "tabslite.HomeViewPagerF"

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(LOG_NAME, "************************************************ options menu created")
        val searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem.actionView
        SearchHelper.initializeSearchBar("", searchView as SearchView, requireContext(), viewLifecycleOwner, {q ->
            Log.i(LOG_NAME, "Starting search from Home for '$q'")
            val direction = HomeViewPagerFragmentDirections.actionViewPagerFragmentToSearchResultFragment(q)
            view?.findNavController()?.navigate(direction)
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            FAVORITE_TABS_PAGE_INDEX -> R.drawable.garden_tab_selector
            TOP_TABS_PAGE_INDEX -> R.drawable.suggested_tabs_tab_selector
            PLAYLISTS_PAGE_INDEX -> R.drawable.playlists_selector
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            FAVORITE_TABS_PAGE_INDEX -> getString(R.string.fav_tabs_title)
            TOP_TABS_PAGE_INDEX -> getString(R.string.suggested_tabs_title)
            PLAYLISTS_PAGE_INDEX -> getString(R.string.playlists_section_title)
            else -> null
        }
    }
}
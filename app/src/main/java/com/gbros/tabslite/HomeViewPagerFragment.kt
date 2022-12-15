package com.gbros.tabslite

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import com.gbros.tabslite.adapters.FAVORITE_TABS_PAGE_INDEX
import com.gbros.tabslite.adapters.PLAYLISTS_PAGE_INDEX
import com.gbros.tabslite.adapters.PagerAdapter
import com.gbros.tabslite.adapters.TOP_TABS_PAGE_INDEX
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.databinding.FragmentViewPagerBinding
import com.gbros.tabslite.workers.SearchHelper
import com.gbros.tabslite.workers.UgApi
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val LOG_NAME = "tabslite.HomeViewPagerF"

class HomeViewPagerFragment : Fragment() {

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            hideKeyboard()
            if (::appBarLayout.isInitialized) {
                appBarLayout.setExpanded(true)
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) { }

        override fun onTabUnselected(tab: TabLayout.Tab?) { }
    }

    private lateinit var appBarLayout: AppBarLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // update the top tabs list
        val appDatabase = AppDatabase.getInstance(requireContext())
        GlobalScope.launch { UgApi.fetchTopTabs(appDatabase, true) }

        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager
        appBarLayout = binding.appBarLayout

        viewPager.adapter = PagerAdapter(this)
        tabLayout.addOnTabSelectedListener(onTabSelectedListener)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.search -> {
                    val searchView = menuItem.actionView as SearchView
                        searchView.findViewTreeLifecycleOwner()?.let {
                            SearchHelper.initializeSearchBar(
                                "",
                                searchView,
                                requireContext(),
                                it
                            ) { q ->
                                Log.i(LOG_NAME, "Starting search from Home for '$q'")
                                val direction =
                                    HomeViewPagerFragmentDirections.actionViewPagerFragmentToSearchResultFragment(
                                        q
                                    )
                                view.findNavController().navigate(direction)
                            }
                        }

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

    private fun hideKeyboard() {
        activity?.apply {
            currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }
}
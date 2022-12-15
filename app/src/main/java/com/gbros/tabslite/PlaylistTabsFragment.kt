package com.gbros.tabslite

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gbros.tabslite.adapters.BrowseTabsAdapter
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.databinding.FragmentBrowseTabsBinding
import com.gbros.tabslite.utilities.FAVORITE_TABS_SORTING_PREF_NAME
import com.gbros.tabslite.utilities.PREFS_NAME
import com.gbros.tabslite.viewmodels.PlaylistTabsListViewModel
import com.gbros.tabslite.viewmodels.PlaylistTabsViewModelFactory

private const val LOG_NAME = "tabslite.FavoriteTabsFr"

class PlaylistTabsFragment(playlistId: Int) : Fragment() {

    private lateinit var binding: FragmentBrowseTabsBinding

    private val viewModel: PlaylistTabsListViewModel by viewModels {
        PlaylistTabsViewModelFactory(AppDatabase.getInstance(requireContext().applicationContext).tabFullDao(), playlistId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBrowseTabsBinding.inflate(inflater, container, false)
        val adapter = BrowseTabsAdapter()
        binding.favoriteTabsList.adapter = adapter

        binding.findNewSongs.setOnClickListener {
            (activity as HomeActivity).focusSearch()
        }
        binding.swipeRefresh.isEnabled = false

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        subscribeUi(binding)
    }

    private fun subscribeUi(binding: FragmentBrowseTabsBinding) {
        val adapter = binding.favoriteTabsList.adapter as BrowseTabsAdapter

        viewModel.tabList.observe(viewLifecycleOwner) { tabs ->
            binding.hasHistory = !tabs.isNullOrEmpty()

            activity?.application?.apply {
                val storedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(
                    FAVORITE_TABS_SORTING_PREF_NAME,
                    0
                )
                binding.sortBy.setSelection(storedPref)
                adapter.updateSorting(storedPref, tabs)

                binding.sortBy.onItemSelectedListener = adapter.getItemSelectedListener(activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

            } ?: run {
                // default action if context is somehow null
                adapter.submitList(tabs.sortedByDescending { t -> t.dateAdded })  // needed because 0 is the default selection, so the sort might not be called the first time
                Log.v(
                    LOG_NAME,
                    "Submitted new list of size ${tabs.size}.  Current adapter list size is now ${adapter.currentList.size}"
                )
            }
        }
    }
}

package com.gbros.tabslite

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.gbros.tabslite.adapters.BrowseTabsAdapter
import com.gbros.tabslite.data.IntTabBasic
import com.gbros.tabslite.data.TabBasic
import com.gbros.tabslite.databinding.FragmentBrowseTabsBinding
import com.gbros.tabslite.utilities.*
import com.gbros.tabslite.utilities.InjectorUtils
import com.gbros.tabslite.utilities.PREFS_NAME
import com.gbros.tabslite.viewmodels.FavoriteTabsListViewModel

private const val LOG_NAME = "tabslite.FavoriteTabsFragment"

class FavoriteTabsFragment : Fragment() {

    private lateinit var binding: FragmentBrowseTabsBinding

    private val viewModel: FavoriteTabsListViewModel by viewModels {
        InjectorUtils.provideFavoriteTabViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowseTabsBinding.inflate(inflater, container, false)
        val adapter = BrowseTabsAdapter()
        binding.favoriteTabsList.adapter = adapter

        binding.findNewSongs.setOnClickListener {
            (activity as HomeActivity).focusSearch()
        }

        binding.swipeRefresh.isEnabled = false

        subscribeUi(adapter, binding)
        return binding.root
    }

    private fun subscribeUi(adapter: BrowseTabsAdapter, binding: FragmentBrowseTabsBinding) {
        fun updateSorting(position: Int, list: List<IntTabBasic>) {
            Log.v(LOG_NAME, "Updating sorting to SortBy selection position $position")

            val sortedResult = when(position){
                0 -> list.sortedByDescending { t -> t.favoriteTime }  //todo: replace with date of adding to favorites
                1 -> list.sortedBy { t -> t.songName }
                2 -> list.sortedBy { t -> t.artistName }
                3 -> list.sortedByDescending { t -> t.votes }
                else -> list
            }

            adapter.submitList(sortedResult)
        }

        binding.sortBy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /**
             * Callback method to be invoked when the selection disappears from this
             * view. The selection can disappear for instance when touch is activated
             * or when the adapter becomes empty.
             *
             * @param parent The AdapterView that now contains no selected item.
             */
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            /**
             *
             * Callback method to be invoked when an item in this view has been
             * selected. This callback is invoked only when the newly selected
             * position is different from the previously selected position or if
             * there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the
             * data associated with the selected item.
             *
             * @param parent The AdapterView where the selection happened
             * @param view The view within the AdapterView that was clicked
             * @param position The position of the view in the adapter
             * @param id The row id of the item that is selected
             */
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateSorting(position, adapter.currentList)
                // save our spot for next run
                context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putInt(FAVORITE_TABS_SORTING_PREF_NAME, position)?.apply()
                        ?: Log.w(LOG_NAME, "Could not store FavoriteTabs SortBy preference ($position).")
            }
        }

        viewModel.favoriteTabs.observe(viewLifecycleOwner) { result ->
            binding.hasHistory = !result.isNullOrEmpty()

            context?.apply{
                val storedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(FAVORITE_TABS_SORTING_PREF_NAME, 0)
                Log.v(LOG_NAME, "Setting SortBy selection to stored value ($storedPref).")
                binding.sortBy.setSelection(storedPref)
                updateSorting(storedPref, result)
            } ?: run {
                // default action if context is somehow null
                adapter.submitList(result.sortedByDescending { t -> t.favoriteTime })  // needed because 0 is the default selection, so the sort might not be called the first time
                Log.v(LOG_NAME, "Submitted new list of size ${result.size}.  Current adapter list size is now ${adapter.currentList.size}")
            }
        }
    }
}

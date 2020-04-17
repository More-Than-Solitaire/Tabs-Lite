package com.gbros.tabslite

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
import com.gbros.tabslite.databinding.FragmentBrowseTabsBinding
import com.gbros.tabslite.utilities.InjectorUtils
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
        viewModel.favoriteTabs.observe(viewLifecycleOwner) { result ->
            binding.hasHistory = !result.isNullOrEmpty()
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
                    Log.v(LOG_NAME, "SortBy selection changed to $position")
                    val sortedResult = when(position){
                        0 -> result.sortedBy { t -> t.date }  //todo: replace with date of adding to favorites
                        1 -> result.sortedBy { t -> t.songName }
                        2 -> result.sortedBy { t -> t.artistName }
                        3 -> result.sortedBy { t -> t.votes }
                        else -> result
                    }

                    adapter.submitList(sortedResult)
                }
            }

            adapter.submitList(result)  // needed because 0 is the default selection, so the sort won't be called the first time
            binding.sortBy.setSelection(0)
        }
    }
}

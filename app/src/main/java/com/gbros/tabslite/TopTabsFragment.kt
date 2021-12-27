package com.gbros.tabslite

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.gbros.tabslite.adapters.BrowseTabsAdapter
import com.gbros.tabslite.databinding.FragmentBrowseTabsBinding
import com.gbros.tabslite.utilities.Utils
import com.gbros.tabslite.workers.UgApi
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

private const val LOG_NAME = "tabslite.TopTabsFragmen"

class TopTabsFragment : Fragment() {

    private lateinit var binding: FragmentBrowseTabsBinding
    val adapter = BrowseTabsAdapter()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBrowseTabsBinding.inflate(inflater, container, false)
        binding.favoriteTabsList.adapter = adapter
        binding.swipeRefresh.isEnabled = true
        binding.swipeRefresh.isRefreshing = true
        binding.swipeRefresh.setOnRefreshListener(refreshListener)

        binding.hasHistory = true // hide the No Tabs Here message until we've finished loading

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.findNewSongs.setOnClickListener {
            (activity as HomeActivity).focusSearch()
        }

        subscribeUi(adapter, binding)
    }

    private fun subscribeUi(adapter: BrowseTabsAdapter, binding: FragmentBrowseTabsBinding, force: Boolean = false) {
        val topTabsJob = GlobalScope.async { UgApi.getTopTabs(force) }
        binding.sortBy.setSelection(3) // sort by popularity

        topTabsJob.invokeOnCompletion { cause ->
            if(cause != null){
                //problems
                Log.w(javaClass.simpleName, "Error finding top tabs. GetTopTabs job returned non-null. " + cause.message, cause.cause)
                requireActivity().runOnUiThread {
                    binding.hasHistory = adapter.itemCount > 0  // unless we already have items here, show the No Tabs Here message
                    Handler().postDelayed({ binding.swipeRefresh.isRefreshing = false }, 700)
                    view?.let {
                        if (it.isAttachedToWindow) {
                            Snackbar.make(it,"You're not connected to the internet", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                val tabBasics = Utils.tabsToTabBasics(topTabsJob.getCompleted())
                if(activity != null) {
                    requireActivity().runOnUiThread {
                        binding.hasHistory = true

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
                                    0 -> tabBasics.sortedBy { t -> t.date }
                                    1 -> tabBasics.sortedBy { t -> t.songName }
                                    2 -> tabBasics.sortedBy { t -> t.artistName }
                                    3 -> tabBasics // it's already sorted by popularity
                                    else -> tabBasics
                                }

                                adapter.submitList(sortedResult)
                            }
                        }
                        adapter.submitList(tabBasics)
                        binding.swipeRefresh.isRefreshing = false  // visually indicate that we're done refreshing
                    }
                }
            }
        }


    }


    private val refreshListener = OnRefreshListener {
        // Your code to make your refresh action
        subscribeUi(adapter, binding, true)
    }
}

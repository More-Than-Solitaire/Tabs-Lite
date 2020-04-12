package com.gbros.tabslite

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.gbros.tabslite.adapters.BrowseTabsAdapter
import com.gbros.tabslite.databinding.FragmentBrowseTabsBinding
import com.gbros.tabslite.utilities.Utils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

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
        val topTabsJob = GlobalScope.async { (activity as HomeActivity).searchHelper?.api?.getTopTabs(force) }

        topTabsJob.invokeOnCompletion { cause ->
            if(cause != null){
                //problems
                Log.w(javaClass.simpleName, "Error finding top tabs. GetTopTabs job returned non-null. " + cause.message, cause.cause)
                requireActivity().runOnUiThread {
                    binding.hasHistory = adapter.itemCount > 0  // unless we already have items here, show the No Tabs Here message
                    Handler().postDelayed({ binding.swipeRefresh.isRefreshing = false }, 700)
                    view?.let { Snackbar.make(it, "You're not connected to the internet", Snackbar.LENGTH_SHORT).show() }
                }
            } else {
                val result = topTabsJob.getCompleted()
                val resultsNull = result == null
                val tabBasics = result?.let { Utils.tabsToTabBasics(it) }
                if(activity != null) {
                    requireActivity().runOnUiThread {
                        if (tabBasics != null) {
                            binding.hasHistory = true
                            adapter.submitList(tabBasics)
                        } else {
                            binding.hasHistory = false
                            Log.e(javaClass.simpleName, "Error finding top tabs.  GetTopTabs completed, but tabBasics were null.  Results were? ($resultsNull) null as well. ")
                        }
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

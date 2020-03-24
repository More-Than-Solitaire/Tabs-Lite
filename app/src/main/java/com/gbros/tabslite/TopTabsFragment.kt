package com.gbros.tabslite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gbros.tabslite.adapters.BrowseTabsAdapter
import com.gbros.tabslite.databinding.FragmentBrowseTabsBinding
import com.gbros.tabslite.utilities.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class TopTabsFragment : Fragment() {

    private lateinit var binding: FragmentBrowseTabsBinding

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

        subscribeUi(adapter, binding)
        return binding.root
    }

    private fun subscribeUi(adapter: BrowseTabsAdapter, binding: FragmentBrowseTabsBinding) {
        val topTabsJob = GlobalScope.async { (activity as HomeActivity).searchHelper?.api?.getTopTabs() }

        topTabsJob.invokeOnCompletion { cause ->
            if(cause != null){
                //problems
                Log.e(javaClass.simpleName, "Error finding top tabs. GetTopTabs job returned non-null.", cause.cause)
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
                    }
                }
            }
        }
    }
}

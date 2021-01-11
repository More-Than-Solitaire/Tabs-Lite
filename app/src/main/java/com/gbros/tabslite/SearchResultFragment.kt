package com.gbros.tabslite

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isGone
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbros.tabslite.adapters.MySearchResultRecyclerViewAdapter
import com.gbros.tabslite.data.SearchRequestType
import com.gbros.tabslite.databinding.FragmentSearchResultListBinding
import com.gbros.tabslite.workers.SearchHelper
import com.gbros.tabslite.workers.UgApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

private const val LOG_NAME = "tabslite.SearchRsltsFra"

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SearchResultFragment.Callback] interface.
 */
class SearchResultFragment : Fragment() {
    var data: SearchRequestType = SearchRequestType()
    val callback = object : SearchResultFragment.Callback {
        override fun viewSongVersions(songId: Int) {
            val direction = SearchResultFragmentDirections.actionSearchResultFragmentToSongVersionFragment(data.getTabs(songId))
            view?.findNavController()?.navigate(direction)
        }
    }
    val adapter: MySearchResultRecyclerViewAdapter = MySearchResultRecyclerViewAdapter(callback)
    var lastPageExhausted = false  // whether or not we've gone through ALL the pages of search results yet.
    var searchPageNumber = 1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            if (activity is SearchResultsActivity) {
                Log.e(LOG_NAME, "SearchResultsFragment created via activity.  Should use fragment Navigation instead!")
            }

        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "Error in SearchResultFragment onActivityCreated", ex)
            throw ex
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            var query = ""
            arguments?.let {
                query = it.getString("query", "")
            }

            val binding = FragmentSearchResultListBinding.inflate(inflater, container, false)

            // start the search
            restartSearch(query, binding)

            // toolbar
            (activity as AppCompatActivity).let {
                it.setSupportActionBar(binding.toolbar)
                it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                it.supportActionBar?.setDisplayShowHomeEnabled(true)
            }

            SearchHelper.initializeSearchBar(query, binding.search, requireContext(), viewLifecycleOwner, { q ->
                Log.i(LOG_NAME, "Starting search for '$q'")
                restartSearch(q, binding)
            })


            binding.searchResultList.adapter = adapter

            // auto-continue search if we scroll to bottom
            binding.searchResultList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!lastPageExhausted
                            && binding.progressBar.isGone  // use this as a flag for whether we're done with the last set
                            && recyclerView.needsMoreItems()
                    ) {
                        // get more search results
                        searchNextPage(query, binding)
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            })

            binding.textView.isGone = true

            return binding.root
        } catch (ex: Exception) {
            Log.e(LOG_NAME, "Error in SearchResultFragment onCreateView", ex)
            throw ex
        }
    }

    private fun restartSearch(query: String, binding: FragmentSearchResultListBinding) {
        // reset settings
        lastPageExhausted = false
        searchPageNumber = 1
        data = SearchRequestType()
        // (binding.searchResultList.adapter as? MySearchResultRecyclerViewAdapter)?.submitList(emptyList())

        val searchJob = GlobalScope.async { UgApi.search(query) }
        searchJob.invokeOnCompletion(onSearchComplete(query, binding, searchJob))
    }

    private fun searchNextPage(query: String, binding: FragmentSearchResultListBinding) {
        binding.progressBar.isGone = false
        val searchJob = GlobalScope.async { UgApi.search(query, ++searchPageNumber) }
        searchJob.invokeOnCompletion(onSearchComplete(query, binding, searchJob))
    }

    private fun RecyclerView.needsMoreItems(): Boolean {
        val layoutManager = layoutManager as LinearLayoutManager
        return layoutManager.findLastVisibleItemPosition() + 3 > layoutManager.itemCount
    }

    private fun onSearchComplete(query: String, binding: FragmentSearchResultListBinding, searchJob: Deferred<SearchRequestType>) = { cause: Throwable? ->
        if(cause != null) {
            if(cause.message != null && cause.message!!.startsWith("search:")){
                lastPageExhausted = false
                searchPageNumber = 0
                val newQuery = cause.message!!.substring(7)  // no (more?) results for previous query; here's the new suggestion
                (activity as? AppCompatActivity)?.runOnUiThread {
                    Log.i(LOG_NAME, "Continuing search with query '$newQuery'")
                    searchNextPage(newQuery, binding)
                }
            } else {
                // search did not complete
                lastPageExhausted = true
                Log.i(LOG_NAME, "Reached end of search results.")
                (activity as? AppCompatActivity)?.runOnUiThread {
                    binding.progressBar.isGone = true
                    binding.textView.isGone = adapter.currentList.isNotEmpty()
                }
            }

            Unit
        } else {
//            if(activity is SearchResultsActivity) {
//                data.add((activity as SearchResultsActivity).searchJob.getCompleted())
//            }
            data.add(searchJob.getCompleted())
            val songs = data.getSongs()
            activity?.runOnUiThread(Runnable {
                if (songs.isNotEmpty()) {
                    (binding.searchResultList.adapter as MySearchResultRecyclerViewAdapter).submitList(songs)
                    binding.textView.isGone = true
                }

                // before we finish, just make sure our current page is full.
                if (binding.searchResultList.needsMoreItems()) {
                    searchNextPage(query, binding)
                } else {
                    binding.progressBar.isGone = true
                }
            })

            Unit
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface Callback {
        fun viewSongVersions(songId: Int)
    }
}

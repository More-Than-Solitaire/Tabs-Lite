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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

private const val LOG_NAME = "tabslite.SearchRsltsFra"

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SearchResultFragment.Callback] interface.
 */
class SearchResultFragment : Fragment() {
    private var searchHelper: SearchHelper? = null
    lateinit var query: String
    lateinit var binding: FragmentSearchResultListBinding
    var data: SearchRequestType = SearchRequestType()
    val callback = object : SearchResultFragment.Callback {
        override fun viewSongVersions(songId: Int) {
            val direction = SearchResultFragmentDirections.actionSearchResultFragmentToSongVersionFragment(data.getTabs(songId))
            view?.findNavController()?.navigate(direction)
        }
    }
    val adapter: MySearchResultRecyclerViewAdapter = MySearchResultRecyclerViewAdapter(callback as Callback)

    var lastPageExhausted = false  // whether or not we've gone through ALL the pages of search results yet.
    var searchPageNumber = 1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            //searchPageNumber = 1  // when we get to this page from a back button, we need to reset which page of search results we're looking at

            searchHelper = (activity as SearchResultsActivity).searchHelper
            if ((activity as SearchResultsActivity).query != null) {
                query = (activity as SearchResultsActivity).query!!
                Log.i(LOG_NAME, "SearchResultsFragment created for query $query")
            } else {
                Log.e(LOG_NAME, "Creating Search Result Fragment without a query!  This should not happen.")
            }


            //toolbar
            (activity as AppCompatActivity).let {
                it.setSupportActionBar(binding.toolbar)
                it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                it.supportActionBar?.setDisplayShowHomeEnabled(true)
            }

            initializeSearchBar()

            (activity as SearchResultsActivity).searchJob.invokeOnCompletion(onSearchComplete())  // when search completes
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "Error in SearchResultFragment onActivityCreated", ex)
            throw ex
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            binding = FragmentSearchResultListBinding.inflate(inflater, container, false)
            binding.searchResultList.adapter = adapter
            binding.searchResultList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!lastPageExhausted
                            && binding.progressBar.isGone  // use this as a flag for whether we're done with the last set
                            && recyclerView.needsMoreItems()
                    ) {
                        // get more search results
                        searchNextPage()
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

    private fun searchNextPage() {
        binding.progressBar.isGone = false
        (activity as SearchResultsActivity).searchJob = GlobalScope.async { UgApi.search(query, ++searchPageNumber) }
        (activity as SearchResultsActivity).searchJob.start()
        (activity as SearchResultsActivity).searchJob.invokeOnCompletion(onSearchComplete())
    }

    private fun RecyclerView.needsMoreItems(): Boolean {
        val layoutManager = layoutManager as LinearLayoutManager
        return layoutManager.findLastVisibleItemPosition() + 3 > layoutManager.itemCount
    }

    private fun initializeSearchBar(){
        //setup search bar
        val searchManager = (activity as AppCompatActivity).getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.search.setSearchableInfo(searchManager.getSearchableInfo(ComponentName((activity as AppCompatActivity), (activity as AppCompatActivity).javaClass)))
        binding.search.isIconified = false
        binding.search.setQuery(query, false)
        binding.search.clearFocus()

        // don't allow stacking of search activities.  If we search again, get rid of this instance
        binding.search.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                searchHelper!!.updateSuggestions(newText) //update the suggestions
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                (activity as AppCompatActivity).finish()    // finish this activity
                return false // tell the searchview that we didn't handle the search so it still calls another search
            }

        })

        //set up search suggestions
        binding.search.suggestionsAdapter = searchHelper!!.mAdapter;
        val onSuggestionListener = object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor: Cursor = searchHelper!!.mAdapter.getItem(position) as Cursor
                val txt: String = cursor.getString(cursor.getColumnIndex("suggestion"))
                binding.search.setQuery(txt, true)
                return true
            }

            // todo: what does this mean?
            override fun onSuggestionSelect(position: Int): Boolean {
                // Your code here
                return true
            }
        }
        binding.search.setOnSuggestionListener(onSuggestionListener)
    }

    private fun onSearchComplete() = { cause: Throwable? ->
        if(cause != null) {
            if(cause.message != null && cause.message!!.startsWith("search:")){
                lastPageExhausted = false
                searchPageNumber = 0
                query = cause.message!!.substring(7)
                (activity as? AppCompatActivity)?.runOnUiThread {
                    Log.i(LOG_NAME, "Continuing search with query '$query'")
                    searchNextPage()
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
            if(activity is SearchResultsActivity) {
                data.add((activity as SearchResultsActivity).searchJob.getCompleted())
            }
            val songs = data.getSongs()
            (activity as? SearchResultsActivity)?.runOnUiThread(Runnable {
                if (songs.isNotEmpty()) {
                    (binding.searchResultList.adapter as MySearchResultRecyclerViewAdapter).submitList(songs)
                    binding.textView.isGone = true
                }

                // before we finish, just make sure our current page is full.
                if (binding.searchResultList.needsMoreItems()) {
                    searchNextPage()
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

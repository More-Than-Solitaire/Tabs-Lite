package com.gbros.tabslite

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.gbros.tabslite.data.SearchRequestType
import com.gbros.tabslite.workers.SearchHelper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class SearchResultsActivity : AppCompatActivity(), ISearchHelper{

    override lateinit var searchHelper: SearchHelper
    lateinit var searchJob: Deferred<SearchRequestType>
    lateinit var getVersions: Deferred<Boolean>


    var query: String = ""
        private set(value) { field = value }

    override fun onCreate(savedInstanceState: Bundle?) {
        searchHelper = SearchHelper(this)
        super.onCreate(savedInstanceState)
        setContentView<com.gbros.tabslite.databinding.ActivitySearchResultsBinding>(
                this, R.layout.activity_search_results)
        handleIntent(intent)

        // start suggestion observer
        searchHelper.getSuggestionCursor().observe(this, searchHelper.suggestionObserver)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            query = intent.getStringExtra(SearchManager.QUERY)

            if(searchHelper.api == null) {
                Log.e(javaClass.simpleName, "Could not start search; UgApi instance was null.")
                return
            }
            searchJob = GlobalScope.async { searchHelper.api!!.search(query) }
            searchJob.start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

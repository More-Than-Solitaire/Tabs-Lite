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
import java.lang.Exception

class SearchResultsActivity : AppCompatActivity(), ISearchHelper{

    override var searchHelper: SearchHelper? = null
    lateinit var searchJob: Deferred<SearchRequestType>
    var getVersions: Deferred<Boolean>? = null

    var query: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView<com.gbros.tabslite.databinding.ActivitySearchResultsBinding>(
                    this, R.layout.activity_search_results)
            searchHelper = SearchHelper(this)
            handleIntent(intent)

            // start suggestion observer
            searchHelper!!.getSuggestionCursor().observe(this, searchHelper!!.suggestionObserver)
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "Error creating SearchResultsActivity", ex)
            throw ex
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            query = intent.getStringExtra(SearchManager.QUERY)

            if(query == null) {
                Log.e(javaClass.simpleName, "Could not start search; query was null")
            }
            if(searchHelper?.api == null) {
                Log.e(javaClass.simpleName, "Could not start search; UgApi instance was null.")
                return
            }
            searchJob = GlobalScope.async { searchHelper!!.api!!.search(query!!) }
            searchJob.start()
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressed(); return true }
}

package com.gbros.tabslite

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import com.gbros.tabslite.utilities.ApiHelper
import com.gbros.tabslite.workers.SearchHelper
import com.google.android.gms.instantapps.InstantApps
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class HomeActivity : AppCompatActivity(), ISearchHelper {
    override var searchHelper: SearchHelper? = null
    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem
    lateinit var updateJob: Deferred<String?>

    override fun onCreate(savedInstanceState: Bundle?) {
        updateJob = GlobalScope.async { ApiHelper.updateApiKey() }  // set the api key now before we need it
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        updateJob.invokeOnCompletion {
            val parentLayout: View = findViewById(android.R.id.content)
            if(updateJob.getCompleted() == null){
                Snackbar.make(parentLayout, "", Snackbar.LENGTH_SHORT)
            }
        }

        searchHelper = SearchHelper(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)

        if(com.google.android.gms.common.wrappers.InstantApps.isInstantApp(this)){
            menu.findItem(R.id.get_app).isVisible = true
        }

        implementSearch(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(item.itemId == R.id.dark_mode_toggle) {
            (application as DefaultApplication).darkModeDialog(this)  // show dialog asking user which mode they want
            true
        } else if(item.itemId == R.id.get_app) {
            val postInstall = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .setPackage("com.gbros.tabslite")
            InstantApps.showInstallPrompt(this, postInstall, 0, null)

            true
        } else {
            false // let someone else take care of this click
        }
    }

    private fun implementSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        searchView = searchMenuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // the search view is now open. add your logic if you want
                Handler().post(Runnable {
                    searchView.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(searchView.findFocus(), 0)
                })
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // the search view is closing. add your logic if you want
                return true
            }
        })

        // start suggestion observer
        searchHelper?.getSuggestionCursor()?.observe(this, searchHelper!!.suggestionObserver)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                Log.v(javaClass.simpleName, "Query text changed to '$newText' in HomeActivity.")
                searchHelper?.updateSuggestions(newText) //update the suggestions
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false // tell the searchview that we didn't handle the search so it still calls a search
            }

        })

        //set up search suggestions
        searchView.suggestionsAdapter = searchHelper?.mAdapter;
        val onSuggestionListener = object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor: Cursor = searchHelper?.mAdapter?.getItem(position) as Cursor
                val txt: String = cursor.getString(cursor.getColumnIndex("suggestion"))
                searchView.setQuery(txt, true)
                return true
            }

            // todo: what does this mean?
            override fun onSuggestionSelect(position: Int): Boolean {
                // Your code here
                return true
            }
        }
        searchView.setOnSuggestionListener(onSuggestionListener)
    }

    fun focusSearch() {
        searchView.requestFocusFromTouch()
        searchMenuItem.expandActionView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}


package com.gbros.tabslite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import com.gbros.tabslite.workers.SearchHelper
import com.google.android.gms.instantapps.InstantApps

private const val LOG_NAME = "tabslite.HomeActivity  "
// TODO: handle search intent

class HomeActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var searchMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        if(com.google.android.gms.common.wrappers.InstantApps.isInstantApp(this)){
            menu.findItem(R.id.get_app).isVisible = true
        }

        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchMenuItem = menu.findItem(R.id.search)
        SearchHelper.initializeSearchBar("", searchView, this, this) { q ->
            Log.i(LOG_NAME, "Starting search from Home for '$q'")

            //which navigation direction depends on which is the current fragment
            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host)
            navHost?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                    when (fragment.javaClass) {
                        TabDetailFragment::class.java -> {
                            val direction =
                                TabDetailFragmentDirections.actionTabDetailFragment2ToSearchResultFragment(
                                    q
                                )
                            findNavController(R.id.nav_host).navigate(direction)
                        }
                        HomeViewPagerFragment::class.java -> {
                            val direction =
                                HomeViewPagerFragmentDirections.actionViewPagerFragmentToSearchResultFragment(
                                    q
                                )
                            findNavController(R.id.nav_host).navigate(direction)
                        }
                        else -> {
                            Log.e(
                                LOG_NAME,
                                "Search directions not implemented for this class (${fragment.javaClass.canonicalName}}"
                            )
                        }
                    }
                }
            }
        }

        return super.onCreateOptionsMenu(menu)
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

    fun focusSearch() {
        if (::searchView.isInitialized && ::searchMenuItem.isInitialized) {
            searchView.requestFocusFromTouch()
            searchMenuItem.expandActionView()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}


package com.gbros.tabslite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.gbros.tabslite.workers.SearchHelper

import kotlinx.android.synthetic.main.activity_tab_detail.*

class TabDetailActivity : AppCompatActivity(), ISearchHelper {

    var tabId: Int = -1
    override var searchHelper: SearchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchHelper = SearchHelper(this)

        Log.d(javaClass.simpleName, "Tab Detail Activity Created")
        onNewIntent(intent)
        setContentView(R.layout.activity_tab_detail)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(javaClass.simpleName, "Intent received.")
        super.onNewIntent(intent)
        var data: String = intent!!.data!!.path!!

        // string processing to get the tab id
        if (data.contains('#')) {
            data = data.removeRange(data.indexOf('#') until data.length)  // remove anything after a # in the url
        }
        data = data.substring(data.indexOfLast { c -> c == '-' } + 1) // get the numbers after the last dash
        data = data.trim().trim('/', '-')

        tabId = data.toInt()
        Log.d(javaClass.simpleName, "Finished processing intent string for tab $tabId")
    }
}

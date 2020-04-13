package com.gbros.tabslite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.gbros.tabslite.workers.SearchHelper

import kotlinx.android.synthetic.main.activity_tab_detail.*

private const val LOG_NAME = "tabslite.TabDetailActivity"

class TabDetailActivity : AppCompatActivity(), ISearchHelper {

    var tabId: Int = -1
    var tsp: Int = 0  // transpose level
    override var searchHelper: SearchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchHelper = SearchHelper(this)

        Log.d(LOG_NAME, "Tab Detail Activity Created")
        onNewIntent(intent)
        setContentView(R.layout.activity_tab_detail)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var data: String = intent!!.data!!.toString()
        Log.d(LOG_NAME, "Intent received: $data")

        // string processing to get the tab id
        if (data.contains('#')) {

            if (data.contains("tsp='.*?'".toRegex())) {
                val start = data.indexOf("tsp='") + 5
                val end = data.indexOf('\'', start)
                tsp = data.subSequence(start until end).toString().toInt()
                Log.v(LOG_NAME, "Opening tab with transpose level set via link.  Tsp: '$tsp'")
            }

            data = data.removeRange(data.indexOf('#') until data.length)  // remove anything after a # in the url
        }
        data = data.trimEnd().trimEnd('/', '-')                         // get rid of anything at the end
        data = data.replace("[^\\d]".toRegex(), "-")                // any non digits become -
        data = data.substring(data.indexOfLast { c -> c == '-' } + 1) // get the numbers after the last dash

        tabId = data.toInt()
        Log.d(LOG_NAME, "Finished processing intent string for tab $tabId")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

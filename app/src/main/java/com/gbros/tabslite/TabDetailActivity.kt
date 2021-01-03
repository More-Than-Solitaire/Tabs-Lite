package com.gbros.tabslite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gbros.tabslite.workers.SearchHelper

private const val LOG_NAME = "tabslite.TabDetailActiv"

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
        val link: String = intent!!.data!!.toString()
        Log.d(LOG_NAME, "Intent received: $link")

        tsp = getTransposeLevel(link)
        tabId = getTabId(link)
        Log.d(LOG_NAME, "Finished processing intent string for tab $tabId")
    }

    private fun getTransposeLevel(link: String): Int {
        var transpose = 0
        Log.v(LOG_NAME, "Finding transpose level for link $link")

        if (link.contains('#')) {
            // set transposition level based on GET parameters of link
            if (link.contains("tsp='.*?'".toRegex())) {
                val start = link.indexOf("tsp='") + 5
                val end = link.indexOf('\'', start)
                transpose = link.subSequence(start until end).toString().toInt()
                Log.v(LOG_NAME, "Opening tab with transpose level set via link.  Tsp: '$tsp'")
            }
        }

        Log.v(LOG_NAME, "Transpose $transpose")
        return transpose
    }

    private fun getTabId(link: String): Int {
        Log.v(LOG_NAME, "Getting tabid from link $link")
        var tabid: String = link

        if (tabid.contains('?'))
            tabid = tabid.removeRange(tabid.indexOf('?') until tabid.length)
        if (tabid.contains('#'))
            tabid = tabid.removeRange(tabid.indexOf('#') until link.length)  // remove anything after a # in the url


        tabid = tabid.trimEnd().trimEnd('/', '-')                         // get rid of anything at the end
        tabid = tabid.replace("[^\\d]".toRegex(), "-")                // any non digits become -
        tabid = tabid.substring(tabid.indexOfLast { c -> c == '-' } + 1) // get the numbers after the last dash

        Log.v(LOG_NAME, "Found tabid of ${tabid.toInt()}")
        return tabid.toInt()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

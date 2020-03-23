package com.gbros.tabslite.data

import com.gbros.tabslite.utilities.Utils
import kotlin.collections.ArrayList

class SearchRequestType(var tabs: List<Tab>, var artists: List<String>){
    class Tab(var id: Int, var song_id: Int, var song_name: String, val artist_id: Int, var artist_name: String,
              var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0,
              var rating: Double = 0.0, var date: String = "", var status: String = "", var preset_id: Int = 0,
              var tab_access_type: String = "", var tp_version: Int = 0, var tonality_name: String = "",
              val version_description: String? = "", var verified: Int = 0,
              val recording: TabRequestType.RecordingInfo?
    )

    constructor() : this(ArrayList(), ArrayList())

    private lateinit var songs: LinkedHashMap<Int, ArrayList<Int>>  // songId, List<tabId>
    private lateinit var tabBasics: HashMap<Int, TabBasic>          // tabId, TabBasic

    private fun initSongs() {
        if(::songs.isInitialized) {
            return
        }

        songs = LinkedHashMap()
        indexNewSongs(tabs)
    }
    private fun initTabs() {
        if(::tabBasics.isInitialized) {
            return
        }

        tabBasics = HashMap()
        indexNewTabs(tabs)
    }

    private fun indexNewSongs(newTabs: List<Tab>) {
        for (tab: Tab in newTabs) {
            if(!songs.containsKey(tab.song_id)){
                songs.put(tab.song_id, ArrayList())
            }

            songs[tab.song_id]!!.add(tab.id)
        }
    }
    private fun indexNewTabs(newTabs: List<Tab>){
        initSongs()
        indexNewSongs(newTabs)

        val tbs = Utils.tabsToTabBasics(newTabs)

        for (tb in tbs) {
            if (songs[tb.songId]?.size != null){
                tb.numVersions = songs[tb.songId]?.size!!
            }
            tabBasics[tb.tabId] = tb
        }
    }

    fun getTab(tabId: Int): TabBasic? {
        initTabs()
        return tabBasics[tabId]
    }

    fun getTabBasics(): List<TabBasic> {
        initTabs()
        return ArrayList(tabBasics.values)
    }

    fun getTabIds(songId: Int): List<Int>? {
        initSongs()
        return songs[songId]
    }

    fun getTabs(songId: Int): Array<TabBasic> {
        initTabs()
        if(songs[songId] == null){
            return emptyArray()
        }

        val result = ArrayList<TabBasic>()
        for (tabId in getTabIds(songId)!!){
            result.add(getTab(tabId)!!)
        }

        return result.toTypedArray()
    }

    fun getSongs(): List<TabBasic> {
        initTabs()
        val result: ArrayList<TabBasic> = ArrayList()

        for(tabIdList in songs.values){
            result.add(tabBasics[tabIdList[0]]!!)
        }
        return result
    }

    fun add(newResults: SearchRequestType){
        initTabs()
        artists = ArrayList(artists) + ArrayList(newResults.artists)
        tabs = ArrayList(tabs) + ArrayList(newResults.tabs)

        indexNewTabs(newResults.tabs)
    }
}
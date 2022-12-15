package com.gbros.tabslite.data.servertypes

import com.gbros.tabslite.data.Tab
import com.gbros.tabslite.data.tabcontent.TabRequestType

class SearchRequestType(var tabs: List<SearchResultTab>, var artists: List<String>){
    class SearchResultTab(var id: Int, var song_id: Int, var song_name: String, val artist_id: Int, var artist_name: String,
                          var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0,
                          var rating: Double = 0.0, var date: String = "", var status: String = "", var preset_id: Int = 0,
                          var tab_access_type: String = "", var tp_version: Int = 0, var tonality_name: String = "",
                          val version_description: String? = "", var verified: Int = 0,
                          val recording: TabRequestType.RecordingInfo?
    ) {
        fun tabFull(): com.gbros.tabslite.data.Tab {
            val dateToUse = if (date.isNullOrEmpty()) 0 else date.toInt()
            val versionDscToUse = if (version_description.isNullOrEmpty()) "" else version_description
            val recordingAcoustic = if (recording != null) recording.is_acoustic == 1 else false
            val recordingTonality = recording?.tonality_name ?: ""
            val recordingPerformance = recording?.performance.toString()
            val recordingArtists = recording?.getArtists() ?: ArrayList()

            return Tab(
                id,
                song_id,
                song_name,
                artist_name,
                type,
                part,
                version,
                votes,
                rating,
                dateToUse,
                status,
                preset_id,
                tab_access_type,
                tp_version,
                tonality_name,
                versionDscToUse,
                verified == 1,
                recordingAcoustic,
                recordingTonality,
                recordingPerformance,
                recordingArtists
            )
        }
    }

    constructor() : this(ArrayList(), ArrayList())

    private lateinit var songs: LinkedHashMap<Int, HashSet<Int>>  // songId, List<tabId>
    private lateinit var tabFulls: HashMap<Int, com.gbros.tabslite.data.Tab>          // tabId, TabBasic

    private fun initSongs() {
        if(::songs.isInitialized) {
            return
        }

        songs = LinkedHashMap()
        indexNewSongs(tabs)
    }
    private fun initTabs() {
        if(::tabFulls.isInitialized) {
            return
        }

        tabFulls = HashMap()
        indexNewTabs(tabs)
    }

    private fun indexNewSongs(newTabs: List<SearchResultTab>) {
        for (tab: SearchResultTab in newTabs) {
            if(!songs.containsKey(tab.song_id)){
                songs.put(tab.song_id, HashSet())
            }

            songs[tab.song_id]!!.add(tab.id)
        }
    }
    private fun indexNewTabs(newTabs: List<SearchResultTab>){
        initSongs()
        indexNewSongs(newTabs)

        val tabs: ArrayList<com.gbros.tabslite.data.Tab> = ArrayList()
        for (srTab in newTabs) {
            tabs.add(srTab.tabFull())
        }

        for (tb in tabs) {
            if (songs[tb.songId]?.size != null){
                tb.numVersions = songs[tb.songId]?.size!!
            }
            tabFulls[tb.tabId] = tb
        }
    }

    private fun getTab(tabId: Int): com.gbros.tabslite.data.Tab? {
        initTabs()
        return tabFulls[tabId]
    }

    private fun getTabIds(songId: Int): Set<Int>? {
        initSongs()
        return songs[songId]
    }

    fun getTabs(songId: Int): Array<com.gbros.tabslite.data.Tab> {
        initTabs()
        if(songs[songId] == null){
            return emptyArray()
        }

        val result = ArrayList<com.gbros.tabslite.data.Tab>()
        for (tabId in getTabIds(songId)!!){
            result.add(getTab(tabId)!!)
        }

        return result.toTypedArray()
    }

    fun getSongs(): List<com.gbros.tabslite.data.Tab> {
        initTabs()
        val result: ArrayList<com.gbros.tabslite.data.Tab> = ArrayList()

        for(tabIdList in songs.values){
            result.add(tabFulls[tabIdList.first()]!!)  // add the first tab for each song
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
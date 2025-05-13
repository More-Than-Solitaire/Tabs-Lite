package com.gbros.tabslite.data.servertypes

import com.gbros.tabslite.data.tab.TabDataType

class SearchRequestType(private var tabs: List<SearchResultTab>, private var artists: List<String>){
    class SearchResultTab(var id: Int, var song_id: Int, var song_name: String, val artist_id: Int, var artist_name: String,
                          var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0,
                          var rating: Double = 0.0, var date: String = "", var status: String = "", var preset_id: Int = 0,
                          var tab_access_type: String = "", var tp_version: Int = 0, var tonality_name: String = "",
                          val version_description: String? = "", var verified: Int = 0,
                          val recording: TabRequestType.RecordingInfo?
    ) {
        fun tabFull(): TabDataType {
            val dateToUse = if (date.isNullOrEmpty()) 0 else date.toInt()
            val versionDscToUse = if (version_description.isNullOrEmpty()) "" else version_description
            val recordingAcoustic = if (recording != null) recording.is_acoustic == 1 else false
            val recordingTonality = recording?.tonality_name ?: ""
            val recordingPerformance = recording?.performance.toString()
            val recordingArtists = recording?.getArtists() ?: ArrayList()

            return TabDataType(
                tabId = id,
                songId = song_id,
                songName = song_name,
                artistName = artist_name,
                artistId = artist_id,
                type = type,
                part = part,
                version = version,
                votes = votes,
                rating = rating,
                date = dateToUse,
                status = status,
                presetId = preset_id,
                tabAccessType = tab_access_type,
                tpVersion = tp_version,
                tonalityName = tonality_name,
                versionDescription = versionDscToUse,
                isVerified = verified == 1,
                recordingIsAcoustic = recordingAcoustic,
                recordingTonalityName = recordingTonality,
                recordingPerformance = recordingPerformance,
                recordingArtists = recordingArtists
            )
        }
    }
    // region public data

    var didYouMean: String? = null

    // endregion

    constructor(didYouMean: String = "") : this(ArrayList(), ArrayList()) {
        this.didYouMean = didYouMean
    }

    // region private data

    private lateinit var songs: LinkedHashMap<Int, MutableList<Int>>  // songId, List<tabId>
    private lateinit var tabFulls: HashMap<Int, TabDataType>          // tabId, TabBasic

    // endregion

    // region public methods

    fun getAllTabs(): List<TabDataType> {
        return tabFulls.values.toList()
    }

    fun getSongs(): List<TabDataType> {
        initTabs()
        val result: ArrayList<TabDataType> = ArrayList()

        for(tabIdList in songs.values){
            result.add(tabFulls[tabIdList.first()]!!)  // add the first tab for each song
        }
        return result
    }

    // endregion

    // region private methods

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
                songs.put(tab.song_id, mutableListOf())
            }

            songs[tab.song_id]!!.add(tab.id)
        }
    }
    private fun indexNewTabs(newTabs: List<SearchResultTab>){
        initSongs()
        indexNewSongs(newTabs)

        val tabs: ArrayList<TabDataType> = ArrayList()
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

    private fun getTabIds(songId: Int): IntArray {
        initSongs()

        songs[songId]?.let { return it.toIntArray() }
        return intArrayOf()
    }

    // endregion
}
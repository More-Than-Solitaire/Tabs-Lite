package com.gbros.tabslite.data.servertypes

import com.gbros.tabslite.data.tab.TabDataType

class SearchRequestType(private var tabs: List<SearchResultTab>, private var artists: List<String>){
    class SearchResultTab(var id: Int, var song_id: Int, var song_name: String, val artist_id: Int, var artist_name: String,
                          var type: String = "", var part: String = "", var version: Int = 0, var votes: Int = 0,
                          var difficulty: String = "",var rating: Double = 0.0, var date: String? = "", var status: String = "",
                          var preset_id: Int, var tab_access_type: String = "", var tp_version: Int, var tonality_name: String = "",
                          val version_description: String? = "", var verified: Int = 0
    ) {
        fun tabFull(): TabDataType {
            val dateToUse = if (date.isNullOrEmpty()) 0 else date!!.toLong()
            val versionDscToUse = if (version_description.isNullOrEmpty()) "" else version_description

            return TabDataType(
                tabId = "7567-$id", // todo: fix when search returns prefixed ids
                songId = "7567-$song_id",
                songName = song_name,
                artistName = artist_name,
                artistId = "7567-$artist_id",
                type = type,
                part = part,
                version = version,
                votes = votes,
                rating = rating,
                date = dateToUse,
                status = status,
                tabAccessType = tab_access_type,
                tonalityName = tonality_name,
                versionDescription = versionDscToUse,
                isVerified = verified == 1,
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

    private lateinit var songs: LinkedHashMap<String, MutableList<String>>  // songId, List<tabId>
    private lateinit var tabFulls: HashMap<String, TabDataType>          // tabId, TabBasic

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

    private fun indexNewSongs(newSongs: List<SearchResultTab>) {
        for (song: SearchResultTab in newSongs) {
            if(!songs.containsKey("7567-${song.song_id}")){  // todo: remove 7567 when search results return prefixed ids
                songs["7567-${song.song_id}"] = mutableListOf()
            }

            songs["7567-${song.song_id}"]!!.add("7567-${song.id}")
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
                tb.versionsCount = songs[tb.songId]?.size!!
            }
            tabFulls[tb.tabId] = tb
        }
    }

    // endregion
}
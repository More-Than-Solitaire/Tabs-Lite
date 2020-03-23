package com.gbros.tabslite.utilities

import com.gbros.tabslite.data.SearchRequestType
import com.gbros.tabslite.data.TabBasic

object Utils {
    fun tabsToTabBasics(tabs: List<SearchRequestType.Tab>): List<TabBasic> {
        val result: ArrayList<TabBasic> = ArrayList()
        for (tab: SearchRequestType.Tab in tabs) {
            val tb = TabBasic(tabId = tab.id, songId = tab.song_id, songName = tab.song_name, artistName = tab.artist_name,
                    type = tab.type, part = tab.part, version = tab.version, votes = tab.votes, rating = tab.rating,
                    date = tab.date.toInt(), status = tab.status, presetId = tab.preset_id, tabAccessType = tab.tab_access_type,
                    tpVersion = tab.tp_version, tonalityName = tab.tonality_name,
                    isVerified = (tab.verified != 0))
            if (tab.recording != null) {
                tb.recordingArtists = tab.recording.getArtists()
                tb.recordingIsAcoustic = (tab.recording.is_acoustic != 0)
                tb.recordingPerformance = tab.recording.performance.toString()
                tb.recordingTonalityName = tab.recording.tonality_name
            } else {
                tb.recordingArtists = ArrayList(emptyList())
                tb.recordingIsAcoustic = false
                tb.recordingPerformance = ""
                tb.recordingTonalityName = ""
            }

            if (tab.version_description != null) {
                tb.versionDescription = tab.version_description
            } else {
                tb.versionDescription = ""
            }
            tb.numVersions = 1

            result.add(tb)
        }

        return result
    }
}
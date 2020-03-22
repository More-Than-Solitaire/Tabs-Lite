package com.gbros.tabslite.data

interface IntTabBasic : IntSong {
    val tabId: Int
    val type: String
    val part: String
    val version: Int
    val votes: Int
    val rating: Double
    val date: Int
    val status: String
    val presetId: Int
    val tabAccessType: String
    val tpVersion: Int
    val tonalityName: String
    val versionDescription: String

    // in JSON these are in a separate sublevel "recording"
    val recordingIsAcoustic: Boolean
    val recordingTonalityName: String
    val recordingPerformance: String
    val recordingArtists: ArrayList<String>
}
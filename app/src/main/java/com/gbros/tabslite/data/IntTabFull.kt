package com.gbros.tabslite.data

import android.os.Parcelable

interface IntTabFull: Parcelable {
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

    val songId: Int
    val songName: String
    val artistName: String
    val isVerified: Boolean
    val numVersions: Int

    // in JSON these are in a separate sublevel "recording"
    val recordingIsAcoustic: Boolean
    val recordingTonalityName: String
    val recordingPerformance: String
    val recordingArtists: ArrayList<String>

    var recommended: ArrayList<String>
    var userRating: Int
    var difficulty: String
    var tuning: String
    var capo: Int
    var urlWeb: String
    var strumming: ArrayList<String>
    var videosCount: Int
    var proBrother: Int
    var contributorUserId: Int
    var contributorUserName: String
    var content: String

    fun getCapoText(): String {
        return when (capo) {
            0 -> "None"
            1 -> "1st Fret"
            2 -> "2nd Fret"
            3 -> "3rd Fret"
            else -> capo.toString() + "th Fret"
        }
    }

    fun getUrl(): String{
        // only allowed chars are alphanumeric and dash.
        var url = "https://tabslite.com/tab/"
        url += tabId.toString()
        return url
    }
}
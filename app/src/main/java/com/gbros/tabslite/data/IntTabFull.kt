package com.gbros.tabslite.data

interface IntTabFull: IntTabBasic {
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
}
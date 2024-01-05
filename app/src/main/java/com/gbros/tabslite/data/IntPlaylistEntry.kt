package com.gbros.tabslite.data

import android.os.Parcelable

interface IntPlaylistEntry: Parcelable {
    val entryId: Int
    val playlistId: Int
    val tabId: Int
    val nextEntryId: Int?
    val prevEntryId: Int?
    val dateAdded: Long
    var transpose: Int

    fun transpose(halfSteps: Int) {
        transpose = (transpose + halfSteps) % 12
    }
}
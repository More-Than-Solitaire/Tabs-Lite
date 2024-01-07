package com.gbros.tabslite.data

interface IPlaylistEntry {
    val entryId: Int
    val playlistId: Int
    val tabId: Int
    val nextEntryId: Int?
    val prevEntryId: Int?
    val dateAdded: Long
    val transpose: Int
}
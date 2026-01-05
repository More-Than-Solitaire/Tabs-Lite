package com.gbros.tabslite.data.playlist

open class IDataPlaylistEntry(override var tabId: String, override val transpose: Int, open val entryId: Int, open val playlistId: Int, open val nextEntryId: Int?, open val prevEntryId: Int?, open val dateAdded: Long):
    IPlaylistEntry(tabId, transpose)

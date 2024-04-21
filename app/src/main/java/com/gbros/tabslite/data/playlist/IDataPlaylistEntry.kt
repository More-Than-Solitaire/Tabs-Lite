package com.gbros.tabslite.data.playlist

open class IDataPlaylistEntry(override val tabId: Int, override val transpose: Int, open val entryId: Int, open val playlistId: Int, open val nextEntryId: Int?, open val prevEntryId: Int?, open val dateAdded: Long):
    IPlaylistEntry(tabId, transpose)

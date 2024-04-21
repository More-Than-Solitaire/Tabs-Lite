package com.gbros.tabslite.data.playlist

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistFileExportType(val playlists: List<Playlist>, val entries: List<PlaylistEntry>)
package com.gbros.tabslite.data.servertypes

import kotlinx.serialization.Serializable

/**
 * A new song to be created in the database.
 */
@Serializable
data class SongRequestType(
    var song_id: String = "",
    var song_name: String = "",
    var artist_name: String = "",
    var artist_id: String = "",
    var song_genre: String = "",
    var total_votes: Int = 0,
    var versions_count: Int = 0,
    var status: String = "pending",
)

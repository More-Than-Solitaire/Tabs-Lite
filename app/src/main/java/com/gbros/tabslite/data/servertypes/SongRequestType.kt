package com.gbros.tabslite.data.servertypes

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * A new song to be created in the database.
 */
data class SongRequestType(
    var song_id: String = "",
    var song_name: String = "",
    var artist_name: String = "",
    var artist_id: String = "",
    var song_genre: String = "",
    var total_votes: Int = 0,
    var versions_count: Int = 0,
    var status: String = "pending",
    @ServerTimestamp var created_at: Date? = null
)

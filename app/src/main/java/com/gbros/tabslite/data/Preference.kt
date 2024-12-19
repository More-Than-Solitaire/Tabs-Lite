package com.gbros.tabslite.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "preferences"
)

/**
 * Store user preferences by name in the local database.
 */
data class Preference(
    /**
     * The name of the preference.  Usually stored in the Constants class.
     */
    @PrimaryKey @ColumnInfo(name = "name") var name: String,

    /**
     * The preference value (e.g. "true" or "a-z")
     */
    @ColumnInfo(name = "value") var value: String = "",
) {

    companion object {
        const val FAVORITES_SORT: String = "FAVORITES_SORT"
        const val POPULAR_SORT: String = "POPULAR_SORT"
        const val PLAYLIST_SORT: String = "PLAYLIST_SORT"
        const val AUTOSCROLL_DELAY: String = "AUTOSCROLL_DELAY"
    }
}
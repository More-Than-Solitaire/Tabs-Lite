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
        /**
         * The preference name for the user preference of which order the favorites playlist should
         * be ordered in
         */
        const val FAVORITES_SORT: String = "FAVORITES_SORT"

        /**
         * The preference name for which order the popular tabs playlist should be ordered in
         */
        const val POPULAR_SORT: String = "POPULAR_SORT"

        /**
         * The preference name for which order the user-created playlists should be sorted in
         */
        const val PLAYLIST_SORT: String = "PLAYLIST_SORT"

        /**
         * The preference name for the delay in ms between 1px scrolls during autoscroll
         */
        const val AUTOSCROLL_DELAY: String = "AUTOSCROLL_DELAY"

        /**
         * The preference name for which instrument to display chords for
         */
        const val INSTRUMENT: String = "INSTRUMENT"

        /**
         * The preference name for whether to use the flats forms of chords vs sharps
         */
        const val USE_FLATS: String = "USE_FLATS"

        /**
         * The preference name for the [ThemeSelection] to use
         */
        const val APP_THEME: String = "APP_THEME"
    }
}
package com.gbros.tabslite.compose.songlist

/**
 * The ways a song list can be sorted, and their string representations
 */
enum class SortBy {
    DateAdded,
    Name,
    ArtistName,
    Popularity;

    companion object {
        fun getString(entry: SortBy): String {
            return when(entry) {
                DateAdded -> "Date Added"
                Popularity -> "Popularity"
                ArtistName -> "Artist Name"
                Name -> "Title"
            }
        }
    }
}

package com.gbros.tabslite.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_suggestions"
)

/**
 * Store suggested searches by query in the local database
 */
data class SearchSuggestions (
    /**
     * The search query that these suggestions are for
     */
    @PrimaryKey val query: String,

    /**
     * The list of search suggestions for this query
     */
    @ColumnInfo(name = "suggested_searches") val suggestedSearches: List<String>
)

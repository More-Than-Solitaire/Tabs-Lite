package com.gbros.tabslite.data

import androidx.room.Dao
import androidx.room.Query

/**
 * The Data Access Object for the Preference class
 */
@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preference WHERE name = :name")
    suspend fun getPreference(name: String): Preference
}
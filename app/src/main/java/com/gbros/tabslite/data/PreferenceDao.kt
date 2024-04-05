package com.gbros.tabslite.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

/**
 * The Data Access Object for the Preference class
 */
@Dao
interface PreferenceDao {
    @Query("SELECT * FROM preferences WHERE name = :name")
    fun getLivePreference(name: String): LiveData<Preference>

    @Query("SELECT value FROM preferences WHERE name = :name")
    suspend fun getPreferenceValue(name: String): String

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pref: Preference)

    @Upsert
    suspend fun upsertPreference(preference: Preference)
}
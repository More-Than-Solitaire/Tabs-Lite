package com.gbros.tabslite.data.chord

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface ChordVariationDao {
    @Query("SELECT * FROM chord_variation WHERE chord_id = :chordId")
    suspend fun getChordVariations(chordId: String): List<ChordVariation>

    @Query("SELECT * FROM chord_variation WHERE chord_id = :chordId")
    fun chordVariations(chordId: String): LiveData<List<ChordVariation>>

    @Query("SELECT * FROM chord_variation WHERE id = :variationId")
    suspend fun getChordVariation(variationId: String): ChordVariation

    @Query("SELECT EXISTS(SELECT 1 FROM chord_variation WHERE id = :chordId LIMIT 1)")
    suspend fun chordExists(chordId: String): Boolean

    @Query("SELECT DISTINCT chord_id FROM chord_variation WHERE chord_id IN (:chordIds)")
    suspend fun findAll(chordIds: List<String>): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chords: List<ChordVariation>)
}
package com.gbros.tabslite.data

import androidx.room.*

/**
 * The Data Access Object for the Chord Variation class.
 */
@Dao
interface ChordVariationDao {
    @Query("SELECT * FROM chord_variation WHERE chord_id = :chordId")
    suspend fun getChordVariations(chordId: String): List<ChordVariation>

    @Query("SELECT * FROM chord_variation WHERE id = :variationId")
    suspend fun getChordVariation(variationId: String): ChordVariation

    @Query("SELECT EXISTS(SELECT 1 FROM chord_variation WHERE id = :chordId LIMIT 1)")
    suspend fun chordExists(chordId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chords: List<ChordVariation>)
}
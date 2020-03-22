package com.gbros.tabslite.data

class ChordVariationRepository private constructor(private val chordDao: ChordVariationDao) {
    suspend fun getChordVariations(chordId: String) = chordDao.getChordVariations(chordId)
    suspend fun getChordVariation(chordId: String) = chordDao.getChordVariation(chordId)
    suspend fun insertAll(chords: List<ChordVariation>) = chordDao.insertAll(chords)


    companion object {
        // For Singleton instantiation
        @Volatile private var instance: ChordVariationRepository? = null

        fun getInstance(chordDao: ChordVariationDao) =
                instance ?: synchronized(this) {
                    instance ?: ChordVariationRepository(chordDao).also { instance = it }
                }
    }
}
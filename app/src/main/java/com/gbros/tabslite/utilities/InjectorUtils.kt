package com.gbros.tabslite.utilities

import android.content.Context
import com.gbros.tabslite.data.*
import com.gbros.tabslite.viewmodels.*

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {
    private fun getChordVariationRepository(context: Context): ChordVariationRepository {
        return ChordVariationRepository.getInstance(
                AppDatabase.getInstance(context.applicationContext).chordVariationDao())
    }

    private fun getTabFullRepository(context: Context): TabFullRepository {
        return TabFullRepository.getInstance(
                AppDatabase.getInstance(context.applicationContext).tabFullDao())
    }

    fun provideTabDetailViewModelFactory(
            context: Context,
            tabId: Int
    ): TabDetailViewModelFactory {
        return TabDetailViewModelFactory(getTabFullRepository(context),
                getChordVariationRepository(context), tabId)
    }

    fun provideFavoriteTabViewModelFactory(context: Context): FavoriteTabsViewModelFactory {
        return FavoriteTabsViewModelFactory(getTabFullRepository(context))
    }
}

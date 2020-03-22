package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gbros.tabslite.data.*

/**
 * Factory for creating a [TabDetailViewModel] with a constructor that takes a [TabFullRepository]
 * and an ID for the current [TabFull].
 */
class TabDetailViewModelFactory(
        private val tabRepository: TabFullRepository,
        private val chordVariationRepository: ChordVariationRepository,
        private val tabId: Int
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TabDetailViewModel(tabRepository, chordVariationRepository, tabId) as T
    }
}

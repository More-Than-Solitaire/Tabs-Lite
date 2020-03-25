package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.data.ChordVariationRepository
import com.gbros.tabslite.data.TabFullRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * The ViewModel used in [PlantDetailFragment].
 */
class TabDetailViewModel(
        val tabRepository: TabFullRepository,
        private val chordVariationRepository: ChordVariationRepository,
        private val tabId: Int
) : ViewModel() {
    var tab = viewModelScope.async { tabRepository.getTab(tabId) }

    //eventually todo: getUsedChords


    fun setFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            if(isFavorite) {
                tabRepository.favoriteTab(tabId)
            } else {
                tabRepository.unfavoriteTab(tabId)
            }
        }
    }
}

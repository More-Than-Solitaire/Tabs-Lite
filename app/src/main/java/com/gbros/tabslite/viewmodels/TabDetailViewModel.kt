package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.data.ChordVariationRepository
import com.gbros.tabslite.data.TabFull
import com.gbros.tabslite.data.TabFullDao
import kotlinx.coroutines.async

/**
 * The ViewModel used in [PlantDetailFragment].
 */
class TabDetailViewModel(
    val tabFullDao: TabFullDao,
    private val chordVariationRepository: ChordVariationRepository,
    private val tabId: Int
) : ViewModel() {
    var getTabJob = viewModelScope.async { tabFullDao.getTab(tabId) }
    var tab: TabFull? = null
    //eventually todo: getUsedChords


}

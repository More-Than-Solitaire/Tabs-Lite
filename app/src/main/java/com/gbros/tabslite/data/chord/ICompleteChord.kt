package com.gbros.tabslite.data.chord

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

abstract class ICompleteChord(initialValue: List<ChordVariation> = emptyList()) :
    LiveData<List<ChordVariation>>() {
    /**
     * Tracks the progress of this chord's loading.  If it's being loaded from the internet, will be
     * false until the loading is complete.  Once it's true, if the value is still empty, no such
     * chord could be loaded
     */
    var loadingComplete: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        if (initialValue.isNotEmpty()) {
            value = initialValue
        }
    }
}
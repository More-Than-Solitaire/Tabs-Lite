package com.gbros.tabslite.utilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData


/**
 * Combine multiple livedata sources into a new livedata source
 */
fun <T1, T2, R> LiveData<T1>.combine(
    liveData2: LiveData<T2>,
    combineFn: (value1: T1?, value2: T2?) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    addSource(this@combine) {
        value = combineFn(it, liveData2.value)
    }
    addSource(liveData2) {
        value = combineFn(this@combine.value, it)
    }
}

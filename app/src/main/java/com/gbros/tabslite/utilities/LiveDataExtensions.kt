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

/**
 * Combine multiple livedata sources into a new livedata source
 */
fun <T1, T2, T3, R> LiveData<T1>.combine(
    liveData2: LiveData<T2>,
    liveData3: LiveData<T3>,
    combineFn: (value1: T1?, value2: T2?, value3: T3?) -> R
): LiveData<R> = MediatorLiveData<R>().apply {
    addSource(this@combine) {
        value = combineFn(it, liveData2.value, liveData3.value)
    }
    addSource(liveData2) {
        value = combineFn(this@combine.value, it, liveData3.value)
    }
    addSource(liveData3) {
        value = combineFn(this@combine.value, liveData2.value, it)
    }
}

/**
 * Combines an arbitrary number of LiveData sources into a new LiveData source.
 * The resulting LiveData will emit a list of all the values from the source LiveData objects.
 *
 * @param sources A vararg of LiveData objects to combine.
 * @param combineFn A function that takes a list of nullable values and returns the combined result.
 * @return A LiveData that emits the combined value.
 */
fun <T, R> combine(
    vararg sources: LiveData<out T>,
    combineFn: (values: List<T?>) -> R
): LiveData<R> {
    val mediator = MediatorLiveData<R>()
    val onChange = {
        val values = sources.map { it.value }
        mediator.value = combineFn(values)
    }

    sources.forEach { source ->
        mediator.addSource(source) { onChange() }
    }
    return mediator
}

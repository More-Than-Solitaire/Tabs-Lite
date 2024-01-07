package com.gbros.tabslite.data.chord

import androidx.lifecycle.MediatorLiveData
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.utilities.UgApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This class represents all variations of a particular chord.  Upon initialization, they will be fetched from the
 * local database if they exist there, or loaded from the internet if needed.
 */
class CompleteChord(private var db: AppDatabase, chordName: String) : ICompleteChord(chordName) {

    //region private data

    /**
     * Tracks updates in the chord variation database query for this chord, and updates our internal value if
     * anything changes
     */
    private val chordVariationsWatcher = MediatorLiveData<List<ChordVariation>>()

    /**
     * Tracks the number of times this chord has been reloaded from the internet.  We'll automatically load it
     * once each time the class is initiated if it's not in our database but any further must be manual relaods.
     */
    private var numLoadsFromInternet = 0

    //endregion

    //region constructor

    init {
        chordVariationsWatcher.addSource(db.chordVariationDao().chordVariations(chordName)) {
            this.value = it

            if (it.isEmpty() && numLoadsFromInternet == 0) {
                launchInternetReload()
            } else {
                loadingComplete.value = true
            }
        }
    }

    //endregion

    //region private methods

    @OptIn(DelicateCoroutinesApi::class)
    private fun launchInternetReload() {
        GlobalScope.launch { reloadChordFromInternet() }
    }

    //endregion

    //region public methods

    suspend fun reloadChordFromInternet(force: Boolean = false) {
        loadingComplete.value = false
        numLoadsFromInternet++

        UgApi.updateChordVariations(listOf(chordName), db, force)

        loadingComplete.value = true
    }

    //endregion
}
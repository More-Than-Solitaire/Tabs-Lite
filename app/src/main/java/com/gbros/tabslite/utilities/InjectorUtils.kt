package com.gbros.tabslite.utilities

import android.content.Context
import com.gbros.tabslite.data.AppDatabase
import com.gbros.tabslite.data.PlaylistRepository
import com.gbros.tabslite.viewmodels.PlaylistsViewModelFactory

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {
    private fun getPlaylistRepository(context: Context): PlaylistRepository {
        return PlaylistRepository.getInstance(
                AppDatabase.getInstance(context.applicationContext).playlistDao()
        )
    }

    fun providePlaylistsViewModelFactory(context: Context): PlaylistsViewModelFactory {
        return PlaylistsViewModelFactory(getPlaylistRepository(context))
    }
}

package com.gbros.tabslite

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.gbros.tabslite.utilities.DARK_MODE_PREF_NAME
import com.gbros.tabslite.utilities.PREFS_NAME

// thanks https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class
class DefaultApplication : Application() {
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    override fun onCreate() {
        super.onCreate()

        // set dark mode based on preferences
        val settings: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val darkModePref = settings.getInt(DARK_MODE_PREF_NAME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(darkModePref) // thanks https://proandroiddev.com/android-dark-theme-implementation-recap-4fcffb0c4bff
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    override fun onConfigurationChanged ( newConfig : Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    override fun onLowMemory() {
        super.onLowMemory()
        // todo: we can delete tabs from the database that aren't favorites
    }
}
package com.gbros.tabslite

import android.app.Application
import com.gbros.tabslite.utilities.ApiHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// thanks https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class
class DefaultApplication : Application() {
    /**
     * Called when the application is starting, before any other application objects have been created.
     * Overriding this method is totally optional!
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        // initialize the API key.  Must be at the Application level, because the app has multiple entry points.
        GlobalScope.launch { ApiHelper.updateApiKey() }  // set the api key now before we need it
    }
}
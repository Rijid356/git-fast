package com.gitfast.app

import android.app.Application
import android.content.pm.ApplicationInfo
import com.gitfast.app.logging.CrashlyticsTree
import com.gitfast.app.logging.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class GitFastApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        Timber.plant(FileLoggingTree(this))
    }
}

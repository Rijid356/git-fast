package com.gitfast.app.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Timber tree that forwards WARN and ERROR logs to Firebase Crashlytics.
 * Throwables are recorded as non-fatal exceptions; messages are logged as breadcrumbs.
 */
class CrashlyticsTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        crashlytics.log("${tag ?: "---"}: $message")
        if (t != null) {
            crashlytics.recordException(t)
        }
    }
}

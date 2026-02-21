package com.gitfast.app.data.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data object Syncing : SyncStatus()
    data class Success(val timestamp: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

@Singleton
class SyncStatusStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    var lastSyncedAt: Long
        get() = prefs.getLong(KEY_LAST_SYNCED_AT, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_SYNCED_AT, value).apply()
        }

    var hasCompletedInitialSync: Boolean
        get() = prefs.getBoolean(KEY_INITIAL_SYNC_COMPLETE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_INITIAL_SYNC_COMPLETE, value).apply()
        }

    fun setSyncing() {
        _syncStatus.value = SyncStatus.Syncing
    }

    fun setSuccess() {
        val now = System.currentTimeMillis()
        lastSyncedAt = now
        _syncStatus.value = SyncStatus.Success(now)
    }

    fun setError(message: String) {
        _syncStatus.value = SyncStatus.Error(message)
    }

    fun setIdle() {
        _syncStatus.value = SyncStatus.Idle
    }

    companion object {
        private const val PREFS_NAME = "gitfast_sync"
        private const val KEY_LAST_SYNCED_AT = "last_synced_at"
        private const val KEY_INITIAL_SYNC_COMPLETE = "initial_sync_complete"
    }
}

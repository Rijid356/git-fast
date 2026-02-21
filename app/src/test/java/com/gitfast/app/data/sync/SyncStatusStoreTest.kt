package com.gitfast.app.data.sync

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncStatusStoreTest {

    private lateinit var store: SyncStatusStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        store = SyncStatusStore(context)
    }

    @Test
    fun `lastSyncedAt defaults to 0`() {
        assertEquals(0L, store.lastSyncedAt)
    }

    @Test
    fun `lastSyncedAt persists value`() {
        store.lastSyncedAt = 12345L
        assertEquals(12345L, store.lastSyncedAt)
    }

    @Test
    fun `hasCompletedInitialSync defaults to false`() {
        assertFalse(store.hasCompletedInitialSync)
    }

    @Test
    fun `hasCompletedInitialSync persists value`() {
        store.hasCompletedInitialSync = true
        assertTrue(store.hasCompletedInitialSync)
    }

    @Test
    fun `syncStatus starts as Idle`() {
        assertEquals(SyncStatus.Idle, store.syncStatus.value)
    }

    @Test
    fun `setSyncing updates status to Syncing`() {
        store.setSyncing()
        assertEquals(SyncStatus.Syncing, store.syncStatus.value)
    }

    @Test
    fun `setSuccess updates status and lastSyncedAt`() {
        store.setSuccess()
        val status = store.syncStatus.value
        assertTrue(status is SyncStatus.Success)
        assertTrue(store.lastSyncedAt > 0)
    }

    @Test
    fun `setError updates status with message`() {
        store.setError("Network error")
        val status = store.syncStatus.value
        assertTrue(status is SyncStatus.Error)
        assertEquals("Network error", (status as SyncStatus.Error).message)
    }

    @Test
    fun `setIdle resets status to Idle`() {
        store.setSyncing()
        store.setIdle()
        assertEquals(SyncStatus.Idle, store.syncStatus.value)
    }
}

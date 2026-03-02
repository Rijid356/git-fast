package com.gitfast.app

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.gitfast.app.auth.GoogleAuthManager
import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.local.LapStartPointDao
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.sync.FirestoreSync
import com.gitfast.app.data.sync.SyncStatusStore
import com.gitfast.app.ui.settings.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    private lateinit var settingsStore: SettingsStore
    private lateinit var googleAuthManager: GoogleAuthManager
    private lateinit var firestoreSync: FirestoreSync
    private lateinit var syncStatusStore: SyncStatusStore
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var bodyCompRepository: BodyCompRepository
    private lateinit var lapStartPointDao: LapStartPointDao
    private lateinit var viewModel: SettingsViewModel
    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        settingsStore = mockk(relaxed = true)
        googleAuthManager = mockk(relaxed = true)
        firestoreSync = mockk(relaxed = true)
        syncStatusStore = mockk(relaxed = true)
        healthConnectManager = mockk(relaxed = true)
        bodyCompRepository = mockk(relaxed = true)
        lapStartPointDao = mockk(relaxed = true)
        every { settingsStore.autoPauseEnabled } returns true
        every { settingsStore.keepScreenOn } returns true
        every { googleAuthManager.currentUser } returns MutableStateFlow(null)
        every { syncStatusStore.lastSyncedAt } returns 0L
        every { syncStatusStore.syncStatus } returns MutableStateFlow(com.gitfast.app.data.sync.SyncStatus.Idle)
        viewModel = SettingsViewModel(app, settingsStore, googleAuthManager, firestoreSync, syncStatusStore, healthConnectManager, bodyCompRepository, lapStartPointDao)
    }

    @Test
    fun `init loads current settings from store`() {
        val state = viewModel.uiState.value
        assertTrue(state.autoPauseEnabled)
        assertTrue(state.keepScreenOn)
    }

    @Test
    fun `setAutoPauseEnabled updates store and ui state`() {
        viewModel.setAutoPauseEnabled(false)

        verify { settingsStore.autoPauseEnabled = false }
        assertFalse(viewModel.uiState.value.autoPauseEnabled)
    }

    @Test
    fun `setKeepScreenOn updates store and ui state`() {
        viewModel.setKeepScreenOn(false)

        verify { settingsStore.keepScreenOn = false }
        assertFalse(viewModel.uiState.value.keepScreenOn)
    }

    @Test
    fun `toggling autoPause back to true updates state`() {
        viewModel.setAutoPauseEnabled(false)
        viewModel.setAutoPauseEnabled(true)

        assertTrue(viewModel.uiState.value.autoPauseEnabled)
    }

    @Test
    fun `init with non-default settings`() {
        val customStore = mockk<SettingsStore>(relaxed = true)
        every { customStore.autoPauseEnabled } returns false
        every { customStore.keepScreenOn } returns false

        val vm = SettingsViewModel(app, customStore, googleAuthManager, firestoreSync, syncStatusStore, healthConnectManager, bodyCompRepository, lapStartPointDao)
        val state = vm.uiState.value

        assertFalse(state.autoPauseEnabled)
        assertFalse(state.keepScreenOn)
    }
}

package com.gitfast.app.ui.settings

import android.app.Application
import com.gitfast.app.auth.GoogleAuthManager
import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.local.LapStartPointDao
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.sync.FirestoreSync
import com.gitfast.app.data.sync.SyncStatus
import com.gitfast.app.data.sync.SyncStatusStore
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockApplication: Application
    private lateinit var mockSettingsStore: SettingsStore
    private lateinit var mockGoogleAuthManager: GoogleAuthManager
    private lateinit var mockFirestoreSync: FirestoreSync
    private lateinit var mockSyncStatusStore: SyncStatusStore
    private lateinit var mockHealthConnectManager: HealthConnectManager
    private lateinit var mockBodyCompRepository: BodyCompRepository
    private lateinit var mockLapStartPointDao: LapStartPointDao

    private val currentUserFlow = MutableStateFlow<FirebaseUser?>(null)
    private val syncStatusFlow = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockApplication = mockk(relaxed = true)
        mockSettingsStore = mockk(relaxed = true)
        mockGoogleAuthManager = mockk(relaxed = true)
        mockFirestoreSync = mockk(relaxed = true)
        mockSyncStatusStore = mockk(relaxed = true)
        mockHealthConnectManager = mockk(relaxed = true)
        mockBodyCompRepository = mockk(relaxed = true)
        mockLapStartPointDao = mockk(relaxed = true)

        every { mockSettingsStore.autoPauseEnabled } returns true
        every { mockSettingsStore.keepScreenOn } returns true
        every { mockSettingsStore.autoLapEnabled } returns false
        every { mockSettingsStore.homeArrivalEnabled } returns false
        every { mockSettingsStore.hasHomeLocation } returns false
        every { mockSettingsStore.screenshotOverlayEnabled } returns false
        every { mockGoogleAuthManager.currentUser } returns currentUserFlow
        every { mockSyncStatusStore.syncStatus } returns syncStatusFlow
        every { mockSyncStatusStore.lastSyncedAt } returns 0L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(
        application = mockApplication,
        settingsStore = mockSettingsStore,
        googleAuthManager = mockGoogleAuthManager,
        firestoreSync = mockFirestoreSync,
        syncStatusStore = mockSyncStatusStore,
        healthConnectManager = mockHealthConnectManager,
        bodyCompRepository = mockBodyCompRepository,
        lapStartPointDao = mockLapStartPointDao,
    )

    // =========================================================================
    // Initial state
    // =========================================================================

    @Test
    fun `initial state loads from SettingsStore defaults`() {
        val vm = createViewModel()
        val state = vm.uiState.value

        assertTrue(state.autoPauseEnabled)
        assertTrue(state.keepScreenOn)
        assertFalse(state.autoLapEnabled)
        assertFalse(state.homeArrivalEnabled)
        assertFalse(state.hasHomeLocation)
    }

    @Test
    fun `initial state reflects non-default settings`() {
        every { mockSettingsStore.autoPauseEnabled } returns false
        every { mockSettingsStore.keepScreenOn } returns false
        every { mockSettingsStore.autoLapEnabled } returns true
        every { mockSettingsStore.homeArrivalEnabled } returns true
        every { mockSettingsStore.hasHomeLocation } returns true

        val vm = createViewModel()
        val state = vm.uiState.value

        assertFalse(state.autoPauseEnabled)
        assertFalse(state.keepScreenOn)
        assertTrue(state.autoLapEnabled)
        assertTrue(state.homeArrivalEnabled)
        assertTrue(state.hasHomeLocation)
    }

    @Test
    fun `initial state is not signed in when currentUser is null`() {
        val vm = createViewModel()

        assertFalse(vm.uiState.value.isSignedIn)
        assertNull(vm.uiState.value.userEmail)
    }

    @Test
    fun `initial state is signed in when currentUser exists`() {
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.email } returns "test@example.com"
        currentUserFlow.value = mockUser

        val vm = createViewModel()

        assertTrue(vm.uiState.value.isSignedIn)
        assertEquals("test@example.com", vm.uiState.value.userEmail)
    }

    // =========================================================================
    // Auth state observation
    // =========================================================================

    @Test
    fun `auth state updates when user signs in`() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.isSignedIn)

        val mockUser = mockk<FirebaseUser>()
        every { mockUser.email } returns "user@test.com"
        currentUserFlow.value = mockUser

        assertTrue(vm.uiState.value.isSignedIn)
        assertEquals("user@test.com", vm.uiState.value.userEmail)
    }

    @Test
    fun `auth state updates when user signs out`() {
        val mockUser = mockk<FirebaseUser>()
        every { mockUser.email } returns "user@test.com"
        currentUserFlow.value = mockUser

        val vm = createViewModel()
        assertTrue(vm.uiState.value.isSignedIn)

        currentUserFlow.value = null

        assertFalse(vm.uiState.value.isSignedIn)
        assertNull(vm.uiState.value.userEmail)
    }

    // =========================================================================
    // Sync status observation
    // =========================================================================

    @Test
    fun `sync status updates to syncing`() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.isSyncing)

        syncStatusFlow.value = SyncStatus.Syncing

        assertTrue(vm.uiState.value.isSyncing)
        assertEquals(SyncStatus.Syncing, vm.uiState.value.syncStatus)
    }

    @Test
    fun `sync status updates to success`() {
        val vm = createViewModel()

        syncStatusFlow.value = SyncStatus.Success(12345L)

        assertFalse(vm.uiState.value.isSyncing)
        assertTrue(vm.uiState.value.syncStatus is SyncStatus.Success)
    }

    @Test
    fun `sync status updates to error`() {
        val vm = createViewModel()

        syncStatusFlow.value = SyncStatus.Error("Network error")

        assertFalse(vm.uiState.value.isSyncing)
        assertTrue(vm.uiState.value.syncStatus is SyncStatus.Error)
    }

    // =========================================================================
    // Setter methods
    // =========================================================================

    @Test
    fun `setAutoPauseEnabled persists and updates state`() {
        val vm = createViewModel()

        vm.setAutoPauseEnabled(false)

        verify { mockSettingsStore.autoPauseEnabled = false }
        assertFalse(vm.uiState.value.autoPauseEnabled)
    }

    @Test
    fun `setKeepScreenOn persists and updates state`() {
        val vm = createViewModel()

        vm.setKeepScreenOn(false)

        verify { mockSettingsStore.keepScreenOn = false }
        assertFalse(vm.uiState.value.keepScreenOn)
    }

    @Test
    fun `setAutoLapEnabled persists and updates state`() {
        val vm = createViewModel()

        vm.setAutoLapEnabled(true)

        verify { mockSettingsStore.autoLapEnabled = true }
        assertTrue(vm.uiState.value.autoLapEnabled)
    }

    @Test
    fun `setHomeArrivalEnabled persists and updates state`() {
        val vm = createViewModel()

        vm.setHomeArrivalEnabled(true)

        verify { mockSettingsStore.homeArrivalEnabled = true }
        assertTrue(vm.uiState.value.homeArrivalEnabled)
    }

    // =========================================================================
    // clearHomeLocation
    // =========================================================================

    @Test
    fun `clearHomeLocation clears store and disables home arrival`() {
        every { mockSettingsStore.hasHomeLocation } returns true
        every { mockSettingsStore.homeArrivalEnabled } returns true
        val vm = createViewModel()

        vm.clearHomeLocation()

        verify { mockSettingsStore.clearHomeLocation() }
        verify { mockSettingsStore.homeArrivalEnabled = false }
        assertFalse(vm.uiState.value.hasHomeLocation)
        assertFalse(vm.uiState.value.homeArrivalEnabled)
    }

    // =========================================================================
    // signOut and syncNow
    // =========================================================================

    @Test
    fun `signOut delegates to GoogleAuthManager`() = runTest {
        val vm = createViewModel()

        vm.signOut()

        coVerify { mockGoogleAuthManager.signOut() }
    }

    @Test
    fun `syncNow delegates to FirestoreSync fullSync`() = runTest {
        val vm = createViewModel()

        vm.syncNow()

        coVerify { mockFirestoreSync.fullSync() }
    }

    // =========================================================================
    // signIn
    // =========================================================================

    @Test
    fun `signIn triggers initial migration on first sign-in`() = runTest {
        val mockUser = mockk<FirebaseUser>()
        val mockContext = mockk<android.content.Context>(relaxed = true)
        coEvery { mockGoogleAuthManager.signIn(any()) } returns Result.success(mockUser)
        every { mockSyncStatusStore.hasCompletedInitialSync } returns false

        val vm = createViewModel()
        vm.signIn(mockContext)

        coVerify { mockFirestoreSync.initialMigration() }
    }

    @Test
    fun `signIn skips initial migration when already synced`() = runTest {
        val mockUser = mockk<FirebaseUser>()
        val mockContext = mockk<android.content.Context>(relaxed = true)
        coEvery { mockGoogleAuthManager.signIn(any()) } returns Result.success(mockUser)
        every { mockSyncStatusStore.hasCompletedInitialSync } returns true

        val vm = createViewModel()
        vm.signIn(mockContext)

        coVerify(exactly = 0) { mockFirestoreSync.initialMigration() }
    }

    @Test
    fun `signIn skips initial migration on sign-in failure`() = runTest {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        coEvery { mockGoogleAuthManager.signIn(any()) } returns Result.failure(Exception("failed"))
        every { mockSyncStatusStore.hasCompletedInitialSync } returns false

        val vm = createViewModel()
        vm.signIn(mockContext)

        coVerify(exactly = 0) { mockFirestoreSync.initialMigration() }
    }

    @Test
    fun `signIn sets signInError on failure`() = runTest {
        val mockContext = mockk<android.content.Context>(relaxed = true)
        coEvery { mockGoogleAuthManager.signIn(any()) } returns Result.failure(Exception("Network error"))

        val vm = createViewModel()
        assertNull(vm.uiState.value.signInError)

        vm.signIn(mockContext)

        assertEquals("Network error", vm.uiState.value.signInError)
    }

    // =========================================================================
    // Lap Start Points (multi-park)
    // =========================================================================

    @Test
    fun `initial state has zero lap start point count`() {
        val vm = createViewModel()
        assertEquals(0, vm.uiState.value.lapStartPointCount)
    }

    @Test
    fun `clearAllLapStartPoints calls deleteAll on DAO`() = runTest {
        val vm = createViewModel()

        vm.clearAllLapStartPoints()

        coVerify { mockLapStartPointDao.deleteAll() }
    }

    // =========================================================================
    // Screenshot overlay
    // =========================================================================

    @Test
    fun `initial state has screenshotOverlayEnabled false`() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.screenshotOverlayEnabled)
    }

    @Test
    fun `setScreenshotOverlayEnabled persists and updates state`() {
        val vm = createViewModel()

        vm.setScreenshotOverlayEnabled(true)

        verify { mockSettingsStore.screenshotOverlayEnabled = true }
        assertTrue(vm.uiState.value.screenshotOverlayEnabled)
    }

    @Test
    fun `initial state reflects screenshotOverlayEnabled when true`() {
        every { mockSettingsStore.screenshotOverlayEnabled } returns true

        val vm = createViewModel()
        assertTrue(vm.uiState.value.screenshotOverlayEnabled)
    }

    @Test
    fun `signIn clears signInError on success`() = runTest {
        val mockUser = mockk<FirebaseUser>()
        val mockContext = mockk<android.content.Context>(relaxed = true)

        // First, trigger an error
        coEvery { mockGoogleAuthManager.signIn(any()) } returns Result.failure(Exception("fail"))
        val vm = createViewModel()
        vm.signIn(mockContext)
        assertEquals("fail", vm.uiState.value.signInError)

        // Now succeed
        coEvery { mockGoogleAuthManager.signIn(any()) } returns Result.success(mockUser)
        every { mockSyncStatusStore.hasCompletedInitialSync } returns true
        vm.signIn(mockContext)

        assertNull(vm.uiState.value.signInError)
    }
}

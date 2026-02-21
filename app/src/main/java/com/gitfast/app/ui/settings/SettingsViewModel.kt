package com.gitfast.app.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.auth.GoogleAuthManager
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.DistanceUnit
import com.gitfast.app.data.sync.FirestoreSync
import com.gitfast.app.data.sync.SyncStatus
import com.gitfast.app.data.sync.SyncStatusStore
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val autoPauseEnabled: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.MILES,
    val keepScreenOn: Boolean = true,
    val autoLapEnabled: Boolean = false,
    val autoLapAnchorRadiusMeters: Int = 15,
    val homeArrivalEnabled: Boolean = false,
    val hasHomeLocation: Boolean = false,
    val homeArrivalRadiusMeters: Int = 30,
    val isCapturingLocation: Boolean = false,
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val lastSyncedAt: Long = 0L,
    val isSyncing: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsStore: SettingsStore,
    private val googleAuthManager: GoogleAuthManager,
    private val firestoreSync: FirestoreSync,
    private val syncStatusStore: SyncStatusStore,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            autoPauseEnabled = settingsStore.autoPauseEnabled,
            distanceUnit = settingsStore.distanceUnit,
            keepScreenOn = settingsStore.keepScreenOn,
            autoLapEnabled = settingsStore.autoLapEnabled,
            autoLapAnchorRadiusMeters = settingsStore.autoLapAnchorRadiusMeters,
            homeArrivalEnabled = settingsStore.homeArrivalEnabled,
            hasHomeLocation = settingsStore.hasHomeLocation,
            homeArrivalRadiusMeters = settingsStore.homeArrivalRadiusMeters,
            isSignedIn = googleAuthManager.currentUser.value != null,
            userEmail = googleAuthManager.currentUser.value?.email,
            lastSyncedAt = syncStatusStore.lastSyncedAt,
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state
        viewModelScope.launch {
            googleAuthManager.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    isSignedIn = user != null,
                    userEmail = user?.email,
                )
            }
        }

        // Observe sync status
        viewModelScope.launch {
            syncStatusStore.syncStatus.collect { status ->
                _uiState.value = _uiState.value.copy(
                    syncStatus = status,
                    isSyncing = status is SyncStatus.Syncing,
                    lastSyncedAt = syncStatusStore.lastSyncedAt,
                )
            }
        }
    }

    fun signIn(activityContext: Context) {
        viewModelScope.launch {
            val result = googleAuthManager.signIn(activityContext)
            if (result.isSuccess && !syncStatusStore.hasCompletedInitialSync) {
                firestoreSync.initialMigration()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthManager.signOut()
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            firestoreSync.fullSync()
        }
    }

    fun setAutoPauseEnabled(enabled: Boolean) {
        settingsStore.autoPauseEnabled = enabled
        _uiState.value = _uiState.value.copy(autoPauseEnabled = enabled)
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        settingsStore.distanceUnit = unit
        _uiState.value = _uiState.value.copy(distanceUnit = unit)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        settingsStore.keepScreenOn = enabled
        _uiState.value = _uiState.value.copy(keepScreenOn = enabled)
    }

    fun setAutoLapEnabled(enabled: Boolean) {
        settingsStore.autoLapEnabled = enabled
        _uiState.value = _uiState.value.copy(autoLapEnabled = enabled)
    }

    fun setAutoLapAnchorRadius(radius: Int) {
        settingsStore.autoLapAnchorRadiusMeters = radius
        _uiState.value = _uiState.value.copy(autoLapAnchorRadiusMeters = radius)
    }

    fun setHomeArrivalEnabled(enabled: Boolean) {
        settingsStore.homeArrivalEnabled = enabled
        _uiState.value = _uiState.value.copy(homeArrivalEnabled = enabled)
    }

    fun setHomeArrivalRadius(radius: Int) {
        settingsStore.homeArrivalRadiusMeters = radius
        _uiState.value = _uiState.value.copy(homeArrivalRadiusMeters = radius)
    }

    @SuppressLint("MissingPermission")
    fun captureCurrentLocation() {
        _uiState.value = _uiState.value.copy(isCapturingLocation = true)
        val fusedClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    settingsStore.homeLatitude = location.latitude
                    settingsStore.homeLongitude = location.longitude
                    _uiState.value = _uiState.value.copy(
                        hasHomeLocation = true,
                        isCapturingLocation = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isCapturingLocation = false)
                }
            }
            .addOnFailureListener {
                _uiState.value = _uiState.value.copy(isCapturingLocation = false)
            }
    }

    fun clearHomeLocation() {
        settingsStore.clearHomeLocation()
        settingsStore.homeArrivalEnabled = false
        _uiState.value = _uiState.value.copy(
            hasHomeLocation = false,
            homeArrivalEnabled = false
        )
    }
}

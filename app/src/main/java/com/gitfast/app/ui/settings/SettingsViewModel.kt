package com.gitfast.app.ui.settings

import androidx.lifecycle.ViewModel
import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.DistanceUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val autoPauseEnabled: Boolean = true,
    val distanceUnit: DistanceUnit = DistanceUnit.MILES,
    val keepScreenOn: Boolean = true,
    val autoLapEnabled: Boolean = false,
    val autoLapDistanceMeters: Int = 400,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            autoPauseEnabled = settingsStore.autoPauseEnabled,
            distanceUnit = settingsStore.distanceUnit,
            keepScreenOn = settingsStore.keepScreenOn,
            autoLapEnabled = settingsStore.autoLapEnabled,
            autoLapDistanceMeters = settingsStore.autoLapDistanceMeters,
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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

    fun setAutoLapDistanceMeters(distance: Int) {
        settingsStore.autoLapDistanceMeters = distance
        _uiState.value = _uiState.value.copy(autoLapDistanceMeters = distance)
    }
}

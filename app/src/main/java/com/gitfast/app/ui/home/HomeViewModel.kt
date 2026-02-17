package com.gitfast.app.ui.home

import androidx.lifecycle.ViewModel
import com.gitfast.app.data.local.WorkoutStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutStateStore: WorkoutStateStore,
) : ViewModel() {

    private val _showRecoveryDialog = MutableStateFlow(false)
    val showRecoveryDialog: StateFlow<Boolean> = _showRecoveryDialog.asStateFlow()

    init {
        checkForIncompleteWorkout()
    }

    private fun checkForIncompleteWorkout() {
        _showRecoveryDialog.value = workoutStateStore.hasActiveWorkout()
    }

    fun dismissRecoveryDialog() {
        workoutStateStore.clearActiveWorkout()
        _showRecoveryDialog.value = false
    }
}

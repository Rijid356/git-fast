package com.gitfast.app.ui.home

import androidx.lifecycle.ViewModel
import com.gitfast.app.service.WorkoutStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    workoutStateManager: WorkoutStateManager
) : ViewModel() {

    val workoutState = workoutStateManager.workoutState
    val gpsPoints = workoutStateManager.gpsPoints
}

package com.gitfast.app.ui.analytics.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.PersonalRecordsCalculator
import com.gitfast.app.util.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalRecordsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonalRecordsUiState())
    val uiState: StateFlow<PersonalRecordsUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    private fun loadRecords() {
        viewModelScope.launch {
            val allWorkouts = workoutRepository.getAllCompletedWorkoutsOnce()
            if (allWorkouts.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isEmpty = true) }
                return@launch
            }

            val runs = allWorkouts.filter { it.activityType == ActivityType.RUN }
            val walks = allWorkouts.filter { it.activityType == ActivityType.DOG_WALK }
            val runsWithLaps = workoutRepository.getAllRunsWithLaps()
            val totalRunDistance = workoutRepository.getTotalDistanceMeters() -
                workoutRepository.getTotalDogWalkDistanceMeters()
            val totalWalkDistance = workoutRepository.getTotalDogWalkDistanceMeters()
            val longestStreak = StreakCalculator.getLongestStreak(allWorkouts)

            val sections = mutableListOf<RecordSection>()

            val runRecords = PersonalRecordsCalculator.calculateRunRecords(runs, runsWithLaps)
            if (runRecords.isNotEmpty()) {
                sections.add(RecordSection("RUNNING", runRecords))
            }

            val walkRecords = PersonalRecordsCalculator.calculateWalkRecords(walks)
            if (walkRecords.isNotEmpty()) {
                sections.add(RecordSection("DOG WALKS", walkRecords))
            }

            val overallRecords = PersonalRecordsCalculator.calculateOverallRecords(
                allWorkouts = allWorkouts,
                totalRunDistance = totalRunDistance,
                totalWalkDistance = totalWalkDistance,
                longestStreak = longestStreak,
            )
            if (overallRecords.isNotEmpty()) {
                sections.add(RecordSection("OVERALL", overallRecords))
            }

            _uiState.update { it.copy(sections = sections, isLoading = false) }
        }
    }
}

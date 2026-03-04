package com.gitfast.app.ui.soreness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.MuscleGroup
import com.gitfast.app.data.model.SorenessIntensity
import com.gitfast.app.data.model.SorenessLog
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.SorenessRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.AchievementChecker
import com.gitfast.app.util.AchievementSnapshot
import com.gitfast.app.util.StatsCalculator
import com.gitfast.app.util.StreakCalculator
import com.gitfast.app.util.XpCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SorenessLogUiState(
    val todayLog: SorenessLog? = null,
    val muscleIntensities: Map<MuscleGroup, SorenessIntensity> = emptyMap(),
    val showingFront: Boolean = true,
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val xpEarned: Int? = null,
    val achievementNames: List<String> = emptyList(),
)

@HiltViewModel
class SorenessLogViewModel @Inject constructor(
    private val sorenessRepository: SorenessRepository,
    private val characterRepository: CharacterRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SorenessLogUiState())
    val uiState: StateFlow<SorenessLogUiState> = _uiState.asStateFlow()

    val todayLog: StateFlow<SorenessLog?> = sorenessRepository.observeTodayLog()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadTodayLog()
    }

    private fun loadTodayLog() {
        viewModelScope.launch {
            val existing = sorenessRepository.getTodayLog()
            if (existing != null) {
                _uiState.value = _uiState.value.copy(
                    todayLog = existing,
                    muscleIntensities = existing.muscleIntensities,
                    notes = existing.notes ?: "",
                )
            }
        }
    }

    fun cycleMuscleIntensity(muscle: MuscleGroup) {
        val current = _uiState.value.muscleIntensities
        val currentIntensity = current[muscle]
        val nextMap = when (currentIntensity) {
            null -> current + (muscle to SorenessIntensity.MILD)
            SorenessIntensity.MILD -> current + (muscle to SorenessIntensity.MODERATE)
            SorenessIntensity.MODERATE -> current + (muscle to SorenessIntensity.SEVERE)
            SorenessIntensity.SEVERE -> current - muscle
        }
        _uiState.value = _uiState.value.copy(muscleIntensities = nextMap)
    }

    fun toggleFrontBack() {
        _uiState.value = _uiState.value.copy(showingFront = !_uiState.value.showingFront)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun startEditing() {
        val log = _uiState.value.todayLog ?: return
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            muscleIntensities = log.muscleIntensities,
            notes = log.notes ?: "",
        )
    }

    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(isEditing = false)
    }

    fun saveSoreness() {
        val state = _uiState.value
        if (state.muscleIntensities.isEmpty()) return

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val workouts = workoutRepository.getCompletedWorkouts().first()
            val streakDays = StreakCalculator.getCurrentStreak(workouts)
            val xpResult = XpCalculator.calculateSorenessXp(state.muscleIntensities, streakDays)

            val dateKey = "soreness:${LocalDate.now()}"
            val isUpdate = state.todayLog != null

            val log = sorenessRepository.logSoreness(
                muscleIntensities = state.muscleIntensities,
                notes = state.notes.ifBlank { null },
                xpAwarded = xpResult.totalXp,
            )

            // Award XP (idempotent — won't double-award on edit)
            val maxIntensity = state.muscleIntensities.values.maxByOrNull { it.ordinal }
                ?: SorenessIntensity.MILD
            characterRepository.awardXp(
                profileId = 1,
                workoutId = dateKey,
                xpAmount = xpResult.totalXp,
                reason = "Soreness check-in; ${maxIntensity.displayName}",
            )

            // Update TGH stat
            val recentLogs = sorenessRepository.getLast30DaysLogs()
            val toughness = StatsCalculator.calculateToughness(recentLogs)
            characterRepository.updateToughness(profileId = 1, toughness = toughness)

            // Check RECOVERY achievements
            val totalCount = sorenessRepository.getTotalCount()
            val unlockedIds = characterRepository.getUnlockedAchievementIds(1)
            val snapshot = AchievementSnapshot(
                allWorkouts = workouts,
                totalLapCount = 0,
                dogWalkCount = 0,
                characterLevel = characterRepository.getProfileLevel(1),
                unlockedIds = unlockedIds,
                totalSorenessLogCount = totalCount,
                toughnessStat = toughness,
            )
            val newAchievements = AchievementChecker.checkNewAchievements(snapshot)
            val achievementNames = mutableListOf<String>()
            for (def in newAchievements) {
                val xp = characterRepository.unlockAchievement(profileId = 1, def = def)
                if (xp > 0) {
                    achievementNames.add(def.title)
                }
            }

            _uiState.value = _uiState.value.copy(
                todayLog = log,
                isSaving = false,
                isEditing = false,
                xpEarned = if (!isUpdate) xpResult.totalXp else null,
                achievementNames = achievementNames,
            )
        }
    }

    fun dismissXpToast() {
        _uiState.value = _uiState.value.copy(xpEarned = null, achievementNames = emptyList())
    }
}

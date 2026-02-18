package com.gitfast.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.UnlockedAchievement
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    characterRepository: CharacterRepository,
    workoutRepository: WorkoutRepository,
) : ViewModel() {

    val profile: StateFlow<CharacterProfile> =
        combine(
            characterRepository.getProfile(),
            workoutRepository.getCompletedWorkouts(),
        ) { profile, workouts ->
            val streak = StreakCalculator.getCurrentStreak(workouts)
            profile.copy(
                currentStreak = streak,
                streakMultiplier = StreakCalculator.getMultiplier(streak),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CharacterProfile())

    val recentXpTransactions: StateFlow<List<XpTransaction>> =
        characterRepository.getRecentXpTransactions(20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedAchievementIds: StateFlow<Set<String>> =
        characterRepository.getUnlockedAchievements()
            .map { list -> list.map { it.achievementId }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
}

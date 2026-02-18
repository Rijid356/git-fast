package com.gitfast.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0) // 0=ME, 1=JUNIPER
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // User profile (profileId=1)
    val profile: StateFlow<CharacterProfile> =
        combine(
            characterRepository.getProfile(1),
            workoutRepository.getCompletedWorkouts(),
        ) { profile, workouts ->
            val streak = StreakCalculator.getCurrentStreak(workouts)
            profile.copy(
                currentStreak = streak,
                streakMultiplier = StreakCalculator.getMultiplier(streak),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CharacterProfile())

    val recentXpTransactions: StateFlow<List<XpTransaction>> =
        characterRepository.getRecentXpTransactions(profileId = 1, limit = 20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedAchievementIds: StateFlow<Set<String>> =
        characterRepository.getUnlockedAchievements(1)
            .map { list -> list.map { it.achievementId }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Juniper profile (profileId=2)
    val juniperProfile: StateFlow<CharacterProfile> =
        characterRepository.getProfile(2)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CharacterProfile())

    val juniperXpTransactions: StateFlow<List<XpTransaction>> =
        characterRepository.getRecentXpTransactions(profileId = 2, limit = 20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val juniperUnlockedAchievementIds: StateFlow<Set<String>> =
        characterRepository.getUnlockedAchievements(2)
            .map { list -> list.map { it.achievementId }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
}

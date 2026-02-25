package com.gitfast.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.repository.CharacterRepository
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.healthconnect.HealthConnectManager
import com.gitfast.app.data.repository.WorkoutRepository
import com.gitfast.app.util.StatBreakdown
import com.gitfast.app.util.StatsCalculator
import com.gitfast.app.util.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Data class for VIT stat UI state.
 * Encapsulates Health Connect connection status and vitality data.
 */
data class VitalityUiState(
    val healthConnectConnected: Boolean = false,
    val weighInCount30d: Int = 0,
    val bodyFatTrendPercent: Double? = null,
    val vitalityStat: Int = 1,
    val breakdown: StatBreakdown? = null,
)

@HiltViewModel
class CharacterSheetViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    workoutRepository: WorkoutRepository,
    private val bodyCompRepository: BodyCompRepository,
    private val healthConnectManager: HealthConnectManager,
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

    // Stat breakdowns for user (includes VIT)
    val statBreakdowns: StateFlow<Map<String, StatBreakdown>> =
        workoutRepository.getCompletedWorkouts()
            .map { workouts ->
                val recentRuns = workouts
                    .filter { it.activityType == ActivityType.RUN }
                    .take(20)
                mapOf(
                    "SPD" to StatsCalculator.speedBreakdown(recentRuns, isWalk = false),
                    "END" to StatsCalculator.enduranceBreakdown(workouts),
                    "CON" to StatsCalculator.consistencyBreakdown(workouts),
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Stat breakdowns for Juniper (no VIT — Juniper doesn't weigh in)
    val juniperStatBreakdowns: StateFlow<Map<String, StatBreakdown>> =
        workoutRepository.getCompletedWorkoutsByType(ActivityType.DOG_WALK)
            .map { walks ->
                mapOf(
                    "SPD" to StatsCalculator.speedBreakdown(walks.take(20), isWalk = true),
                    "END" to StatsCalculator.enduranceBreakdown(walks),
                    "CON" to StatsCalculator.consistencyBreakdown(walks),
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // VIT stat state — only for user profile (ME tab)
    private val _vitalityState = MutableStateFlow(VitalityUiState())
    val vitalityState: StateFlow<VitalityUiState> = _vitalityState.asStateFlow()

    init {
        loadVitalityData()
    }

    private fun loadVitalityData() {
        viewModelScope.launch {
            val connected = healthConnectManager.isAvailable() && healthConnectManager.hasPermissions()
            if (!connected) {
                _vitalityState.value = VitalityUiState()
                return@launch
            }

            val weighInCount = bodyCompRepository.getWeighInCount(30)

            // Compute body fat trend: difference between earliest and latest reading in 30 days
            val now = Instant.now()
            val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)
            val readings = bodyCompRepository.getReadingsInRange(thirtyDaysAgo, now).first()
            val bodyFatReadings = readings
                .filter { it.bodyFatPercent != null }
                .sortedBy { it.timestamp }
            val bodyFatTrend = if (bodyFatReadings.size >= 2) {
                bodyFatReadings.last().bodyFatPercent!! - bodyFatReadings.first().bodyFatPercent!!
            } else {
                null
            }

            updateVitalityData(connected, weighInCount, bodyFatTrend)
        }
    }

    /**
     * Update VIT stat UI state from Health Connect data.
     */
    private fun updateVitalityData(
        connected: Boolean,
        weighInCount30d: Int,
        bodyFatTrendPercent: Double?,
    ) {
        val stat = if (connected) {
            StatsCalculator.calculateVitality(weighInCount30d, bodyFatTrendPercent)
        } else {
            1
        }
        _vitalityState.value = VitalityUiState(
            healthConnectConnected = connected,
            weighInCount30d = weighInCount30d,
            bodyFatTrendPercent = bodyFatTrendPercent,
            vitalityStat = stat,
            breakdown = if (connected) {
                StatsCalculator.vitalityBreakdown(weighInCount30d, bodyFatTrendPercent, stat)
            } else {
                null
            },
        )
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
}

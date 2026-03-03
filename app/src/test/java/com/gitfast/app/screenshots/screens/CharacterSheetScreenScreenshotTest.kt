package com.gitfast.app.screenshots.screens

import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.screenshots.FullScreenScreenshotTestBase
import com.gitfast.app.ui.character.CharacterSheetScreen
import com.gitfast.app.ui.character.CharacterSheetViewModel
import com.gitfast.app.ui.character.ForagingUiState
import com.gitfast.app.ui.character.StrengthUiState
import com.gitfast.app.ui.character.ToughnessUiState
import com.gitfast.app.ui.character.VitalityUiState
import com.gitfast.app.util.StatBreakdown
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class CharacterSheetScreenScreenshotTest : FullScreenScreenshotTestBase() {

    private fun createViewModel(selectedTab: Int = 0): CharacterSheetViewModel {
        return mockk<CharacterSheetViewModel>(relaxed = true) {
            every { this@mockk.selectedTab } returns MutableStateFlow(selectedTab)
            every { profile } returns MutableStateFlow(
                CharacterProfile(
                    level = 12,
                    totalXp = 4850,
                    xpForCurrentLevel = 4500,
                    xpForNextLevel = 5200,
                    xpProgressInLevel = 350,
                    xpProgress = 0.5f,
                    speedStat = 34,
                    enduranceStat = 28,
                    consistencyStat = 45,
                    vitalityStat = 22,
                    currentStreak = 3,
                    streakMultiplier = 1.2,
                ),
            )
            every { juniperProfile } returns MutableStateFlow(
                CharacterProfile(
                    level = 8,
                    totalXp = 2100,
                    xpForCurrentLevel = 1800,
                    xpForNextLevel = 2500,
                    xpProgressInLevel = 300,
                    xpProgress = 0.43f,
                    speedStat = 18,
                    enduranceStat = 22,
                    consistencyStat = 30,
                    vitalityStat = 1,
                    currentStreak = 3,
                    streakMultiplier = 1.2,
                ),
            )
            every { recentXpTransactions } returns MutableStateFlow(
                listOf(
                    XpTransaction(
                        id = "tx1",
                        workoutId = "run1",
                        xpAmount = 150,
                        reason = "3.12 mi run",
                        timestamp = Instant.parse("2026-02-24T07:55:00Z"),
                    ),
                    XpTransaction(
                        id = "tx2",
                        workoutId = "run2",
                        xpAmount = 120,
                        reason = "2.45 mi run",
                        timestamp = Instant.parse("2026-02-22T18:21:00Z"),
                    ),
                    XpTransaction(
                        id = "tx3",
                        workoutId = "achievement:first_5k",
                        xpAmount = 500,
                        reason = "Achievement: First 5K",
                        timestamp = Instant.parse("2026-02-20T07:33:00Z"),
                    ),
                ),
            )
            every { juniperXpTransactions } returns MutableStateFlow(
                listOf(
                    XpTransaction(
                        id = "jtx1",
                        workoutId = "walk1",
                        xpAmount = 80,
                        reason = "1.05 mi dog walk",
                        timestamp = Instant.parse("2026-02-24T12:22:00Z"),
                    ),
                    XpTransaction(
                        id = "jtx2",
                        workoutId = "walk2",
                        xpAmount = 70,
                        reason = "0.92 mi dog walk",
                        timestamp = Instant.parse("2026-02-22T11:30:00Z"),
                    ),
                ),
            )
            every { unlockedAchievementIds } returns MutableStateFlow(
                setOf("first_run", "first_5k", "streak_3", "speed_demon"),
            )
            every { juniperUnlockedAchievementIds } returns MutableStateFlow(
                setOf("first_walk", "good_boy"),
            )
            every { statBreakdowns } returns MutableStateFlow(
                mapOf(
                    "SPD" to StatBreakdown(
                        description = "Based on your recent pace",
                        details = listOf("Avg pace" to "8:14 /mi", "Best pace" to "6:52 /mi"),
                        brackets = "7:00-8:30 /mi = 25-50",
                        decayNote = "Decays after 14 days of inactivity",
                    ),
                    "END" to StatBreakdown(
                        description = "Based on distance and duration",
                        details = listOf("Total distance" to "142.3 mi", "Avg duration" to "28m"),
                        brackets = "100-200 mi = 20-40",
                        decayNote = "Decays after 14 days of inactivity",
                    ),
                    "CON" to StatBreakdown(
                        description = "Based on workout frequency",
                        details = listOf("Current streak" to "3 days", "Best streak" to "12 days"),
                        brackets = "3-7 day streak = 30-60",
                        decayNote = "Resets when streak breaks",
                    ),
                ),
            )
            every { juniperStatBreakdowns } returns MutableStateFlow(
                mapOf(
                    "SPD" to StatBreakdown(
                        description = "Based on walk pace",
                        details = listOf("Avg pace" to "21:40 /mi"),
                        brackets = "18:00-25:00 /mi = 10-30",
                        decayNote = "Decays after 14 days",
                    ),
                    "END" to StatBreakdown(
                        description = "Based on walk distance",
                        details = listOf("Total distance" to "28.5 mi"),
                        brackets = "20-50 mi = 15-35",
                        decayNote = "Decays after 14 days",
                    ),
                    "CON" to StatBreakdown(
                        description = "Based on walk frequency",
                        details = listOf("Current streak" to "3 days"),
                        brackets = "3-7 day streak = 20-50",
                        decayNote = "Resets when streak breaks",
                    ),
                ),
            )
            every { foragingState } returns MutableStateFlow(
                ForagingUiState(
                    foragingStat = 32,
                    breakdown = StatBreakdown(
                        description = "Based on total dog walk events logged",
                        details = listOf("Total events" to "18", "Effective score" to "32"),
                        brackets = "5→10 | 20→25 | 50→50 | 100→75 | 200→99",
                        decayNote = "Cumulative — never decays. Every walk event counts!",
                    ),
                ),
            )
            every { strengthState } returns MutableStateFlow(
                StrengthUiState(
                    strengthStat = 42,
                    breakdown = StatBreakdown(
                        description = "Based on 30-day exercise volume (reps × weight factor)",
                        details = listOf(
                            "Sets (30d)" to "24",
                            "Total reps" to "186",
                            "Weighted sets" to "18",
                            "Volume score" to "138",
                            "Effective score" to "42",
                        ),
                        brackets = "0→1 | 50→25 | 150→50 | 300→75 | 500→99",
                        decayNote = "Actively decays — uses a 30-day rolling window. Keep training!",
                    ),
                ),
            )
            every { toughnessState } returns MutableStateFlow(
                ToughnessUiState(
                    toughnessStat = 15,
                    breakdown = StatBreakdown(
                        description = "Based on 30-day soreness logs",
                        details = listOf("Logs (30d)" to "4", "Mild / Mod / Sev" to "2 / 1 / 1"),
                        brackets = "0→1 | 3→25 | 7→50 | 14→75 | 25→99",
                        decayNote = "Actively decays — 30-day window",
                    ),
                ),
            )
            every { vitalityState } returns MutableStateFlow(
                VitalityUiState(
                    healthConnectConnected = true,
                    weighInCount30d = 24,
                    bodyFatTrendPercent = -0.7,
                    vitalityStat = 22,
                    breakdown = StatBreakdown(
                        description = "Based on body composition tracking",
                        details = listOf("Weigh-ins (30d)" to "24", "Body fat trend" to "-0.7%"),
                        brackets = "15+ weigh-ins = 15-30",
                        decayNote = "Decays without regular weigh-ins",
                    ),
                ),
            )
        }
    }

    @Test
    fun `Screen CharacterSheet User`() {
        captureScreenshot("Screen_CharacterSheet_User", category = "character") {
            CharacterSheetScreen(
                onBackClick = {},
                viewModel = createViewModel(selectedTab = 0),
            )
        }
    }

    @Test
    fun `Screen CharacterSheet Juniper`() {
        captureScreenshot("Screen_CharacterSheet_Juniper", category = "character") {
            CharacterSheetScreen(
                onBackClick = {},
                viewModel = createViewModel(selectedTab = 1),
            )
        }
    }
}

package com.gitfast.app.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitfast.app.data.model.CharacterProfile
import com.gitfast.app.data.model.XpTransaction
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.CyanAccent
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.util.AchievementCategory
import com.gitfast.app.util.AchievementDef
import com.gitfast.app.util.StreakCalculator
import com.gitfast.app.util.XpCalculator
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val tabs = listOf("ME", "JUNIPER")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetScreen(
    onBackClick: () -> Unit,
    viewModel: CharacterSheetViewModel = hiltViewModel(),
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    // User data
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val transactions by viewModel.recentXpTransactions.collectAsStateWithLifecycle()
    val unlockedIds by viewModel.unlockedAchievementIds.collectAsStateWithLifecycle()

    // Juniper data
    val juniperProfile by viewModel.juniperProfile.collectAsStateWithLifecycle()
    val juniperTransactions by viewModel.juniperXpTransactions.collectAsStateWithLifecycle()
    val juniperUnlockedIds by viewModel.juniperUnlockedAchievementIds.collectAsStateWithLifecycle()

    // Active data based on selected tab
    val activeProfile = if (selectedTab == 0) profile else juniperProfile
    val activeTransactions = if (selectedTab == 0) transactions else juniperTransactions
    val activeUnlockedIds = if (selectedTab == 0) unlockedIds else juniperUnlockedIds
    val activeProfileId = if (selectedTab == 0) 1 else 2

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedTab == 0) "Character" else "Juniper",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = NeonGreen,
                            )
                        }
                    },
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selectedTab == index) NeonGreen
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                LevelSection(profile = activeProfile, isJuniper = selectedTab == 1)
            }

            item {
                XpProgressSection(profile = activeProfile)
            }

            item {
                StatsSection(profile = activeProfile)
            }

            item {
                StreakSection(profile = activeProfile)
            }

            item {
                AchievementsSection(
                    unlockedIds = activeUnlockedIds,
                    profileId = activeProfileId,
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "> XP Log",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (activeTransactions.isEmpty()) {
                item {
                    Text(
                        text = if (selectedTab == 0) "No XP earned yet. Complete a workout!"
                        else "No XP earned yet. Take Juniper for a walk!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(activeTransactions) { tx ->
                    XpTransactionRow(transaction = tx)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LevelSection(profile: CharacterProfile, isJuniper: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        if (isJuniper) {
            PixelDog(modifier = Modifier.size(80.dp))
        } else {
            PixelRunner(modifier = Modifier.size(80.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "LEVEL",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${profile.level}",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "${profile.totalXp} total XP",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun XpProgressSection(profile: CharacterProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "LV ${profile.level}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "LV ${profile.level + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { profile.xpProgress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${profile.xpProgressInLevel} / ${XpCalculator.xpCostForLevel(profile.level)} XP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatsSection(profile: CharacterProfile) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "> Character Stats",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        StatBar(label = "SPD", value = profile.speedStat, color = CyanAccent)
        Spacer(modifier = Modifier.height(8.dp))
        StatBar(label = "END", value = profile.enduranceStat, color = AmberAccent)
        Spacer(modifier = Modifier.height(8.dp))
        StatBar(label = "CON", value = profile.consistencyStat, color = NeonGreen)
    }
}

@Composable
private fun StatBar(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.width(36.dp),
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = value / 99f)
                    .height(10.dp)
                    .clip(RectangleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun StreakSection(profile: CharacterProfile) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "> Streak",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RectangleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                if (profile.currentStreak >= 2) {
                    Text(
                        text = "${profile.currentStreak}-day streak",
                        style = MaterialTheme.typography.titleSmall,
                        color = AmberAccent,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${StreakCalculator.getMultiplierLabel(profile.currentStreak)} XP multiplier",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else if (profile.currentStreak == 1) {
                    Text(
                        text = "1-day streak",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Work out tomorrow for 1.1x XP!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = "No active streak",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Complete a workout to start!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (profile.currentStreak >= 2) {
                val daysToMax = if (profile.currentStreak >= 6) 0 else 6 - profile.currentStreak
                if (daysToMax > 0) {
                    Text(
                        text = "$daysToMax to max",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = "MAX",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementsSection(unlockedIds: Set<String>, profileId: Int) {
    val byCategory = AchievementDef.byCategory(profileId)
    val totalCount = AchievementDef.entries.count { it.profileId == profileId }
    val unlockedCount = unlockedIds.size

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "> Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$unlockedCount / $totalCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        for ((category, achievements) in byCategory) {
            Text(
                text = categoryLabel(category),
                style = MaterialTheme.typography.labelSmall,
                color = CyanAccent,
            )
            Spacer(modifier = Modifier.height(4.dp))
            for (achievement in achievements) {
                val isUnlocked = achievement.id in unlockedIds
                AchievementRow(achievement = achievement, isUnlocked = isUnlocked)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AchievementRow(achievement: AchievementDef, isUnlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .background(
                if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isUnlocked) achievement.icon else "[?]",
            style = MaterialTheme.typography.labelMedium,
            color = if (isUnlocked) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.width(48.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isUnlocked) achievement.title else "???",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            )
        }
        Text(
            text = "+${achievement.xpReward}",
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        )
    }
}

private fun categoryLabel(category: AchievementCategory): String {
    return when (category) {
        AchievementCategory.DISTANCE -> "DISTANCE"
        AchievementCategory.FREQUENCY -> "FREQUENCY"
        AchievementCategory.STREAK -> "STREAKS"
        AchievementCategory.LAPS -> "LAPS"
        AchievementCategory.DOG_WALK -> "DOG WALKS"
        AchievementCategory.LEVELING -> "LEVELING"
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
private fun XpTransactionRow(transaction: XpTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.reason.split(";").firstOrNull()?.trim() ?: "Workout",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = transaction.timestamp.atZone(ZoneId.systemDefault()).format(dateFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "+${transaction.xpAmount} XP",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * 16x16 pixel art running man drawn on Canvas.
 * Mid-stride pose with neon green body and cyan accents.
 */
@Composable
private fun PixelRunner(modifier: Modifier = Modifier) {
    val primary = NeonGreen
    val skin = Color(0xFFE6C8A0)
    val hair = CyanAccent
    val shoe = AmberAccent

    // 16x16 grid — each '1' marks a filled pixel
    // Row-major: (row, col, color)
    Canvas(modifier = modifier) {
        val px = size.width / 16f

        fun fill(row: Int, col: Int, color: Color) {
            drawRect(
                color = color,
                topLeft = Offset(col * px, row * px),
                size = Size(px, px),
            )
        }

        // Hair / head top (rows 0-1)
        fill(0, 6, hair); fill(0, 7, hair); fill(0, 8, hair); fill(0, 9, hair)
        fill(1, 5, hair); fill(1, 6, hair); fill(1, 7, hair); fill(1, 8, hair); fill(1, 9, hair)

        // Face (rows 2-3)
        fill(2, 6, skin); fill(2, 7, skin); fill(2, 8, skin); fill(2, 9, skin)
        fill(3, 6, skin); fill(3, 7, skin); fill(3, 8, skin); fill(3, 9, skin)

        // Neck (row 4)
        fill(4, 7, skin); fill(4, 8, skin)

        // Torso (rows 5-8) — shirt
        fill(5, 5, primary); fill(5, 6, primary); fill(5, 7, primary); fill(5, 8, primary); fill(5, 9, primary); fill(5, 10, primary)
        fill(6, 5, primary); fill(6, 6, primary); fill(6, 7, primary); fill(6, 8, primary); fill(6, 9, primary); fill(6, 10, primary)
        fill(7, 6, primary); fill(7, 7, primary); fill(7, 8, primary); fill(7, 9, primary)
        fill(8, 6, primary); fill(8, 7, primary); fill(8, 8, primary); fill(8, 9, primary)

        // Arms — left arm back, right arm forward (running pose)
        fill(5, 4, skin); fill(6, 3, skin); fill(7, 2, skin) // left arm (back)
        fill(5, 11, skin); fill(6, 12, skin); fill(7, 13, skin) // right arm (forward)

        // Shorts (rows 9-10)
        fill(9, 6, hair); fill(9, 7, hair); fill(9, 8, hair); fill(9, 9, hair)
        fill(10, 5, hair); fill(10, 6, hair); fill(10, 8, hair); fill(10, 9, hair)

        // Legs — stride pose (rows 11-13)
        fill(11, 4, skin); fill(11, 5, skin); fill(11, 9, skin); fill(11, 10, skin)
        fill(12, 3, skin); fill(12, 4, skin); fill(12, 10, skin); fill(12, 11, skin)
        fill(13, 2, skin); fill(13, 3, skin); fill(13, 11, skin); fill(13, 12, skin)

        // Shoes (rows 14-15)
        fill(14, 1, shoe); fill(14, 2, shoe); fill(14, 3, shoe)
        fill(14, 11, shoe); fill(14, 12, shoe); fill(14, 13, shoe)
        fill(15, 1, shoe); fill(15, 2, shoe)
        fill(15, 12, shoe); fill(15, 13, shoe)
    }
}

/**
 * 16x16 pixel art dog drawn on Canvas.
 * Side profile with neon green collar and amber body.
 */
@Composable
private fun PixelDog(modifier: Modifier = Modifier) {
    val body = AmberAccent
    val dark = Color(0xFFC06820) // darker shade for ears/detail
    val collar = NeonGreen
    val nose = Color(0xFF2D2D2D)
    val eye = Color.White
    val tongue = Color(0xFFFF6B8A)

    Canvas(modifier = modifier) {
        val px = size.width / 16f

        fun fill(row: Int, col: Int, color: Color) {
            drawRect(
                color = color,
                topLeft = Offset(col * px, row * px),
                size = Size(px, px),
            )
        }

        // Ears (rows 1-3)
        fill(1, 2, dark); fill(1, 3, dark)
        fill(2, 1, dark); fill(2, 2, dark); fill(2, 3, dark)
        fill(3, 2, body)

        // Head (rows 3-6)
        fill(3, 3, body); fill(3, 4, body); fill(3, 5, body); fill(3, 6, body)
        fill(4, 2, body); fill(4, 3, body); fill(4, 4, body); fill(4, 5, body); fill(4, 6, body); fill(4, 7, body)
        fill(5, 2, body); fill(5, 3, body); fill(5, 4, body); fill(5, 5, body); fill(5, 6, body); fill(5, 7, body); fill(5, 8, body)

        // Eye
        fill(4, 4, eye)

        // Snout + nose (rows 6-7)
        fill(6, 3, body); fill(6, 4, body); fill(6, 5, body); fill(6, 6, body); fill(6, 7, body); fill(6, 8, body); fill(6, 9, body)
        fill(7, 6, body); fill(7, 7, body); fill(7, 8, body); fill(7, 9, body); fill(7, 10, nose)
        fill(6, 10, nose); fill(6, 11, nose)

        // Tongue
        fill(8, 9, tongue); fill(8, 10, tongue)

        // Collar (row 7 on neck)
        fill(7, 3, collar); fill(7, 4, collar); fill(7, 5, collar)

        // Body (rows 8-11)
        fill(8, 2, body); fill(8, 3, body); fill(8, 4, body); fill(8, 5, body); fill(8, 6, body); fill(8, 7, body); fill(8, 8, body)
        fill(9, 1, body); fill(9, 2, body); fill(9, 3, body); fill(9, 4, body); fill(9, 5, body); fill(9, 6, body); fill(9, 7, body); fill(9, 8, body)
        fill(10, 1, body); fill(10, 2, body); fill(10, 3, body); fill(10, 4, body); fill(10, 5, body); fill(10, 6, body); fill(10, 7, body); fill(10, 8, body)
        fill(11, 1, body); fill(11, 2, body); fill(11, 3, body); fill(11, 4, body); fill(11, 5, body); fill(11, 6, body); fill(11, 7, body); fill(11, 8, body)

        // Tail (rows 7-9, curling up)
        fill(9, 0, body)
        fill(8, 0, dark); fill(7, 0, dark)
        fill(6, 0, dark); fill(6, 1, dark)

        // Front legs (rows 12-14)
        fill(12, 6, body); fill(12, 7, body)
        fill(13, 6, body); fill(13, 7, body)
        fill(14, 6, dark); fill(14, 7, dark) // paws

        // Back legs (rows 12-14)
        fill(12, 2, body); fill(12, 3, body)
        fill(13, 2, body); fill(13, 3, body)
        fill(14, 2, dark); fill(14, 3, dark) // paws
    }
}

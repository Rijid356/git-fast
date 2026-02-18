package com.gitfast.app.util

enum class AchievementCategory {
    DISTANCE,
    FREQUENCY,
    STREAK,
    LAPS,
    DOG_WALK,
    LEVELING,
}

enum class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val icon: String,
    val category: AchievementCategory,
) {
    // Distance milestones (cumulative)
    FIRST_MILE(
        id = "dist_first_mile",
        title = "First Mile",
        description = "Run a total of 1 mile",
        xpReward = 25,
        icon = "[>]",
        category = AchievementCategory.DISTANCE,
    ),
    MARATHON_CLUB(
        id = "dist_marathon",
        title = "Marathon Club",
        description = "Run a total of 26.2 miles",
        xpReward = 100,
        icon = "[M]",
        category = AchievementCategory.DISTANCE,
    ),
    CENTURY_RUNNER(
        id = "dist_century",
        title = "Century Runner",
        description = "Run a total of 100 miles",
        xpReward = 200,
        icon = "[C]",
        category = AchievementCategory.DISTANCE,
    ),
    ULTRA_RUNNER(
        id = "dist_ultra",
        title = "Ultra Runner",
        description = "Run a total of 250 miles",
        xpReward = 500,
        icon = "[U]",
        category = AchievementCategory.DISTANCE,
    ),

    // Single-workout PRs
    FIVE_K_FINISHER(
        id = "pr_5k",
        title = "5K Finisher",
        description = "Complete a 3.1 mile run",
        xpReward = 50,
        icon = "[5K]",
        category = AchievementCategory.DISTANCE,
    ),
    TEN_K_FINISHER(
        id = "pr_10k",
        title = "10K Finisher",
        description = "Complete a 6.2 mile run",
        xpReward = 100,
        icon = "[10K]",
        category = AchievementCategory.DISTANCE,
    ),
    HALF_MARATHON(
        id = "pr_half",
        title = "Half Marathon",
        description = "Complete a 13.1 mile run",
        xpReward = 250,
        icon = "[HM]",
        category = AchievementCategory.DISTANCE,
    ),

    // Workout count
    FIRST_STEPS(
        id = "count_1",
        title = "First Steps",
        description = "Complete your first workout",
        xpReward = 10,
        icon = "[1]",
        category = AchievementCategory.FREQUENCY,
    ),
    GETTING_STARTED(
        id = "count_5",
        title = "Getting Started",
        description = "Complete 5 workouts",
        xpReward = 25,
        icon = "[5]",
        category = AchievementCategory.FREQUENCY,
    ),
    DEDICATED(
        id = "count_25",
        title = "Dedicated",
        description = "Complete 25 workouts",
        xpReward = 75,
        icon = "[25]",
        category = AchievementCategory.FREQUENCY,
    ),
    COMMITTED(
        id = "count_50",
        title = "Committed",
        description = "Complete 50 workouts",
        xpReward = 150,
        icon = "[50]",
        category = AchievementCategory.FREQUENCY,
    ),
    CENTURION(
        id = "count_100",
        title = "Centurion",
        description = "Complete 100 workouts",
        xpReward = 300,
        icon = "[100]",
        category = AchievementCategory.FREQUENCY,
    ),

    // Streaks (consecutive days)
    THREE_PEAT(
        id = "streak_3",
        title = "Three-Peat",
        description = "Work out 3 days in a row",
        xpReward = 30,
        icon = "[3d]",
        category = AchievementCategory.STREAK,
    ),
    WEEK_WARRIOR(
        id = "streak_7",
        title = "Week Warrior",
        description = "Work out 7 days in a row",
        xpReward = 75,
        icon = "[7d]",
        category = AchievementCategory.STREAK,
    ),
    FORTNIGHT_FORCE(
        id = "streak_14",
        title = "Fortnight Force",
        description = "Work out 14 days in a row",
        xpReward = 150,
        icon = "[14d]",
        category = AchievementCategory.STREAK,
    ),

    // Laps
    LAP_LEADER(
        id = "laps_10",
        title = "Lap Leader",
        description = "Complete 10 total laps",
        xpReward = 50,
        icon = "[L10]",
        category = AchievementCategory.LAPS,
    ),
    TRACK_STAR(
        id = "laps_50",
        title = "Track Star",
        description = "Complete 50 total laps",
        xpReward = 150,
        icon = "[L50]",
        category = AchievementCategory.LAPS,
    ),

    // Dog Walks
    GOOD_BOY(
        id = "dog_1",
        title = "Good Boy",
        description = "Complete your first dog walk",
        xpReward = 15,
        icon = "[D]",
        category = AchievementCategory.DOG_WALK,
    ),
    DOGS_BEST_FRIEND(
        id = "dog_25",
        title = "Dog's Best Friend",
        description = "Complete 25 dog walks",
        xpReward = 100,
        icon = "[D25]",
        category = AchievementCategory.DOG_WALK,
    ),

    // Leveling
    LEVEL_5(
        id = "level_5",
        title = "Level 5",
        description = "Reach level 5",
        xpReward = 50,
        icon = "[L5]",
        category = AchievementCategory.LEVELING,
    ),
    LEVEL_10(
        id = "level_10",
        title = "Level 10",
        description = "Reach level 10",
        xpReward = 100,
        icon = "[L10]",
        category = AchievementCategory.LEVELING,
    );

    companion object {
        fun byCategory(): Map<AchievementCategory, List<AchievementDef>> =
            entries.groupBy { it.category }

        fun findById(id: String): AchievementDef? =
            entries.find { it.id == id }
    }
}

package com.gitfast.app.util

enum class AchievementCategory {
    DISTANCE,
    FREQUENCY,
    STREAK,
    LAPS,
    DOG_WALK,
    DOG_WALK_EVENT,
    BODY_COMP,
    LEVELING,
}

enum class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val icon: String,
    val category: AchievementCategory,
    val profileId: Int = 1,
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

    // Dog Walks (user profile)
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

    // Dog Walks (Juniper profile)
    JUNIPER_FIRST_SNIFF(
        id = "juniper_first_sniff",
        title = "First Sniff",
        description = "Complete Juniper's first walk",
        xpReward = 15,
        icon = "[J1]",
        category = AchievementCategory.DOG_WALK,
        profileId = 2,
    ),
    JUNIPER_TRAIL_SNIFFER(
        id = "juniper_trail_sniffer",
        title = "Trail Sniffer",
        description = "Walk 10 miles with Juniper",
        xpReward = 50,
        icon = "[J10]",
        category = AchievementCategory.DOG_WALK,
        profileId = 2,
    ),
    JUNIPER_ADVENTURE_PUP(
        id = "juniper_adventure_pup",
        title = "Adventure Pup",
        description = "Walk 25 miles with Juniper",
        xpReward = 100,
        icon = "[J25]",
        category = AchievementCategory.DOG_WALK,
        profileId = 2,
    ),
    JUNIPER_PACK_LEADER(
        id = "juniper_pack_leader",
        title = "Pack Leader",
        description = "Complete 10 walks with Juniper",
        xpReward = 50,
        icon = "[JP]",
        category = AchievementCategory.DOG_WALK,
        profileId = 2,
    ),
    JUNIPER_TRAIL_MASTER(
        id = "juniper_trail_master",
        title = "Trail Master",
        description = "Complete 50 walks with Juniper",
        xpReward = 200,
        icon = "[JM]",
        category = AchievementCategory.DOG_WALK,
        profileId = 2,
    ),
    JUNIPER_GOOD_GIRL(
        id = "juniper_good_girl",
        title = "Good Girl",
        description = "Reach level 5 with Juniper",
        xpReward = 50,
        icon = "[JG]",
        category = AchievementCategory.LEVELING,
        profileId = 2,
    ),

    // Dog Walk Events (Juniper profile)
    JUNIPER_FIRST_FIND(
        id = "juniper_first_find",
        title = "First Find",
        description = "Log your first dog walk event",
        xpReward = 10,
        icon = "[F1]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_KEEN_NOSE(
        id = "juniper_keen_nose",
        title = "Keen Nose",
        description = "Log 10 Deep Sniffs",
        xpReward = 25,
        icon = "[KN]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_SNACK_HUNTER(
        id = "juniper_snack_hunter",
        title = "Snack Hunter",
        description = "Find 10 snacks on walks",
        xpReward = 25,
        icon = "[SH]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_SQUIRREL_NEMESIS(
        id = "juniper_squirrel_nemesis",
        title = "Squirrel Nemesis",
        description = "Chase 5 squirrels",
        xpReward = 30,
        icon = "[SN]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_SOCIAL_BUTTERFLY(
        id = "juniper_social_butterfly",
        title = "Social Butterfly",
        description = "Meet 10 friendly dogs",
        xpReward = 25,
        icon = "[SB]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_ZOOMIE_CHAMPION(
        id = "juniper_zoomie_champ",
        title = "Zoomie Champion",
        description = "Have 10 zoomie bursts",
        xpReward = 25,
        icon = "[ZC]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_ADVENTURE_LOG_50(
        id = "juniper_log_50",
        title = "Adventure Log 50",
        description = "Log 50 total events across all walks",
        xpReward = 50,
        icon = "[L50]",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_HYDRATION_HERO(
        id = "juniper_hydration_hero",
        title = "Hydration Hero",
        description = "Logged 10 water breaks",
        xpReward = 30,
        icon = "💦",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),
    JUNIPER_VOCAL_PUP(
        id = "juniper_vocal_pup",
        title = "Vocal Pup",
        description = "Logged 10 bark/react events",
        xpReward = 30,
        icon = "🗣️",
        category = AchievementCategory.DOG_WALK_EVENT,
        profileId = 2,
    ),

    // Body Composition
    FIRST_WEIGH_IN(
        id = "body_first_weigh_in",
        title = "First Weigh-In",
        description = "Sync your first weight reading",
        xpReward = 10,
        icon = "[W1]",
        category = AchievementCategory.BODY_COMP,
    ),
    WEEK_TRACKER(
        id = "body_week_tracker",
        title = "Week Tracker",
        description = "7-day weigh-in streak",
        xpReward = 30,
        icon = "[W7]",
        category = AchievementCategory.BODY_COMP,
    ),
    MONTH_TRACKER(
        id = "body_month_tracker",
        title = "Month Tracker",
        description = "28-day weigh-in streak",
        xpReward = 75,
        icon = "[W28]",
        category = AchievementCategory.BODY_COMP,
    ),
    BODY_AWARE(
        id = "body_aware",
        title = "Body Aware",
        description = "Record 50 weigh-ins",
        xpReward = 50,
        icon = "[B50]",
        category = AchievementCategory.BODY_COMP,
    ),
    CENTURION_SCALE(
        id = "body_centurion_scale",
        title = "Centurion Scale",
        description = "Record 100 weigh-ins",
        xpReward = 150,
        icon = "[B100]",
        category = AchievementCategory.BODY_COMP,
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
        fun byCategory(profileId: Int? = null): Map<AchievementCategory, List<AchievementDef>> {
            val filtered = if (profileId != null) {
                entries.filter { it.profileId == profileId }
            } else {
                entries.toList()
            }
            return filtered.groupBy { it.category }
        }

        fun findById(id: String): AchievementDef? =
            entries.find { it.id == id }
    }
}

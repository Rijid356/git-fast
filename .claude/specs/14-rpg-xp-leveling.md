# Checkpoint 14: RPG XP & Leveling System

## Overview
Adds an RPG-style experience points (XP) and leveling system. Every completed workout earns XP based on distance, duration, laps, phase completion, and weather conditions. XP accumulates toward levels with an increasing cost curve.

## XP Formula
- **Distance:** 10 XP/mile (run), 8 XP/mile (walk)
- **Duration:** 5 XP per 10 minutes active
- **Laps:** 20 XP per completed lap
- **All-phases bonus:** +15 XP for warmup+laps+cooldown
- **Weather multiplier:** 1.25x rain/snow, 1.1x hot/cold
- **Minimum:** 5 XP per workout

## Leveling Curve
- `xpForLevel(n) = 25 * n * (n-1)`
- Level 1 = 0 XP, Level 2 = 50 XP, Level 3 = 150 XP, Level 4 = 300 XP...
- Each level costs 50 more XP than the previous

## Database Changes
- **Version:** 2 → 3 (Migration_2_3)
- **New tables:** `character_profile` (single row, id=1), `xp_transactions` (FK to workouts)

## Files Created
- `data/local/entity/CharacterProfileEntity.kt`
- `data/local/entity/XpTransactionEntity.kt`
- `data/local/migrations/Migration_2_3.kt`
- `data/local/CharacterDao.kt`
- `data/model/CharacterProfile.kt`
- `data/model/XpTransaction.kt`
- `data/repository/CharacterRepository.kt`
- `util/XpCalculator.kt`
- `ui/character/CharacterSheetScreen.kt`
- `ui/character/CharacterSheetViewModel.kt`

## Files Modified
- `data/local/GitFastDatabase.kt` — version bump, new entities/DAO
- `data/repository/WorkoutSaveManager.kt` — returns `SaveResult`, awards XP
- `di/DatabaseModule.kt` — migration, CharacterDao/CharacterRepository providers
- `ui/workout/ActiveWorkoutViewModel.kt` — XP preview in summary stats
- `ui/workout/WorkoutSummaryScreen.kt` — displays "+N XP" earned
- `ui/home/HomeViewModel.kt` — exposes CharacterProfile
- `ui/home/HomeScreen.kt` — LevelBadge composable
- `navigation/GitFastNavGraph.kt` — CharacterSheet route, xpEarned param

## Key Design Decisions
- **Idempotent XP awarding:** `awardXp()` checks for existing transaction by workoutId before inserting
- **XP preview in ViewModel:** Calculated locally before service saves to show instantly on summary screen
- **Single-row profile:** `character_profile` always has `id=1`, upserted on first XP award

## Tests
- `XpCalculatorTest.kt` — 20 tests covering XP formula, leveling math, weather multipliers
- Updated `WorkoutSaveManagerTest.kt` — now provides CharacterRepository, checks SaveResult
- Updated `WorkoutSaveManagerPhaseTest.kt` — same constructor fix

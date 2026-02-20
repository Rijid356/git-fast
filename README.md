# git-fast

A native Android running & workout tracker with RPG gamification. Track GPS during runs and dog walks, earn XP, level up your character, and unlock achievements.

**Package:** `com.gitfast.app` | **Min SDK** 26 | **Target SDK** 35 | **Kotlin** 2.1.0 | **Compose** + Material3

---

## Architecture Overview

```mermaid
graph TB
    subgraph UI["UI Layer — Jetpack Compose"]
        Home[HomeScreen]
        Workout[ActiveWorkoutScreen]
        Summary[WorkoutSummaryScreen]
        DogSummary[DogWalkSummaryScreen]
        History[HistoryScreen]
        Detail[DetailScreen]
        Character[CharacterSheetScreen]
        Settings[SettingsScreen]
    end

    subgraph VM["ViewModel Layer"]
        HomeVM[HomeViewModel]
        WorkoutVM[ActiveWorkoutViewModel]
        HistoryVM[HistoryViewModel]
        DetailVM[DetailViewModel]
        DogVM[DogWalkSummaryViewModel]
    end

    subgraph Service["Service Layer"]
        WS["WorkoutService<br/><i>LifecycleService</i>"]
        WSM["WorkoutStateManager<br/><i>@Singleton</i>"]
        GPS["GpsTracker<br/><i>FusedLocation</i>"]
        Step["StepTracker<br/><i>SensorManager</i>"]
        APD[AutoPauseDetector]
    end

    subgraph Data["Data Layer"]
        Repo[WorkoutRepository]
        CharRepo[CharacterRepository]
        SaveMgr[WorkoutSaveManager]
        DAO[(WorkoutDao)]
        CharDAO[(CharacterDao)]
        DB[(Room DB v7)]
    end

    subgraph Util["Utilities"]
        XpCalc[XpCalculator]
        StreakCalc[StreakCalculator]
        AchCheck[AchievementChecker]
        StatsCalc[StatsCalculator]
    end

    Home --> HomeVM
    Workout --> WorkoutVM
    History --> HistoryVM
    Detail --> DetailVM
    DogSummary --> DogVM

    WorkoutVM -- ServiceConnection/Binder --> WS
    WS --> WSM
    GPS --> WSM
    Step --> WSM
    APD --> WSM

    WS -- stop --> SaveMgr
    SaveMgr --> Repo
    SaveMgr --> CharRepo
    SaveMgr --> XpCalc
    SaveMgr --> AchCheck
    SaveMgr --> StreakCalc

    Repo --> DAO --> DB
    CharRepo --> CharDAO --> DB

    HomeVM --> Repo
    HomeVM --> CharRepo
    HistoryVM --> Repo
    DetailVM --> Repo
    DogVM --> Repo
```

---

## Data Flow — GPS to Persistence

```mermaid
flowchart LR
    A["FusedLocationProvider<br/>(GPS Hardware)"] -->|Location updates| B[GpsTracker]
    B -->|"Flow&lt;GpsPoint&gt;"| C[WorkoutService]
    C -->|addGpsPoint| D["WorkoutStateManager<br/>(StateFlow)"]

    E[SensorManager] -->|Step events| F[StepTracker]
    F -->|updateStepCount| D

    D -->|"observe StateFlow"| G[ActiveWorkoutViewModel]
    G -->|"Compose state"| H["ActiveWorkoutScreen<br/>(Live Metrics)"]

    D -->|"stopWorkout()"| I[WorkoutSnapshot]
    I --> J[WorkoutSaveManager]

    J -->|"@Transaction"| K[WorkoutRepository]
    K --> L[("Room DB<br/>workouts · phases<br/>laps · gps_points")]

    J --> M[XpCalculator]
    J --> N[AchievementChecker]
    M --> O[CharacterRepository]
    N --> O
    O --> P[("Room DB<br/>character_profiles<br/>xp_transactions<br/>achievements")]
```

---

## Navigation Graph

```mermaid
stateDiagram-v2
    [*] --> Home

    Home --> Workout : Start Run / Dog Walk
    Home --> History
    Home --> CharacterSheet
    Home --> Settings

    Workout --> WorkoutSummary : Stop (RUN)
    Workout --> DogWalkSummary : Stop (DOG_WALK)
    Workout --> Home : Discard

    WorkoutSummary --> Home : Done
    DogWalkSummary --> Home : Save

    History --> WorkoutDetail : Tap workout
    WorkoutDetail --> History : Back

    CharacterSheet --> Home : Back
    Settings --> Home : Back

    note right of Workout
        Receives activityType param
        (RUN or DOG_WALK)
    end note

    note right of WorkoutSummary
        URL-encoded query params
        Achievements pipe-delimited
    end note
```

---

## Database Schema

```mermaid
erDiagram
    WORKOUTS {
        string id PK
        long startTime
        long endTime
        double distanceMeters
        string status "ACTIVE | COMPLETED | DISCARDED"
        string activityType "RUN | DOG_WALK"
        string dogName "nullable"
        string notes "nullable"
        string weather "nullable"
        string energy "nullable"
        string routeTag "nullable"
    }

    WORKOUT_PHASES {
        string id PK
        string workoutId FK
        string type "WARMUP | LAPS | COOLDOWN"
        long startTime
        long endTime
        double distanceMeters
        int steps
    }

    LAPS {
        string id PK
        string phaseId FK
        int lapNumber
        long startTime
        long endTime
        double distanceMeters
        int steps
        double splitLatitude "nullable"
        double splitLongitude "nullable"
    }

    GPS_POINTS {
        long id PK "auto"
        string workoutId FK
        double latitude
        double longitude
        long timestamp
        float accuracy
        int sortIndex
        float speed
    }

    ROUTE_TAGS {
        string name PK
        long createdAt
        long lastUsed
    }

    CHARACTER_PROFILES {
        int id PK "1=User 2=Juniper"
        int totalXp
        int level
        long createdAt
        int speedStat
        int enduranceStat
        int consistencyStat
    }

    XP_TRANSACTIONS {
        string id PK
        string workoutId FK
        int xpAmount
        string reason
        long timestamp
        int profileId
    }

    UNLOCKED_ACHIEVEMENTS {
        string achievementId PK
        int profileId PK
        long unlockedAt
        int xpAwarded
    }

    WORKOUTS ||--o{ WORKOUT_PHASES : "has phases"
    WORKOUTS ||--o{ GPS_POINTS : "has trace"
    WORKOUTS ||--o{ XP_TRANSACTIONS : "earns XP"
    WORKOUT_PHASES ||--o{ LAPS : "has laps"
    CHARACTER_PROFILES ||--o{ UNLOCKED_ACHIEVEMENTS : "unlocks"
```

---

## Service Lifecycle

```mermaid
stateDiagram-v2
    [*] --> Created : onCreate()

    Created --> Tracking : ACTION_START
    Tracking --> Paused : ACTION_PAUSE
    Tracking --> AutoPaused : Speed below threshold
    Paused --> Tracking : ACTION_RESUME
    AutoPaused --> Tracking : Speed resumes

    Tracking --> LapPhase : ACTION_START_LAPS
    LapPhase --> LapPhase : ACTION_MARK_LAP
    LapPhase --> Tracking : ACTION_END_LAPS

    Tracking --> Stopped : ACTION_STOP
    Paused --> Stopped : ACTION_STOP
    LapPhase --> Stopped : ACTION_STOP
    Tracking --> Discarded : ACTION_DISCARD

    Stopped --> [*] : stopSelf()
    Discarded --> [*] : stopSelf()

    state Tracking {
        [*] --> CollectGPS
        [*] --> CollectSteps
        [*] --> UpdateTimer
        [*] --> UpdateNotification
    }

    state Stopped {
        [*] --> CreateSnapshot
        CreateSnapshot --> SaveWorkout
        SaveWorkout --> AwardXP
        AwardXP --> CheckAchievements
    }
```

---

## Workout Phase Flow

```mermaid
flowchart LR
    Start(("Start<br/>Workout")) --> W["WARMUP Phase"]
    W -->|"startLaps()"| L["LAPS Phase"]
    L -->|"markLap()"| L
    L -->|"endLaps()"| C["COOLDOWN Phase"]
    C -->|"stopWorkout()"| Save(("Save &<br/>Summary"))

    style Start fill:#39FF14,color:#000
    style Save fill:#39FF14,color:#000
    style W fill:#0D1117,stroke:#58A6FF,color:#fff
    style L fill:#0D1117,stroke:#58A6FF,color:#fff
    style C fill:#0D1117,stroke:#58A6FF,color:#fff
```

---

## RPG & XP System

```mermaid
flowchart TB
    Finish["Workout Completed"] --> Calc["XpCalculator"]
    Calc --> Base["Base XP<br/>(distance × 10) + (duration ÷ 10)"]
    Base --> Streak["Apply Streak Multiplier<br/>1.0x → 1.5x cap"]
    Streak --> Award["CharacterRepository.awardXp()<br/><i>Idempotent: checks (workoutId, profileId)</i>"]

    Award --> User["Profile 1: User<br/>All workouts"]
    Award -->|"DOG_WALK only"| Dog["Profile 2: Juniper<br/>Dog walks only"]

    Finish --> Achieve["AchievementChecker"]
    Achieve --> Check{"New achievements?"}
    Check -->|Yes| Unlock["Unlock + Bonus XP"]
    Check -->|No| Done["Done"]
    Unlock --> Done

    User --> Stats["StatsCalculator<br/>Speed · Endurance · Consistency<br/>(1–99 RPG brackets)"]
    Dog --> Stats

    style Finish fill:#39FF14,color:#000
    style Done fill:#39FF14,color:#000
```

---

## Dependency Injection

```mermaid
graph TB
    subgraph DatabaseModule["DatabaseModule (@Singleton)"]
        RoomDB["GitFastDatabase"] --> WorkoutDao
        RoomDB --> CharacterDao
        WorkoutDao --> WorkoutRepo[WorkoutRepository]
        CharacterDao --> CharacterRepo[CharacterRepository]
        WorkoutRepo --> SaveManager[WorkoutSaveManager]
        CharacterRepo --> SaveManager
        StateStore[WorkoutStateStore<br/><i>SharedPrefs: crash recovery</i>]
    end

    subgraph ServiceModule["ServiceModule (@Singleton)"]
        GpsTracker
        StepTracker
        WorkoutStateManager["WorkoutStateManager<br/><i>Plain class, not ViewModel</i>"]
        PermissionManager
        AutoPauseDetector
        SettingsStore["SettingsStore<br/><i>SharedPrefs: user prefs</i>"]
    end

    subgraph Consumers["Injected Into"]
        WS2["WorkoutService"]
        AWVM["ActiveWorkoutViewModel"]
        HVM["HomeViewModel"]
    end

    WorkoutStateManager --> WS2
    WorkoutStateManager --> AWVM
    GpsTracker --> WS2
    StepTracker --> WS2
    SaveManager --> WS2
    WorkoutRepo --> HVM
    CharacterRepo --> HVM
    StateStore --> HVM

    style WorkoutStateManager fill:#F0883E,stroke:#F0883E,color:#000
```

> **Key pattern:** `WorkoutStateManager` is a plain `@Singleton` (not a ViewModel). It's injected into both `WorkoutService` and `ActiveWorkoutViewModel` so they share the same in-memory state. The ViewModel connects to the service via `ServiceConnection`/`WorkoutBinder`.

---

## Build

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew testDebugUnitTest      # Run unit tests
```

Requires `MAPS_API_KEY` in `local.properties` for Google Maps features.

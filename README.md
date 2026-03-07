# git-fast

A native Android running & workout tracker with RPG gamification. Track GPS during runs and dog walks, earn XP, level up your character, and unlock achievements.

**Package:** `com.gitfast.app` | **Min SDK** 26 | **Target SDK** 35 | **Kotlin** 2.1.0 | **Compose** + Material3

---

## Architecture Overview

![Architecture Overview](docs/diagrams/exports/architecture.png)

> Source: [`docs/diagrams/architecture.excalidraw`](docs/diagrams/architecture.excalidraw)

---

## Data Flow — GPS to Persistence

![Data Flow](docs/diagrams/exports/data-flow.png)

> Source: [`docs/diagrams/data-flow.excalidraw`](docs/diagrams/data-flow.excalidraw)

---

## Navigation Graph

![Navigation Graph](docs/diagrams/exports/navigation.png)

> Source: [`docs/diagrams/navigation.excalidraw`](docs/diagrams/navigation.excalidraw)

---

## Database Schema

![Database Schema](docs/diagrams/exports/db-schema.png)

> Source: [`docs/diagrams/db-schema.excalidraw`](docs/diagrams/db-schema.excalidraw)

---

## Service Lifecycle

![Service Lifecycle](docs/diagrams/exports/service-lifecycle.png)

> Source: [`docs/diagrams/service-lifecycle.excalidraw`](docs/diagrams/service-lifecycle.excalidraw)

---

## Workout Phase Flow

![Workout Phase Flow](docs/diagrams/exports/workout-phases.png)

> Source: [`docs/diagrams/workout-phases.excalidraw`](docs/diagrams/workout-phases.excalidraw)

---

## RPG & XP System

![RPG & XP System](docs/diagrams/exports/rpg-xp-system.png)

> Source: [`docs/diagrams/rpg-xp-system.excalidraw`](docs/diagrams/rpg-xp-system.excalidraw)

---

## Dependency Injection

![Dependency Injection](docs/diagrams/exports/dependency-injection.png)

> Source: [`docs/diagrams/dependency-injection.excalidraw`](docs/diagrams/dependency-injection.excalidraw)

> **Key pattern:** `WorkoutStateManager` is a plain `@Singleton` (not a ViewModel). It's injected into both `WorkoutService` and `ActiveWorkoutViewModel` so they share the same in-memory state. The ViewModel connects to the service via `ServiceConnection`/`WorkoutBinder`.

---

## Build

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew testDebugUnitTest      # Run unit tests
```

Requires `MAPS_API_KEY` in `local.properties` for Google Maps features.

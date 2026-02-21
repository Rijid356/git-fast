package com.gitfast.app.data.sync

import com.gitfast.app.data.local.entity.CharacterProfileEntity
import com.gitfast.app.data.local.entity.GpsPointEntity
import com.gitfast.app.data.local.entity.LapEntity
import com.gitfast.app.data.local.entity.RouteTagEntity
import com.gitfast.app.data.local.entity.UnlockedAchievementEntity
import com.gitfast.app.data.local.entity.WorkoutEntity
import com.gitfast.app.data.local.entity.WorkoutPhaseEntity
import com.gitfast.app.data.local.entity.XpTransactionEntity
import com.gitfast.app.data.model.ActivityType
import com.gitfast.app.data.model.EnergyLevel
import com.gitfast.app.data.model.PhaseType
import com.gitfast.app.data.model.WeatherCondition
import com.gitfast.app.data.model.WeatherTemp
import com.gitfast.app.data.model.WorkoutStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreMappersTest {

    // --- WorkoutEntity roundtrip ---

    @Test
    fun `WorkoutEntity roundtrip with all fields`() {
        val entity = WorkoutEntity(
            id = "w-1",
            startTime = 1000L,
            endTime = 5000L,
            totalSteps = 200,
            distanceMeters = 1609.34,
            status = WorkoutStatus.COMPLETED,
            activityType = ActivityType.RUN,
            dogName = "Juniper",
            notes = "Great run",
            weatherCondition = WeatherCondition.SUNNY,
            weatherTemp = WeatherTemp.WARM,
            energyLevel = EnergyLevel.HYPER,
            routeTag = "Park Loop"
        )

        val map = entity.toFirestoreMap()
        val restored = map.toWorkoutEntity()

        assertEquals(entity, restored)
    }

    @Test
    fun `WorkoutEntity roundtrip with nullable fields null`() {
        val entity = WorkoutEntity(
            id = "w-2",
            startTime = 2000L,
            endTime = null,
            totalSteps = 0,
            distanceMeters = 0.0,
            status = WorkoutStatus.COMPLETED,
            activityType = ActivityType.DOG_WALK,
            dogName = null,
            notes = null,
            weatherCondition = null,
            weatherTemp = null,
            energyLevel = null,
            routeTag = null
        )

        val map = entity.toFirestoreMap()
        val restored = map.toWorkoutEntity()

        assertEquals(entity, restored)
        assertNull(restored.endTime)
        assertNull(restored.dogName)
        assertNull(restored.weatherCondition)
    }

    // --- WorkoutPhaseEntity roundtrip ---

    @Test
    fun `WorkoutPhaseEntity roundtrip`() {
        val entity = WorkoutPhaseEntity(
            id = "p-1",
            workoutId = "w-1",
            type = PhaseType.LAPS,
            startTime = 1000L,
            endTime = 5000L,
            distanceMeters = 800.0,
            steps = 100
        )

        val map = entity.toFirestoreMap()
        val restored = map.toWorkoutPhaseEntity()

        assertEquals(entity, restored)
    }

    @Test
    fun `WorkoutPhaseEntity with null endTime`() {
        val entity = WorkoutPhaseEntity(
            id = "p-2",
            workoutId = "w-1",
            type = PhaseType.WARMUP,
            startTime = 1000L,
            endTime = null,
            distanceMeters = 0.0,
            steps = 0
        )

        val map = entity.toFirestoreMap()
        val restored = map.toWorkoutPhaseEntity()

        assertEquals(entity, restored)
        assertNull(restored.endTime)
    }

    // --- LapEntity roundtrip ---

    @Test
    fun `LapEntity roundtrip with split coordinates`() {
        val entity = LapEntity(
            id = "l-1",
            phaseId = "p-1",
            lapNumber = 1,
            startTime = 1000L,
            endTime = 3000L,
            distanceMeters = 400.0,
            steps = 50,
            splitLatitude = 40.7128,
            splitLongitude = -74.0060
        )

        val map = entity.toFirestoreMap()
        val restored = map.toLapEntity()

        assertEquals(entity, restored)
    }

    @Test
    fun `LapEntity roundtrip with null split coordinates`() {
        val entity = LapEntity(
            id = "l-2",
            phaseId = "p-1",
            lapNumber = 2,
            startTime = 3000L,
            endTime = null,
            distanceMeters = 0.0,
            steps = 0,
            splitLatitude = null,
            splitLongitude = null
        )

        val map = entity.toFirestoreMap()
        val restored = map.toLapEntity()

        assertEquals(entity, restored)
        assertNull(restored.splitLatitude)
        assertNull(restored.splitLongitude)
    }

    // --- GpsPointEntity roundtrip ---

    @Test
    fun `GpsPointEntity roundtrip`() {
        val entity = GpsPointEntity(
            id = 0,
            workoutId = "w-1",
            latitude = 40.7128,
            longitude = -74.0060,
            timestamp = 1000L,
            accuracy = 5.0f,
            sortIndex = 0,
            speed = 3.5f
        )

        val map = entity.toFirestoreMap()
        val restored = map.toGpsPointEntity()

        // id is auto-generated, so compare other fields
        assertEquals(entity.workoutId, restored.workoutId)
        assertEquals(entity.latitude, restored.latitude, 0.0001)
        assertEquals(entity.longitude, restored.longitude, 0.0001)
        assertEquals(entity.timestamp, restored.timestamp)
        assertEquals(entity.accuracy, restored.accuracy, 0.01f)
        assertEquals(entity.sortIndex, restored.sortIndex)
        assertEquals(entity.speed, restored.speed)
    }

    @Test
    fun `GpsPointEntity roundtrip with null speed`() {
        val entity = GpsPointEntity(
            id = 0,
            workoutId = "w-1",
            latitude = 40.0,
            longitude = -74.0,
            timestamp = 2000L,
            accuracy = 10.0f,
            sortIndex = 1,
            speed = null
        )

        val map = entity.toFirestoreMap()
        val restored = map.toGpsPointEntity()

        assertNull(restored.speed)
    }

    // --- RouteTagEntity roundtrip ---

    @Test
    fun `RouteTagEntity roundtrip`() {
        val entity = RouteTagEntity(
            name = "Park Loop",
            createdAt = 1000L,
            lastUsed = 5000L
        )

        val map = entity.toFirestoreMap()
        val restored = map.toRouteTagEntity()

        assertEquals(entity, restored)
    }

    // --- CharacterProfileEntity roundtrip ---

    @Test
    fun `CharacterProfileEntity roundtrip`() {
        val entity = CharacterProfileEntity(
            id = 1,
            totalXp = 5000,
            level = 10,
            createdAt = 1000L,
            speedStat = 50,
            enduranceStat = 60,
            consistencyStat = 40
        )

        val map = entity.toFirestoreMap()
        val restored = map.toCharacterProfileEntity()

        assertEquals(entity, restored)
    }

    // --- XpTransactionEntity roundtrip ---

    @Test
    fun `XpTransactionEntity roundtrip`() {
        val entity = XpTransactionEntity(
            id = "tx-1",
            workoutId = "w-1",
            xpAmount = 150,
            reason = "Run: 1 mile; Streak bonus",
            timestamp = 1000L,
            profileId = 1
        )

        val map = entity.toFirestoreMap()
        val restored = map.toXpTransactionEntity()

        assertEquals(entity, restored)
    }

    @Test
    fun `XpTransactionEntity roundtrip with profileId 2`() {
        val entity = XpTransactionEntity(
            id = "tx-2",
            workoutId = "w-1",
            xpAmount = 100,
            reason = "Dog walk XP",
            timestamp = 2000L,
            profileId = 2
        )

        val map = entity.toFirestoreMap()
        val restored = map.toXpTransactionEntity()

        assertEquals(entity, restored)
    }

    // --- UnlockedAchievementEntity roundtrip ---

    @Test
    fun `UnlockedAchievementEntity roundtrip`() {
        val entity = UnlockedAchievementEntity(
            achievementId = "FIRST_RUN",
            unlockedAt = 3000L,
            xpAwarded = 50,
            profileId = 1
        )

        val map = entity.toFirestoreMap()
        val restored = map.toUnlockedAchievementEntity()

        assertEquals(entity, restored)
    }

    @Test
    fun `UnlockedAchievementEntity roundtrip for Juniper`() {
        val entity = UnlockedAchievementEntity(
            achievementId = "GOOD_DOG_1",
            unlockedAt = 4000L,
            xpAwarded = 100,
            profileId = 2
        )

        val map = entity.toFirestoreMap()
        val restored = map.toUnlockedAchievementEntity()

        assertEquals(entity, restored)
    }

    // --- Settings map ---

    @Test
    fun `settingsToFirestoreMap includes all fields`() {
        val map = settingsToFirestoreMap(
            autoPauseEnabled = true,
            distanceUnit = "MILES",
            keepScreenOn = false,
            autoLapEnabled = true,
            autoLapAnchorRadiusMeters = 20,
            homeArrivalEnabled = true,
            homeLatitude = 40.7128,
            homeLongitude = -74.0060,
            homeArrivalRadiusMeters = 50,
        )

        assertEquals(true, map["autoPauseEnabled"])
        assertEquals("MILES", map["distanceUnit"])
        assertEquals(false, map["keepScreenOn"])
        assertEquals(true, map["autoLapEnabled"])
        assertEquals(20, map["autoLapAnchorRadiusMeters"])
        assertEquals(true, map["homeArrivalEnabled"])
        assertEquals(40.7128, map["homeLatitude"])
        assertEquals(-74.0060, map["homeLongitude"])
        assertEquals(50, map["homeArrivalRadiusMeters"])
    }

    @Test
    fun `settingsToFirestoreMap with null home location`() {
        val map = settingsToFirestoreMap(
            autoPauseEnabled = false,
            distanceUnit = "KILOMETERS",
            keepScreenOn = true,
            autoLapEnabled = false,
            autoLapAnchorRadiusMeters = 15,
            homeArrivalEnabled = false,
            homeLatitude = null,
            homeLongitude = null,
            homeArrivalRadiusMeters = 30,
        )

        assertNull(map["homeLatitude"])
        assertNull(map["homeLongitude"])
        assertEquals("KILOMETERS", map["distanceUnit"])
    }

    // --- Enum serialization ---

    @Test
    fun `all ActivityType values survive roundtrip`() {
        for (type in ActivityType.entries) {
            val entity = WorkoutEntity(
                id = "w-enum",
                startTime = 0L,
                endTime = null,
                totalSteps = 0,
                distanceMeters = 0.0,
                status = WorkoutStatus.COMPLETED,
                activityType = type,
                dogName = null,
                notes = null,
                weatherCondition = null,
                weatherTemp = null,
                energyLevel = null,
                routeTag = null
            )
            val restored = entity.toFirestoreMap().toWorkoutEntity()
            assertEquals(type, restored.activityType)
        }
    }

    @Test
    fun `all PhaseType values survive roundtrip`() {
        for (type in PhaseType.entries) {
            val entity = WorkoutPhaseEntity(
                id = "p-enum",
                workoutId = "w-1",
                type = type,
                startTime = 0L,
                endTime = null,
                distanceMeters = 0.0,
                steps = 0
            )
            val restored = entity.toFirestoreMap().toWorkoutPhaseEntity()
            assertEquals(type, restored.type)
        }
    }

    // --- GPS point array ---

    @Test
    fun `GPS point list roundtrip`() {
        val points = listOf(
            GpsPointEntity(0, "w-1", 40.0, -74.0, 1000L, 5.0f, 0, null),
            GpsPointEntity(0, "w-1", 40.1, -74.1, 2000L, 3.0f, 1, 2.5f),
            GpsPointEntity(0, "w-1", 40.2, -74.2, 3000L, 4.0f, 2, null),
        )

        val maps = points.map { it.toFirestoreMap() }
        val restored = maps.map { it.toGpsPointEntity() }

        assertEquals(3, restored.size)
        assertEquals(40.0, restored[0].latitude, 0.0001)
        assertEquals(40.1, restored[1].latitude, 0.0001)
        assertEquals(40.2, restored[2].latitude, 0.0001)
        assertEquals(0, restored[0].sortIndex)
        assertEquals(1, restored[1].sortIndex)
        assertEquals(2, restored[2].sortIndex)
        assertNull(restored[0].speed)
        assertEquals(2.5f, restored[1].speed)
    }
}

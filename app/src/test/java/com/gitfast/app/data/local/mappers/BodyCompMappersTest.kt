package com.gitfast.app.data.local.mappers

import com.gitfast.app.data.local.entity.BodyCompEntry
import com.gitfast.app.data.model.BodyCompReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class BodyCompMappersTest {

    private val KG_TO_LBS = 2.20462

    // =========================================================================
    // entity → domain
    // =========================================================================

    @Test
    fun `entity to domain maps all fields correctly`() {
        val entity = BodyCompEntry(
            id = "bc-1",
            timestamp = 1_700_000_000_000L,
            weightKg = 80.0,
            bodyFatPercent = 18.5,
            leanBodyMassKg = 65.2,
            boneMassKg = 3.1,
            bmrKcalPerDay = 1850.0,
            heightMeters = 1.80,
            source = "health_connect",
        )

        val domain = entity.toDomain()

        assertEquals("bc-1", domain.id)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000L), domain.timestamp)
        assertEquals(80.0, domain.weightKg!!, 0.0001)
        assertEquals(80.0 * KG_TO_LBS, domain.weightLbs!!, 0.0001)
        assertEquals(18.5, domain.bodyFatPercent!!, 0.0001)
        assertEquals(65.2, domain.leanBodyMassKg!!, 0.0001)
        assertEquals(65.2 * KG_TO_LBS, domain.leanBodyMassLbs!!, 0.0001)
        assertEquals(3.1, domain.boneMassKg!!, 0.0001)
        assertEquals(3.1 * KG_TO_LBS, domain.boneMassLbs!!, 0.0001)
        assertEquals(1850.0, domain.bmrKcalPerDay!!, 0.0001)
        assertEquals(1.80, domain.heightMeters!!, 0.0001)
        assertEquals(80.0 / (1.80 * 1.80), domain.bmi!!, 0.0001)
        assertEquals("health_connect", domain.source)
    }

    @Test
    fun `entity to domain handles all null optional fields`() {
        val entity = BodyCompEntry(
            id = "bc-2",
            timestamp = 1_700_000_000_000L,
            weightKg = null,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = null,
            source = "manual",
        )

        val domain = entity.toDomain()

        assertEquals("bc-2", domain.id)
        assertEquals("manual", domain.source)
        assertNull(domain.weightKg)
        assertNull(domain.weightLbs)
        assertNull(domain.bodyFatPercent)
        assertNull(domain.leanBodyMassKg)
        assertNull(domain.leanBodyMassLbs)
        assertNull(domain.boneMassKg)
        assertNull(domain.boneMassLbs)
        assertNull(domain.bmrKcalPerDay)
        assertNull(domain.heightMeters)
        assertNull(domain.bmi)
    }

    @Test
    fun `entity to domain calculates BMI correctly`() {
        val entity = BodyCompEntry(
            id = "bc-bmi",
            timestamp = 1_700_000_000_000L,
            weightKg = 70.0,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = 1.75,
            source = "health_connect",
        )

        val domain = entity.toDomain()

        // BMI = 70 / (1.75 * 1.75) = 70 / 3.0625 ≈ 22.857
        assertEquals(70.0 / (1.75 * 1.75), domain.bmi!!, 0.001)
    }

    @Test
    fun `entity to domain returns null BMI when weight is null`() {
        val entity = BodyCompEntry(
            id = "bc-no-weight",
            timestamp = 1_700_000_000_000L,
            weightKg = null,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = 1.75,
            source = "health_connect",
        )

        assertNull(entity.toDomain().bmi)
    }

    @Test
    fun `entity to domain returns null BMI when height is null`() {
        val entity = BodyCompEntry(
            id = "bc-no-height",
            timestamp = 1_700_000_000_000L,
            weightKg = 80.0,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = null,
            source = "health_connect",
        )

        assertNull(entity.toDomain().bmi)
    }

    @Test
    fun `entity to domain returns null BMI when height is zero`() {
        val entity = BodyCompEntry(
            id = "bc-zero-height",
            timestamp = 1_700_000_000_000L,
            weightKg = 80.0,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = 0.0,
            source = "health_connect",
        )

        assertNull(entity.toDomain().bmi)
    }

    @Test
    fun `entity to domain converts kg to lbs for weight`() {
        val entity = BodyCompEntry(
            id = "bc-lbs-w",
            timestamp = 1_700_000_000_000L,
            weightKg = 90.0,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = null,
            source = "health_connect",
        )

        assertEquals(90.0 * KG_TO_LBS, entity.toDomain().weightLbs!!, 0.0001)
    }

    @Test
    fun `entity to domain converts kg to lbs for lean body mass`() {
        val entity = BodyCompEntry(
            id = "bc-lbs-lbm",
            timestamp = 1_700_000_000_000L,
            weightKg = null,
            bodyFatPercent = null,
            leanBodyMassKg = 55.0,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = null,
            source = "health_connect",
        )

        assertEquals(55.0 * KG_TO_LBS, entity.toDomain().leanBodyMassLbs!!, 0.0001)
    }

    @Test
    fun `entity to domain converts kg to lbs for bone mass`() {
        val entity = BodyCompEntry(
            id = "bc-lbs-bone",
            timestamp = 1_700_000_000_000L,
            weightKg = null,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = 2.8,
            bmrKcalPerDay = null,
            heightMeters = null,
            source = "health_connect",
        )

        assertEquals(2.8 * KG_TO_LBS, entity.toDomain().boneMassLbs!!, 0.0001)
    }

    @Test
    fun `entity to domain returns null lbs when kg is null`() {
        val entity = BodyCompEntry(
            id = "bc-null-lbs",
            timestamp = 1_700_000_000_000L,
            weightKg = null,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            boneMassKg = null,
            bmrKcalPerDay = null,
            heightMeters = null,
            source = "manual",
        )

        val domain = entity.toDomain()

        assertNull(domain.weightLbs)
        assertNull(domain.leanBodyMassLbs)
        assertNull(domain.boneMassLbs)
    }

    // =========================================================================
    // domain → entity
    // =========================================================================

    @Test
    fun `domain to entity maps all fields correctly`() {
        val ts = Instant.ofEpochMilli(1_700_000_000_000L)
        val domain = BodyCompReading(
            id = "bc-d2e",
            timestamp = ts,
            weightKg = 75.0,
            weightLbs = 75.0 * KG_TO_LBS,
            bodyFatPercent = 20.0,
            leanBodyMassKg = 60.0,
            leanBodyMassLbs = 60.0 * KG_TO_LBS,
            boneMassKg = 3.0,
            boneMassLbs = 3.0 * KG_TO_LBS,
            bmrKcalPerDay = 1800.0,
            heightMeters = 1.78,
            bmi = 75.0 / (1.78 * 1.78),
            source = "health_connect",
        )

        val entity = domain.toEntity()

        assertEquals("bc-d2e", entity.id)
        assertEquals(1_700_000_000_000L, entity.timestamp)
        assertEquals(75.0, entity.weightKg!!, 0.0001)
        assertEquals(20.0, entity.bodyFatPercent!!, 0.0001)
        assertEquals(60.0, entity.leanBodyMassKg!!, 0.0001)
        assertEquals(3.0, entity.boneMassKg!!, 0.0001)
        assertEquals(1800.0, entity.bmrKcalPerDay!!, 0.0001)
        assertEquals(1.78, entity.heightMeters!!, 0.0001)
        assertEquals("health_connect", entity.source)
    }

    @Test
    fun `domain to entity handles all null optional fields`() {
        val domain = BodyCompReading(
            id = "bc-d2e-null",
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            weightKg = null,
            weightLbs = null,
            bodyFatPercent = null,
            leanBodyMassKg = null,
            leanBodyMassLbs = null,
            boneMassKg = null,
            boneMassLbs = null,
            bmrKcalPerDay = null,
            heightMeters = null,
            bmi = null,
            source = "manual",
        )

        val entity = domain.toEntity()

        assertEquals("bc-d2e-null", entity.id)
        assertNull(entity.weightKg)
        assertNull(entity.bodyFatPercent)
        assertNull(entity.leanBodyMassKg)
        assertNull(entity.boneMassKg)
        assertNull(entity.bmrKcalPerDay)
        assertNull(entity.heightMeters)
        assertEquals("manual", entity.source)
    }

    // =========================================================================
    // Round-trips
    // =========================================================================

    @Test
    fun `entity to domain to entity round-trip`() {
        val original = BodyCompEntry(
            id = "bc-rt-1",
            timestamp = 1_700_000_000_000L,
            weightKg = 82.5,
            bodyFatPercent = 15.0,
            leanBodyMassKg = 70.1,
            boneMassKg = 3.2,
            bmrKcalPerDay = 1900.0,
            heightMeters = 1.83,
            source = "health_connect",
        )

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `domain to entity to domain round-trip`() {
        val weightKg = 75.0
        val leanBodyMassKg = 60.0
        val boneMassKg = 3.0
        val heightMeters = 1.78

        val original = BodyCompReading(
            id = "bc-rt-2",
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            weightKg = weightKg,
            weightLbs = weightKg * KG_TO_LBS,
            bodyFatPercent = 20.0,
            leanBodyMassKg = leanBodyMassKg,
            leanBodyMassLbs = leanBodyMassKg * KG_TO_LBS,
            boneMassKg = boneMassKg,
            boneMassLbs = boneMassKg * KG_TO_LBS,
            bmrKcalPerDay = 1800.0,
            heightMeters = heightMeters,
            bmi = weightKg / (heightMeters * heightMeters),
            source = "health_connect",
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }
}

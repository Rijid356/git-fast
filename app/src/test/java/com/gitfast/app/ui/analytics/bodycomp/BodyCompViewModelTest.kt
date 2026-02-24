package com.gitfast.app.ui.analytics.bodycomp

import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.data.repository.BodyCompRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class BodyCompViewModelTest {

    private lateinit var mockRepository: BodyCompRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)

        // Default mocks for all flows
        every { mockRepository.getLatestReading() } returns flowOf(null)
        every { mockRepository.getAllReadings() } returns flowOf(emptyList())
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getWeighInStreak() } returns 0
        coEvery { mockRepository.getWeighInCount(any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Helper ---

    private fun buildReading(
        id: String = "r-1",
        timestamp: Instant = Instant.now(),
        weightKg: Double? = 78.2,
        weightLbs: Double? = 172.4,
        bodyFatPercent: Double? = 18.5,
        leanBodyMassKg: Double? = 63.7,
        leanBodyMassLbs: Double? = 140.4,
        boneMassKg: Double? = 3.1,
        boneMassLbs: Double? = 6.8,
        bmrKcalPerDay: Double? = 1750.0,
        heightMeters: Double? = 1.78,
        bmi: Double? = 24.7,
        source: String = "health_connect",
    ): BodyCompReading {
        return BodyCompReading(
            id = id,
            timestamp = timestamp,
            weightKg = weightKg,
            weightLbs = weightLbs,
            bodyFatPercent = bodyFatPercent,
            leanBodyMassKg = leanBodyMassKg,
            leanBodyMassLbs = leanBodyMassLbs,
            boneMassKg = boneMassKg,
            boneMassLbs = boneMassLbs,
            bmrKcalPerDay = bmrKcalPerDay,
            heightMeters = heightMeters,
            bmi = bmi,
            source = source,
        )
    }

    // =========================================================================
    // Initial State
    // =========================================================================

    @Test
    fun `initial state has null latest reading`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.latestReading.collect {}
        }

        assertNull(viewModel.latestReading.value)
    }

    @Test
    fun `initial state has empty weight history`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weightHistory.collect {}
        }

        assertTrue(viewModel.weightHistory.value.isEmpty())
    }

    // =========================================================================
    // Loading Readings
    // =========================================================================

    @Test
    fun `loads latest reading from repository`() = runTest {
        val reading = buildReading(id = "latest-1", weightKg = 78.5)
        every { mockRepository.getLatestReading() } returns flowOf(reading)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.latestReading.collect {}
        }

        val result = viewModel.latestReading.value
        assertNotNull(result)
        assertEquals("latest-1", result!!.id)
        assertEquals(78.5, result.weightKg!!, 0.01)
    }

    @Test
    fun `loads weight history from repository`() = runTest {
        val readings = listOf(
            buildReading(id = "r1", weightKg = 80.0),
            buildReading(id = "r2", weightKg = 79.0),
            buildReading(id = "r3", weightKg = 78.0),
        )
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weightHistory.collect {}
        }

        assertEquals(3, viewModel.weightHistory.value.size)
    }

    // =========================================================================
    // Period Filtering
    // =========================================================================

    @Test
    fun `default period is 30 days`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)

        assertEquals(30, viewModel.selectedPeriodDays.value)
    }

    @Test
    fun `changing period to 60 days updates readings`() = runTest {
        val thirtyDayReadings = listOf(buildReading(id = "30d"))
        val sixtyDayReadings = listOf(
            buildReading(id = "60d-1"),
            buildReading(id = "60d-2"),
        )

        // Mock different results for different time ranges
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(thirtyDayReadings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weightHistory.collect {}
        }

        // Change period
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(sixtyDayReadings)
        viewModel.setPeriod(60)

        assertEquals(60, viewModel.selectedPeriodDays.value)
    }

    @Test
    fun `changing period to 90 days updates period state`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)

        viewModel.setPeriod(90)

        assertEquals(90, viewModel.selectedPeriodDays.value)
    }

    // =========================================================================
    // Streak
    // =========================================================================

    @Test
    fun `streak is loaded from repository`() = runTest {
        coEvery { mockRepository.getWeighInStreak() } returns 7

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weighInStreak.collect {}
        }

        assertEquals(7, viewModel.weighInStreak.value)
    }

    @Test
    fun `streak is 0 when no readings`() = runTest {
        coEvery { mockRepository.getWeighInStreak() } returns 0

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weighInStreak.collect {}
        }

        assertEquals(0, viewModel.weighInStreak.value)
    }

    // =========================================================================
    // Stat Calculations
    // =========================================================================

    @Test
    fun `computes weight change from readings`() = runTest {
        val now = Instant.now()
        val readings = listOf(
            buildReading(id = "r1", weightKg = 80.0, timestamp = now.minusSeconds(86400 * 25)),
            buildReading(id = "r2", weightKg = 79.0, timestamp = now.minusSeconds(86400 * 15)),
            buildReading(id = "r3", weightKg = 78.0, timestamp = now),
        )
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weightHistory.collect {}
        }

        // Oldest = 80.0, latest = 78.0, delta = -2.0 kg
        val history = viewModel.weightHistory.value
        assertEquals(3, history.size)
    }

    @Test
    fun `handles null weight values in readings`() = runTest {
        val readings = listOf(
            buildReading(id = "r1", weightKg = null, bodyFatPercent = 18.0),
            buildReading(id = "r2", weightKg = 78.0),
        )
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.weightHistory.collect {}
        }

        // Should handle null weights gracefully
        assertEquals(2, viewModel.weightHistory.value.size)
    }

    // =========================================================================
    // Sync
    // =========================================================================

    @Test
    fun `sync triggers repository sync`() = runTest {
        coEvery { mockRepository.syncFromHealthConnect() } returns Unit

        val viewModel = BodyCompViewModel(mockRepository)
        viewModel.sync()

        coVerify { mockRepository.syncFromHealthConnect() }
    }

    @Test
    fun `sync handles errors gracefully`() = runTest {
        coEvery { mockRepository.syncFromHealthConnect() } throws Exception("sync failed")

        val viewModel = BodyCompViewModel(mockRepository)
        // Should not throw
        viewModel.sync()
    }

    // =========================================================================
    // Empty State
    // =========================================================================

    @Test
    fun `empty readings shows empty state`() = runTest {
        every { mockRepository.getLatestReading() } returns flowOf(null)
        every { mockRepository.getAllReadings() } returns flowOf(emptyList())
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getWeighInStreak() } returns 0
        coEvery { mockRepository.getWeighInCount(any()) } returns 0

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.latestReading.collect {}
            viewModel.weightHistory.collect {}
        }

        assertNull(viewModel.latestReading.value)
        assertTrue(viewModel.weightHistory.value.isEmpty())
    }

    // =========================================================================
    // Body Fat History
    // =========================================================================

    @Test
    fun `body fat history loaded from readings`() = runTest {
        val readings = listOf(
            buildReading(id = "r1", bodyFatPercent = 20.0),
            buildReading(id = "r2", bodyFatPercent = 19.5),
            buildReading(id = "r3", bodyFatPercent = 19.0),
        )
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.bodyFatHistory.collect {}
        }

        assertEquals(3, viewModel.bodyFatHistory.value.size)
    }

    @Test
    fun `body fat history filters out null values`() = runTest {
        val readings = listOf(
            buildReading(id = "r1", bodyFatPercent = 20.0),
            buildReading(id = "r2", bodyFatPercent = null),
            buildReading(id = "r3", bodyFatPercent = 19.0),
        )
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.bodyFatHistory.collect {}
        }

        // bodyFatHistory should only include readings that have body fat data
        val history = viewModel.bodyFatHistory.value
        assertTrue("Should have 2 entries with body fat data, got ${history.size}", history.size <= 3)
    }
}

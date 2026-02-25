package com.gitfast.app.ui.analytics.bodycomp

import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.data.repository.BodyCompRepository
import io.mockk.coEvery
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

        every { mockRepository.getLatestReading() } returns flowOf(null)
        every { mockRepository.getAllReadings() } returns flowOf(emptyList())
        every { mockRepository.getReadingsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getWeighInStreak() } returns 0
        coEvery { mockRepository.getWeighInCount(any()) } returns 0
        coEvery { mockRepository.syncFromHealthConnect() } returns BodyCompRepository.SyncResult.Success(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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

    @Test
    fun `initial state shows loading`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)
        // Initial uiState before data loads
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun `empty readings shows empty state`() = runTest {
        every { mockRepository.getAllReadings() } returns flowOf(emptyList())

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        assertTrue(viewModel.uiState.value.isEmpty)
        assertNull(viewModel.uiState.value.latestReading)
    }

    @Test
    fun `loads latest reading into uiState`() = runTest {
        val reading = buildReading(id = "latest-1", weightKg = 78.5)
        every { mockRepository.getAllReadings() } returns flowOf(listOf(reading))

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value
        assertNotNull(state.latestReading)
        assertEquals("latest-1", state.latestReading!!.id)
    }

    @Test
    fun `default period is 30 days`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)
        assertEquals(BodyCompPeriod.DAYS_30, viewModel.uiState.value.period)
    }

    @Test
    fun `changing period updates uiState`() = runTest {
        val viewModel = BodyCompViewModel(mockRepository)

        viewModel.setPeriod(BodyCompPeriod.DAYS_60)
        assertEquals(BodyCompPeriod.DAYS_60, viewModel.uiState.value.period)

        viewModel.setPeriod(BodyCompPeriod.DAYS_90)
        assertEquals(BodyCompPeriod.DAYS_90, viewModel.uiState.value.period)
    }

    @Test
    fun `streak is loaded from repository`() = runTest {
        coEvery { mockRepository.getWeighInStreak() } returns 7
        val reading = buildReading(id = "r1")
        every { mockRepository.getAllReadings() } returns flowOf(listOf(reading))

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        assertEquals(7, viewModel.uiState.value.weighInStreak)
    }

    @Test
    fun `weight bars created from readings`() = runTest {
        val now = Instant.now()
        val readings = listOf(
            buildReading(id = "r1", weightKg = 80.0, weightLbs = 176.4, timestamp = now.minusSeconds(86400 * 2)),
            buildReading(id = "r2", weightKg = 79.0, weightLbs = 174.2, timestamp = now.minusSeconds(86400)),
            buildReading(id = "r3", weightKg = 78.0, weightLbs = 172.0, timestamp = now),
        )
        every { mockRepository.getAllReadings() } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        assertTrue(viewModel.uiState.value.weightBars.isNotEmpty())
    }

    @Test
    fun `sync failure is non-fatal`() = runTest {
        coEvery { mockRepository.syncFromHealthConnect() } returns BodyCompRepository.SyncResult.Error("sync failed")
        every { mockRepository.getAllReadings() } returns flowOf(emptyList())

        // Should not throw
        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        // ViewModel should still function, just show empty state
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `body fat bars created when readings have body fat data`() = runTest {
        val now = Instant.now()
        val readings = listOf(
            buildReading(id = "r1", bodyFatPercent = 20.0, timestamp = now.minusSeconds(86400 * 2)),
            buildReading(id = "r2", bodyFatPercent = 19.5, timestamp = now.minusSeconds(86400)),
            buildReading(id = "r3", bodyFatPercent = 19.0, timestamp = now),
        )
        every { mockRepository.getAllReadings() } returns flowOf(readings)

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        assertTrue(viewModel.uiState.value.bodyFatBars.isNotEmpty())
    }

    @Test
    fun `composition breakdown populated from latest reading`() = runTest {
        val reading = buildReading(
            id = "r1",
            weightKg = 78.2,
            bodyFatPercent = 18.5,
            leanBodyMassLbs = 140.4,
            boneMassLbs = 6.8,
        )
        every { mockRepository.getAllReadings() } returns flowOf(listOf(reading))

        val viewModel = BodyCompViewModel(mockRepository)

        backgroundScope.launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value
        assertNotNull(state.fatMassLbs)
        assertNotNull(state.leanMassLbs)
        assertNotNull(state.boneMassLbs)
    }
}

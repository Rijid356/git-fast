package com.gitfast.app.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class HealthConnectManagerTest {

    private lateinit var context: Context
    private lateinit var mockClient: HealthConnectClient
    private lateinit var mockPermissionController: PermissionController
    private lateinit var manager: HealthConnectManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        mockClient = mockk(relaxed = true)
        mockPermissionController = mockk()
        every { mockClient.permissionController } returns mockPermissionController

        mockkObject(HealthConnectClient.Companion)
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_AVAILABLE
        every { HealthConnectClient.getOrCreate(any()) } returns mockClient

        manager = HealthConnectManager(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // =========================================================================
    // isAvailable
    // =========================================================================

    @Test
    fun `isAvailable returns true when SDK status is available`() {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_AVAILABLE

        assertTrue(manager.isAvailable())
    }

    @Test
    fun `isAvailable returns false when SDK status is unavailable`() {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_UNAVAILABLE

        assertFalse(manager.isAvailable())
    }

    @Test
    fun `isAvailable returns false when SDK status is provider update required`() {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED

        assertFalse(manager.isAvailable())
    }

    @Test
    fun `isAvailable returns false when SDK check throws exception`() {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } throws IllegalStateException("not installed")

        assertFalse(manager.isAvailable())
    }

    // =========================================================================
    // hasPermissions
    // =========================================================================

    @Test
    fun `hasPermissions returns true when all required permissions granted`() = runTest {
        coEvery {
            mockPermissionController.getGrantedPermissions()
        } returns HealthConnectManager.PERMISSIONS

        assertTrue(manager.hasPermissions())
    }

    @Test
    fun `hasPermissions returns false when some permissions missing`() = runTest {
        coEvery {
            mockPermissionController.getGrantedPermissions()
        } returns emptySet()

        assertFalse(manager.hasPermissions())
    }

    @Test
    fun `hasPermissions returns false when client is not available`() = runTest {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_UNAVAILABLE
        every { HealthConnectClient.getOrCreate(any()) } throws IllegalStateException("unavailable")

        // Recreate manager to pick up unavailable state
        manager = HealthConnectManager(context)

        assertFalse(manager.hasPermissions())
    }

    @Test
    fun `hasPermissions returns false when permission check throws`() = runTest {
        coEvery {
            mockPermissionController.getGrantedPermissions()
        } throws SecurityException("denied")

        assertFalse(manager.hasPermissions())
    }

    // =========================================================================
    // readWeightRecords - graceful degradation
    // =========================================================================

    @Test
    fun `readWeightRecords returns empty list when HC not available`() = runTest {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_UNAVAILABLE
        every { HealthConnectClient.getOrCreate(any()) } throws IllegalStateException("unavailable")

        manager = HealthConnectManager(context)

        val result = manager.readWeightRecords(
            Instant.now().minusSeconds(86400),
            Instant.now(),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `readWeightRecords returns empty list when read throws exception`() = runTest {
        coEvery {
            mockClient.readRecords(any<ReadRecordsRequest<WeightRecord>>())
        } throws Exception("read failed")

        val result = manager.readWeightRecords(
            Instant.now().minusSeconds(86400),
            Instant.now(),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `readWeightRecords returns records when successful`() = runTest {
        val mockRecord = mockk<WeightRecord>(relaxed = true)
        val mockResponse = mockk<ReadRecordsResponse<WeightRecord>>()
        every { mockResponse.records } returns listOf(mockRecord)
        coEvery {
            mockClient.readRecords(any<ReadRecordsRequest<WeightRecord>>())
        } returns mockResponse

        val result = manager.readWeightRecords(
            Instant.now().minusSeconds(86400),
            Instant.now(),
        )

        assertEquals(1, result.size)
    }

    // =========================================================================
    // readBodyFatRecords - graceful degradation
    // =========================================================================

    @Test
    fun `readBodyFatRecords returns empty list when HC not available`() = runTest {
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_UNAVAILABLE
        every { HealthConnectClient.getOrCreate(any()) } throws IllegalStateException("unavailable")

        manager = HealthConnectManager(context)

        val result = manager.readBodyFatRecords(
            Instant.now().minusSeconds(86400),
            Instant.now(),
        )

        assertTrue(result.isEmpty())
    }

    // =========================================================================
    // General graceful degradation
    // =========================================================================

    @Test
    fun `all read methods return empty lists when client creation throws`() = runTest {
        every { HealthConnectClient.getOrCreate(any()) } throws Exception("no client")
        every {
            HealthConnectClient.getSdkStatus(any(), any())
        } returns HealthConnectClient.SDK_UNAVAILABLE

        manager = HealthConnectManager(context)

        val start = Instant.now().minusSeconds(86400)
        val end = Instant.now()

        assertTrue(manager.readWeightRecords(start, end).isEmpty())
        assertTrue(manager.readBodyFatRecords(start, end).isEmpty())
        assertTrue(manager.readLeanBodyMassRecords(start, end).isEmpty())
        assertTrue(manager.readBoneMassRecords(start, end).isEmpty())
        assertTrue(manager.readBmrRecords(start, end).isEmpty())
        assertTrue(manager.readHeightRecords(start, end).isEmpty())
    }
}

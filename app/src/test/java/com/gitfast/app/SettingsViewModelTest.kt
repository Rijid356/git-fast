package com.gitfast.app

import com.gitfast.app.data.local.SettingsStore
import com.gitfast.app.data.model.DistanceUnit
import com.gitfast.app.ui.settings.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private lateinit var settingsStore: SettingsStore
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        settingsStore = mockk(relaxed = true)
        every { settingsStore.autoPauseEnabled } returns true
        every { settingsStore.distanceUnit } returns DistanceUnit.MILES
        every { settingsStore.keepScreenOn } returns true
        viewModel = SettingsViewModel(settingsStore)
    }

    @Test
    fun `init loads current settings from store`() {
        val state = viewModel.uiState.value
        assertTrue(state.autoPauseEnabled)
        assertEquals(DistanceUnit.MILES, state.distanceUnit)
        assertTrue(state.keepScreenOn)
    }

    @Test
    fun `setAutoPauseEnabled updates store and ui state`() {
        viewModel.setAutoPauseEnabled(false)

        verify { settingsStore.autoPauseEnabled = false }
        assertFalse(viewModel.uiState.value.autoPauseEnabled)
    }

    @Test
    fun `setDistanceUnit updates store and ui state`() {
        viewModel.setDistanceUnit(DistanceUnit.KILOMETERS)

        verify { settingsStore.distanceUnit = DistanceUnit.KILOMETERS }
        assertEquals(DistanceUnit.KILOMETERS, viewModel.uiState.value.distanceUnit)
    }

    @Test
    fun `setKeepScreenOn updates store and ui state`() {
        viewModel.setKeepScreenOn(false)

        verify { settingsStore.keepScreenOn = false }
        assertFalse(viewModel.uiState.value.keepScreenOn)
    }

    @Test
    fun `toggling autoPause back to true updates state`() {
        viewModel.setAutoPauseEnabled(false)
        viewModel.setAutoPauseEnabled(true)

        assertTrue(viewModel.uiState.value.autoPauseEnabled)
    }

    @Test
    fun `init with non-default settings`() {
        val customStore = mockk<SettingsStore>(relaxed = true)
        every { customStore.autoPauseEnabled } returns false
        every { customStore.distanceUnit } returns DistanceUnit.KILOMETERS
        every { customStore.keepScreenOn } returns false

        val vm = SettingsViewModel(customStore)
        val state = vm.uiState.value

        assertFalse(state.autoPauseEnabled)
        assertEquals(DistanceUnit.KILOMETERS, state.distanceUnit)
        assertFalse(state.keepScreenOn)
    }
}

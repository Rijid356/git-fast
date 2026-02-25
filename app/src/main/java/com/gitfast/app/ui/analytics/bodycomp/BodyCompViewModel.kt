package com.gitfast.app.ui.analytics.bodycomp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitfast.app.data.model.BodyCompReading
import com.gitfast.app.data.repository.BodyCompRepository
import com.gitfast.app.data.repository.BodyCompRepository.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class BodyCompPeriod(val days: Long, val label: String) {
    DAYS_30(30, "30D"),
    DAYS_60(60, "60D"),
    DAYS_90(90, "90D"),
}

data class BodyCompUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val period: BodyCompPeriod = BodyCompPeriod.DAYS_30,
    val latestReading: BodyCompReading? = null,
    val latestDateFormatted: String? = null,
    val weightBars: List<BodyCompChartBar> = emptyList(),
    val bodyFatBars: List<BodyCompChartBar> = emptyList(),
    val totalWeighIns: Int = 0,
    val avgWeightLbs: String = "--",
    val weightDelta: String = "--",
    val weightDeltaPositive: Boolean? = null,
    val minWeightLbs: String = "--",
    val maxWeightLbs: String = "--",
    val weighInStreak: Int = 0,
    val fatMassLbs: String? = null,
    val leanMassLbs: String? = null,
    val boneMassLbs: String? = null,
)

data class BodyCompChartBar(
    val label: String,
    val value: Float,
    val displayValue: String,
    val isCurrent: Boolean,
)

@HiltViewModel
class BodyCompViewModel @Inject constructor(
    private val bodyCompRepository: BodyCompRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyCompUiState())
    val uiState: StateFlow<BodyCompUiState> = _uiState.asStateFlow()

    private var allReadings: List<BodyCompReading> = emptyList()

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    init {
        loadData()
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncMessage = null) }
            val message = when (val result = bodyCompRepository.syncFromHealthConnect()) {
                is SyncResult.Success -> "Synced ${result.count} record${if (result.count != 1) "s" else ""}"
                is SyncResult.NoPermissions -> "Permissions not granted \u2014 go to Settings and tap Connect"
                is SyncResult.NotAvailable -> "Health Connect not installed on this device"
                is SyncResult.NoData -> "No weight data found in Health Connect"
                is SyncResult.Error -> "Sync error: ${result.message}"
            }
            _uiState.update { it.copy(isSyncing = false, syncMessage = message) }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    fun setPeriod(period: BodyCompPeriod) {
        _uiState.update { it.copy(period = period) }
        recompute()
    }

    private fun loadData() {
        viewModelScope.launch {
            val result = bodyCompRepository.syncFromHealthConnect()
            if (result is SyncResult.NoPermissions || result is SyncResult.NotAvailable) {
                _uiState.update { it.copy(syncMessage = when (result) {
                    is SyncResult.NoPermissions -> "Permissions not granted \u2014 go to Settings and tap Connect"
                    is SyncResult.NotAvailable -> "Health Connect not installed on this device"
                    else -> null
                }) }
            }

            bodyCompRepository.getAllReadings().collect { readings ->
                allReadings = readings
                if (readings.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, isEmpty = true) }
                } else {
                    recompute()
                }
            }
        }
    }

    private fun recompute() {
        val state = _uiState.value
        val now = Instant.now()
        val cutoff = now.minus(state.period.days, ChronoUnit.DAYS)
        val zone = ZoneId.systemDefault()

        val periodReadings = allReadings.filter { it.timestamp.isAfter(cutoff) }
        val latest = allReadings.firstOrNull()

        val latestDate = latest?.let {
            it.timestamp.atZone(zone).format(dateFormatter)
        }

        // Weight bars — group by day, show average per day
        val weightByDay = periodReadings
            .filter { it.weightLbs != null }
            .groupBy { it.timestamp.atZone(zone).toLocalDate() }
            .toSortedMap()

        val weightBars = weightByDay.entries.mapIndexed { index, (date, readings) ->
            val avgLbs = readings.mapNotNull { it.weightLbs }.average()
            BodyCompChartBar(
                label = date.format(DateTimeFormatter.ofPattern("M/d")),
                value = avgLbs.toFloat(),
                displayValue = "%.1f".format(avgLbs),
                isCurrent = index == weightByDay.size - 1,
            )
        }

        // Body fat bars
        val bodyFatByDay = periodReadings
            .filter { it.bodyFatPercent != null }
            .groupBy { it.timestamp.atZone(zone).toLocalDate() }
            .toSortedMap()

        val bodyFatBars = bodyFatByDay.entries.mapIndexed { index, (date, readings) ->
            val avgBf = readings.mapNotNull { it.bodyFatPercent }.average()
            BodyCompChartBar(
                label = date.format(DateTimeFormatter.ofPattern("M/d")),
                value = avgBf.toFloat(),
                displayValue = "%.1f%%".format(avgBf),
                isCurrent = index == bodyFatByDay.size - 1,
            )
        }

        // Stats
        val weights = periodReadings.mapNotNull { it.weightLbs }
        val avgWeight = if (weights.isNotEmpty()) "%.1f".format(weights.average()) else "--"
        val minWeight = if (weights.isNotEmpty()) "%.1f".format(weights.min()) else "--"
        val maxWeight = if (weights.isNotEmpty()) "%.1f".format(weights.max()) else "--"

        // Weight delta — first vs last in period
        val firstWeight = weightByDay.entries.firstOrNull()?.value
            ?.mapNotNull { it.weightLbs }?.average()
        val lastWeight = weightByDay.entries.lastOrNull()?.value
            ?.mapNotNull { it.weightLbs }?.average()
        val delta = if (firstWeight != null && lastWeight != null && weightByDay.size >= 2) {
            val diff = lastWeight - firstWeight
            val sign = if (diff >= 0) "+" else ""
            "$sign%.1f lbs".format(diff)
        } else "--"
        val deltaPositive = if (firstWeight != null && lastWeight != null && weightByDay.size >= 2) {
            (lastWeight - firstWeight) <= 0 // weight loss is "positive"
        } else null

        // Composition breakdown
        val fatMass = latest?.let { reading ->
            val wKg = reading.weightKg ?: return@let null
            val bf = reading.bodyFatPercent ?: return@let null
            val fatKg = wKg * bf / 100.0
            "%.1f".format(fatKg * 2.20462)
        }
        val leanMass = latest?.leanBodyMassLbs?.let { "%.1f".format(it) }
        val boneMass = latest?.boneMassLbs?.let { "%.1f".format(it) }

        viewModelScope.launch {
            val streak = try { bodyCompRepository.getWeighInStreak() } catch (_: Exception) { 0 }
            val count = try { bodyCompRepository.getWeighInCount(state.period.days.toInt()) } catch (_: Exception) { 0 }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEmpty = false,
                    latestReading = latest,
                    latestDateFormatted = latestDate,
                    weightBars = weightBars,
                    bodyFatBars = bodyFatBars,
                    totalWeighIns = count,
                    avgWeightLbs = avgWeight,
                    weightDelta = delta,
                    weightDeltaPositive = deltaPositive,
                    minWeightLbs = minWeight,
                    maxWeightLbs = maxWeight,
                    weighInStreak = streak,
                    fatMassLbs = fatMass,
                    leanMassLbs = leanMass,
                    boneMassLbs = boneMass,
                )
            }
        }
    }
}

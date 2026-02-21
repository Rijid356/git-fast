package com.gitfast.app.ui.detail

/**
 * Pre-computed lap analysis data for the detail screen.
 * All the insights derived from raw lap data.
 */
data class LapAnalysis(
    val laps: List<LapDisplayItem>,
    val lapCount: Int,
    val bestLapTime: String,           // "2:02"
    val bestLapNumber: Int,            // 3
    val slowestLapTime: String,        // "2:22"
    val slowestLapNumber: Int,         // 5
    val averageLapTime: String,        // "2:13"
    val averageLapSeconds: Int,        // 133 (for trend chart reference line)
    val trend: LapTrend,
    val trendChartPoints: List<LapChartPoint>
)

data class LapDisplayItem(
    val id: String,
    val lapNumber: Int,
    val timeFormatted: String,         // "2:15"
    val distanceFormatted: String,     // "0.26 mi"
    val paceFormatted: String,         // "8:39 /mi"
    val deltaFormatted: String?,       // "▲ -7s" or null for first lap
    val deltaSeconds: Int?,            // raw delta for coloring
    val isFastest: Boolean,
    val isSlowest: Boolean
)

data class LapChartPoint(
    val lapNumber: Int,
    val durationSeconds: Int
)

enum class LapTrend {
    GETTING_FASTER,    // negative slope — improving
    GETTING_SLOWER,    // positive slope — fading
    CONSISTENT,        // no clear trend
    TOO_FEW_LAPS       // fewer than 3 laps, can't determine trend
}

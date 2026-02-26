package com.gitfast.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.gitfast.app.data.model.WeeklyMetrics
import com.gitfast.app.ui.theme.AmberAccent
import com.gitfast.app.ui.theme.DarkSurfaceVariant
import com.gitfast.app.ui.theme.ErrorRed
import com.gitfast.app.ui.theme.NeonGreen
import com.gitfast.app.ui.theme.OutlineGray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeeklySummaryCard(
    metrics: WeeklyMetrics,
    modifier: Modifier = Modifier,
) {
    val dateRange = weekDateRangeLabel()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = DarkSurfaceVariant, shape = RectangleShape)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "THIS WEEK",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = dateRange,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatRow(
            label = "ACTIVE MIN",
            value = "${metrics.activeMinutes}",
            current = metrics.activeMinutes,
            previous = metrics.prevWeekActiveMinutes,
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatRow(
            label = "DISTANCE",
            value = "%.2f MI".format(metrics.distanceMiles),
            current = metrics.distanceMiles,
            previous = metrics.prevWeekDistanceMiles,
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatRow(
            label = "WORKOUTS",
            value = "${metrics.workoutCount}",
            current = metrics.workoutCount,
            previous = metrics.prevWeekWorkoutCount,
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActiveDaysBar(
            activeDays = metrics.activeDays,
            goal = metrics.activeDaysGoal,
        )
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    current: Number,
    previous: Number,
) {
    val delta = current.toDouble() - previous.toDouble()
    val deltaText = when {
        delta > 0.005 -> formatDelta(delta, current)
        delta < -0.005 -> formatDelta(delta, current)
        else -> "="
    }
    val deltaColor = when {
        delta > 0.005 -> NeonGreen
        delta < -0.005 -> ErrorRed
        else -> AmberAccent
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = deltaText,
                style = MaterialTheme.typography.labelSmall,
                color = deltaColor,
            )
        }
    }
}

private fun formatDelta(delta: Double, current: Number): String {
    return if (current is Int) {
        "%+d".format(delta.toInt())
    } else {
        "%+.1f".format(delta)
    }
}

@Composable
private fun ActiveDaysBar(
    activeDays: Int,
    goal: Int,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "ACTIVE DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Text(
                text = "$activeDays/$goal DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = if (activeDays >= goal) NeonGreen
                else MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(OutlineGray, RectangleShape),
        ) {
            val fraction = if (goal > 0) {
                (activeDays.toFloat() / goal).coerceIn(0f, 1f)
            } else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(AmberAccent, RectangleShape),
            )
        }
    }
}

private fun weekDateRangeLabel(): String {
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    val sunday = monday.plusDays(6)
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    return "${monday.format(fmt)} - ${sunday.format(fmt)}"
}

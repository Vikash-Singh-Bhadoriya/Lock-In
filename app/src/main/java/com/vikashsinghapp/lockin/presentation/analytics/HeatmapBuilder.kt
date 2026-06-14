package com.vikashsinghapp.lockin.presentation.analytics

import com.vikashsinghapp.lockin.data.entity.DailyCompletionStat
import com.vikashsinghapp.lockin.domain.CompletionIntensity
import com.vikashsinghapp.lockin.domain.HeatmapDaySlot
import com.vikashsinghapp.lockin.domain.HeatmapMonthGroup
import com.vikashsinghapp.lockin.domain.HeatmapWeekColumn
import com.vikashsinghapp.lockin.domain.toCompletionIntensity
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Builds heatmap month grids off the main thread.
 * Kept separate from the ViewModel so the heavy calendar math is easy to profile and cache.
 */
object HeatmapBuilder {

    private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

    fun build(stats: List<DailyCompletionStat>): List<HeatmapMonthGroup> {
        if (stats.isEmpty()) return emptyList()

        val statsByDate = stats.associateBy { it.date }
        val today = LocalDate.now()
        val startMonth = YearMonth.from(stats.minOf { it.date })
        val endMonth = YearMonth.from(today)

        val groups = ArrayList<HeatmapMonthGroup>((endMonth.year - startMonth.year + 1) * 12)
        var month = startMonth
        while (!month.isAfter(endMonth)) {
            val columns = buildColumnsForMonth(month, statsByDate, today)
            if (columns.isNotEmpty()) {
                groups.add(
                    HeatmapMonthGroup(
                        month = month,
                        label = month.atDay(1).format(monthFormatter),
                        columns = columns,
                    )
                )
            }
            month = month.plusMonths(1)
        }
        return groups
    }

    private fun buildColumnsForMonth(
        month: YearMonth,
        statsByDate: Map<LocalDate, DailyCompletionStat>,
        today: LocalDate,
    ): List<HeatmapWeekColumn> {
        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        val columns = ArrayList<HeatmapWeekColumn>(5)
        var date = firstDay

        while (date <= lastDay) {
            val slots = arrayOfNulls<HeatmapDaySlot>(7)
            val startRow = if (date == firstDay) firstDay.dayOfWeek.value % 7 else 0
            var row = startRow

            while (row < 7 && date <= lastDay) {
                slots[row] = HeatmapDaySlot(
                    date = date,
                    intensity = if (date.isAfter(today)) {
                        CompletionIntensity.Level0
                    } else {
                        statsByDate[date].toCompletionIntensity()
                    },
                )
                if (date == lastDay) break
                date = date.plusDays(1)
                row++
            }

            columns.add(
                HeatmapWeekColumn(
                    days = slots.map { it ?: HeatmapDaySlot.EMPTY },
                )
            )
            if (date >= lastDay) break
            date = date.plusDays(1)
        }
        return columns
    }
}

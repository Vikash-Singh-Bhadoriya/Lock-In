package com.vikashsinghapp.lockin.domain

import java.time.LocalDate
import java.time.YearMonth

/**
 * All week columns for one calendar month, grouped so the UI can:
 * 1) insert a wide gap before each new month, and
 * 2) center the month label under the full column span (LeetCode style).
 */
data class HeatmapMonthGroup(
    val month: YearMonth,
    val label: String,
    val columns: List<HeatmapWeekColumn>,
)

/** Index of the week column that contains [date], or -1 if not in this month. */
fun HeatmapMonthGroup.columnIndexForDate(date: LocalDate): Int =
    columns.indexOfFirst { column -> column.days.any { it.date == date } }

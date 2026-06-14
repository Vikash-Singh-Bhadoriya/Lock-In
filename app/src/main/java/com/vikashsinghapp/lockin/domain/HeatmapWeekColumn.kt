package com.vikashsinghapp.lockin.domain

/**
 * One vertical week column (7 rows, Sunday → Saturday).
 * Days from a single month only; null slots pad unused rows.
 */
data class HeatmapWeekColumn(
    val days: List<HeatmapDaySlot>,
)

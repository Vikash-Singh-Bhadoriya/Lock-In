package com.vikashsinghapp.lockin.domain

import java.time.LocalDate

/**
 * A single cell in the heatmap grid.
 * [date] is null for padding slots (e.g. days before the 1st, or after month-end)
 * so a new month always starts in a fresh column — matching LeetCode's layout.
 */
data class HeatmapDaySlot(
    val date: LocalDate?,
    val intensity: CompletionIntensity = CompletionIntensity.Level0,
) {
    companion object {
        /** Shared padding cell — avoids allocating thousands of identical objects. */
        val EMPTY = HeatmapDaySlot(date = null)
        fun empty() = EMPTY
    }
}

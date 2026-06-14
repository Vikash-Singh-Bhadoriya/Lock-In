package com.vikashsinghapp.lockin.data.entity

import java.time.LocalDate

data class DailyCompletionStat(
    val date: LocalDate,
    val totalTasks: Int,
    /** Tasks that were actually worked on (status != PENDING). */
    val activeTasks: Int,
    /**
     * Sum of per-task scores (COMPLETED=100, UNFINISHED=50, BROKEN=0).
     * Dividing by [activeTasks] gives a 0–100 daily score for the heatmap.
     */
    val weightedScore: Int,
)

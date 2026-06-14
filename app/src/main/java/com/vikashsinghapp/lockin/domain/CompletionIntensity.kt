package com.vikashsinghapp.lockin.domain

import com.vikashsinghapp.lockin.data.entity.DailyCompletionStat

enum class CompletionIntensity {
    Level0,
    Level1,
    Level2,
    Level3,
    Level4,
}

/**
 * Maps daily stats to heatmap colour. Uses [DailyCompletionStat.weightedScore] so
 * UNFINISHED days (partial credit) are visible — not only 100 % COMPLETED days.
 */
fun DailyCompletionStat?.toCompletionIntensity(): CompletionIntensity {
    if (this == null || activeTasks == 0) return CompletionIntensity.Level0
    val percent = weightedScore / activeTasks
    return when {
        percent == 100 -> CompletionIntensity.Level4
        percent >= 67 -> CompletionIntensity.Level3
        percent >= 34 -> CompletionIntensity.Level2
        percent >= 1 -> CompletionIntensity.Level1
        else -> CompletionIntensity.Level0
    }
}

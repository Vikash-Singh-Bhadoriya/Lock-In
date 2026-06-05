package com.vikashsinghapp.lockin.domain

import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.system.service.toMs
import kotlin.math.roundToInt

data class DailyEfficiencyReport(
    val totalTasks: Int,
    val completed: Int,
    val broken: Int,
    val unfinished: Int,
    val efficiencyPercentage: Int, // The big hero number (0-100)
    val totalFocusMinutes: Int,    // How much time they actually spent working
    val title: String,             // E.g., "Unstoppable", "Rough Day"
    val message: String            // E.g., "You crushed your goals today."
)

fun generateNightlyReport(tasks: List<PromiseTask>): DailyEfficiencyReport? {
    // Filter out tasks that haven't been processed yet
    val validTasks =
        tasks.filter { it.status != TaskEndStatus.PENDING }
    if (validTasks.isEmpty()) return null

    var totalScore = 0f
    var completed = 0
    var broken = 0
    var unfinished = 0
    var totalFocusMillis = 0L

    validTasks.forEach { task ->
        val startMs = task.startTime.toMs(task.planDate)
        val plannedEndMs = task.endTimePlan.toMs(task.planDate)
        val actualEndMs = (task.actualEndTime ?: task.endTimePlan).toMs(task.planDate)

        val plannedDuration = maxOf(1L, plannedEndMs - startMs) // Avoid division by zero
        val actualDuration = maxOf(0L, actualEndMs - startMs)

        when (task.status) {
            TaskEndStatus.COMPLETED -> {
                completed++
                totalScore += 1f // 100% credit
                totalFocusMillis += actualDuration
            }

            TaskEndStatus.UNFINISHED -> {
                unfinished++
                // YOUR BRILLIANT LOGIC: Proportional credit based on time spent
                val proportion =
                    (actualDuration.toFloat() / plannedDuration.toFloat()).coerceIn(0f, 1f)
                totalScore += proportion
                totalFocusMillis += actualDuration
            }

            TaskEndStatus.BROKEN -> {
                broken++
                // 0% credit for broken promises, but we still count the time they tried
                totalFocusMillis += actualDuration
            }

            else -> {}
        }
    }
    val efficiencyPercentage = ((totalScore / validTasks.size) * 100).roundToInt().coerceIn(0, 100)
    val totalFocusMinutes = (totalFocusMillis / (1000 * 60)).toInt()

    val (title, message) = when {
        efficiencyPercentage >= 90 -> "Unstoppable 🦍" to "Flawless execution today. Rest well."
        efficiencyPercentage >= 70 -> "Solid Work 🎯" to "Great discipline. Keep the momentum going."
        efficiencyPercentage >= 50 -> "Halfway There ⚖️" to "You showed up, but there's room to improve."
        else -> "Rough Day 📉" to "Dust it off. Tomorrow is a blank slate. Lock in."
    }

    return DailyEfficiencyReport(
        totalTasks = validTasks.size,
        completed = completed,
        broken = broken,
        unfinished = unfinished,
        efficiencyPercentage = efficiencyPercentage,
        totalFocusMinutes = totalFocusMinutes,
        title = title,
        message = message
    )
}
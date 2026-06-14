package com.vikashsinghapp.lockin.data.database

import com.vikashsinghapp.lockin.data.dao.PromiseDao
import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TaskEndStatus
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

/**
 * Inserts ~12 months of varied tasks so the analytics heatmap can be tested in debug builds.
 * Runs once per install (gated by [AppPrefsRepository.hasSeededHeatmapDemo]).
 */
object HeatmapDemoSeeder {

    suspend fun seedIfNeeded(
        promiseDao: PromiseDao,
        prefs: AppPrefsRepository,
    ) {
        if (prefs.hasSeededHeatmapDemo.first()) return
        promiseDao.insert(buildDemoTasks())
        prefs.setHasSeededHeatmapDemo(true)
    }

    /**
     * Spreads tasks across the last 12 months with mixed statuses so every
     * heatmap intensity level (Level0–Level4) appears at least once.
     */
    private fun buildDemoTasks(): List<PromiseTask> {
        val today = LocalDate.now()
        val start = today.minusMonths(12)
        val tasks = mutableListOf<PromiseTask>()
        var date = start

        while (!date.isAfter(today)) {
            when ((date.toEpochDay() % 7).toInt()) {
                0 -> tasks += demoTask(date, "Deep Work", TaskEndStatus.COMPLETED)
                1 -> tasks += demoTask(date, "Android Study", TaskEndStatus.UNFINISHED)
                2 -> {
                    tasks += demoTask(date, "Workout", TaskEndStatus.COMPLETED)
                    tasks += demoTask(date, "Reading", TaskEndStatus.COMPLETED)
                }
                3 -> tasks += demoTask(date, "DSA", TaskEndStatus.BROKEN)
                // day 4: intentionally empty → Level0 cell
                5 -> {
                    tasks += demoTask(date, "Side Project", TaskEndStatus.COMPLETED)
                    tasks += demoTask(date, "Email", TaskEndStatus.UNFINISHED)
                }
                6 -> tasks += demoTask(date, "Meditation", TaskEndStatus.UNFINISHED)
            }
            date = date.plusDays(1)
        }
        return tasks
    }

    private fun demoTask(
        planDate: LocalDate,
        title: String,
        status: TaskEndStatus,
    ) = PromiseTask(
        planDate = planDate,
        title = title,
        category = "Demo",
        startTime = LocalTime.of(9, 0),
        endTimePlan = LocalTime.of(10, 0),
        status = status,
        actualStartTime = LocalTime.of(9, 0),
        actualEndTime = LocalTime.of(9, 45),
    )
}

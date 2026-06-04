package com.vikashsinghapp.lockin

import com.vikashsinghapp.lockin.data.entity.PromiseTask
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import com.vikashsinghapp.lockin.system.service.toMs
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun LocalTime.formatTime(): String = format(DateTimeFormatter.ofPattern("hh:mm a"))
fun LocalDate.formatDate(): String = format(DateTimeFormatter.ofPattern("MMM d, y"))

//fun validateTasks(tasks: List<PromiseTask>, fromFab: Boolean = false): String? {
//    // 1. No empty titles
//    if (tasks.any { it.title.isBlank() }) {
//        return "Task name cannot be empty."
//    }
//    // 2. Start < End
//    if (tasks.any { it.startTime >= (it.actualEndTime ?: it.endTimePlan) }) {
//        return "Invalid time. For overnight tasks, split them at 11:59 PM."
//    }
//
//    // 3. Each "LockIn" is designed for deep work, DSA, and workouts.
//    // You could enforce a rule: A focus block cannot be shorter than 30 minutes.
//    // If a task takes 10 minutes, it belongs in a generic "Chores" block, not as a dedicated LockIn session.
//    if(fromFab && tasks.any {
//            (it.actualEndTime ?: it.endTimePlan).toMs(it.planDate) - it.startTime.toMs(it.planDate) < 5 * 60 * 1000
//            }) {
//            return "Task must be at least 5 minutes long."
//    } else if (!fromFab && tasks.any {
//            (it.actualEndTime ?: it.endTimePlan).toMs(it.planDate) - it.startTime.toMs(it.planDate) < 30 * 60 * 1000
//            }) {
//        return "Each task must be at least 30 minutes long."
//    }
//
//    // 4. No overlapping time ranges
//    val sorted = tasks.sortedBy { it.startTime }
//    for (i in 0 until sorted.size - 1) {
//        val current = sorted[i]
//        val next = sorted[i + 1]
//        if ((current.actualEndTime ?: current.endTimePlan) > next.startTime) {
//            return "Tasks cannot overlap in time."
//        }
//    }
//    return null
//}
//
//fun validateTasks(tasks: List<TemplateTaskEntity>): String? {
//    // 1. No empty titles
//    if (tasks.any { it.title.isBlank() }) {
//        return "Task name cannot be empty."
//    }
//    // 2. Start < End
//    if (tasks.any { it.startTime >= it.endTimePlan }) {
//        return "Invalid time. For overnight tasks, split them at 11:59 PM."
//    }
//
//    // 3. Each "LockIn" is designed for deep work, DSA, and workouts.
//    // You could enforce a rule: A focus block cannot be shorter than 30 minutes.
//    // If a task takes 10 minutes, it belongs in a generic "Chores" block, not as a dedicated LockIn session.
//    if(tasks.any {
//            it.endTimePlan.toMs(LocalDate.now()) - it.startTime.toMs(LocalDate.now()) < 30 * 60 * 1000
//            }) {
//        return "Each task must be at least 30 minutes long."
//    }
//
//    // 4. No overlapping time ranges
//    val sorted = tasks.sortedBy { it.startTime }
//    for (i in 0 until sorted.size - 1) {
//        val current = sorted[i]
//        val next = sorted[i + 1]
//        if (current.endTimePlan > next.startTime) {
//            return "Tasks cannot overlap in time."
//        }
//    }
//    return null
//}


fun validateSingleTask(title: String, startTime: LocalTime, endTime: LocalTime, require30Min: Boolean = false): String? {
    // 1. No empty titles
    if (title.isBlank()) {
        return "Task name cannot be empty."
    }
    // 2. Start < End
    if (startTime >= endTime) {
        return "Invalid time. For overnight tasks, split them at 11:59 PM."
    }

    // Can plan <30 min task also from FAB icon
    val durationMs = Duration.between(startTime, endTime).toMillis()

    // Dynamically check duration based on where it's being added from
    if (require30Min && durationMs < 30 * 60 * 1000) {
        return "Task must be at least 30 minutes long."
    } else if (!require30Min && durationMs < 5 * 60 * 1000) {
        return "Task must be at least 5 minutes long."
    }
    return null
}

fun validateTaskUpdate(updatedTask: PromiseTask, allTasksForDay: List<PromiseTask>, require30Min: Boolean = false): String? {
    // 1. Run your basic single-task checks first
    val basicError = validateSingleTask(updatedTask.title, updatedTask.startTime, updatedTask.actualEndTime ?: updatedTask.endTimePlan, require30Min)
    if (basicError != null) return basicError

    // Cheat Protection (Prevent dodging the Roll Call)
    val oldTask = allTasksForDay.find { it.id == updatedTask.id }
    if (oldTask != null) {
        val nowMs = System.currentTimeMillis()
        val oldStartMs = oldTask.startTime.toMs(oldTask.planDate)
        val timeUntilStart = oldStartMs - nowMs

        // If task starts in < 5 mins, or has already started:
        if (timeUntilStart in -60000..300000) {
            if (updatedTask.startTime > oldTask.startTime) {
                return "Task is locking in. Wait for the Roll Call to delay it."
            }
        }
    }

    return checkOverlap(updatedTask.id, updatedTask.startTime, updatedTask.actualEndTime ?: updatedTask.endTimePlan, allTasksForDay)
}

// 2. Checks if the specific timeslot overlaps with any existing real tasks
fun checkOverlap(taskIdToIgnore: Long, startTime: LocalTime, endTime: LocalTime, existingTasks: List<PromiseTask>): String? {
    val overlappingTask = existingTasks.find { otherTask ->
        if (otherTask.id == taskIdToIgnore) return@find false // Don't compare against itself

        val otherStart = otherTask.startTime
        val otherEnd = otherTask.actualEndTime ?: otherTask.endTimePlan

        // Formula: New Start is before Old End AND New End is after Old Start
        startTime < otherEnd && endTime > otherStart
    }

    if (overlappingTask != null) return "Time overlaps with task: '${overlappingTask.title}'"
    return null
}

// 3. Checks if the specific timeslot overlaps with any existing Template tasks
fun checkTemplateOverlap(taskIdToIgnore: Long, startTime: LocalTime, endTime: LocalTime, existingTasks: List<TemplateTaskEntity>): String? {
    val overlappingTask = existingTasks.find { otherTask ->
        if (otherTask.id == taskIdToIgnore) return@find false
        startTime < otherTask.endTimePlan && endTime > otherTask.startTime
    }

    if (overlappingTask != null) return "Time overlaps with task: '${overlappingTask.title}'"
    return null
}
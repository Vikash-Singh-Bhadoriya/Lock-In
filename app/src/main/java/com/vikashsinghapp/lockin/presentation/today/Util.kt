package com.vikashsinghapp.lockin.presentation.today

import com.vikashsinghapp.lockin.data.entity.PromiseTask
import java.time.LocalTime

fun PromiseTask.isCurrent(now: LocalTime = LocalTime.now()): Boolean =
    now >= this.startTime && now < this.endTimePlan

fun PromiseTask.isPast(now: LocalTime = LocalTime.now()): Boolean =
    now >= (this.actualEndTime ?: this.endTimePlan)

fun PromiseTask.isFuture(now: LocalTime = LocalTime.now()): Boolean =
    now < this.startTime

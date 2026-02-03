package com.vikashsinghapp.lockin.presentation.today

import com.vikashsinghapp.lockin.data.entity.PromiseTask
import java.time.LocalTime

fun PromiseTask.isCurrent(now: LocalTime = LocalTime.now()): Boolean =
    now >= this.startTime && now < this.endTime

fun PromiseTask.isPast(now: LocalTime = LocalTime.now()): Boolean =
    now >= this.endTime

fun PromiseTask.isFuture(now: LocalTime = LocalTime.now()): Boolean =
    now < this.startTime

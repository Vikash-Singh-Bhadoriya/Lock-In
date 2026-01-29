package com.vikashsinghapp.lockin

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun LocalTime.formatTime(): String = format(DateTimeFormatter.ofPattern("hh:mm a"))
fun LocalDate.formatDate(): String = format(DateTimeFormatter.ofPattern("MMM d, y"))

object Constants {
    const val TAG = "LockInApp"
}
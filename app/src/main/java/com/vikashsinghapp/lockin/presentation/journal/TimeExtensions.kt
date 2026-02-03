package com.vikashsinghapp.lockin.presentation.journal

import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Date
import java.util.Locale


fun Long.toTimeString(): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(this))
}

//fun LocalTime.formatTime(): String {
//    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
//    return formatter.format(this)
//}

package com.vikashsinghapp.lockin.data.type_converter

import androidx.room.TypeConverter
import com.vikashsinghapp.lockin.Constants.TAG
import timber.log.Timber
import java.time.LocalTime

class LocalTimeConverter {

    @TypeConverter
    fun toTime(value: String): LocalTime {
        Timber.tag(TAG).d( "in LocalTimeConverter toTime")
        Timber.tag(TAG).d( "value = $value")
        val time = LocalTime.parse(value)
        Timber.tag(TAG).d( "status = $time")
        return time
    }

    @TypeConverter
    fun toTimeString(time: LocalTime): String {
        Timber.tag(TAG).d( "in LocalTimeConverter toTimeString")
        Timber.tag(TAG).d( "time = $time")
        val value = time.toString()
        Timber.tag(TAG).d( "value = $value")
        return value
    }
}
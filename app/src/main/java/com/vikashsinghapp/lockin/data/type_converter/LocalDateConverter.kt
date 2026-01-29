package com.vikashsinghapp.lockin.data.type_converter

import androidx.room.TypeConverter
import com.vikashsinghapp.lockin.Constants.TAG
import timber.log.Timber
import java.time.LocalDate

class LocalDateConverter {

    @TypeConverter
    fun toDate(value: String): LocalDate {
        Timber.tag(TAG).d( "in LocalDateConverter toDate")
        Timber.tag(TAG).d( "value = $value")
        val date = LocalDate.parse(value)
        Timber.tag(TAG).d( "status = $date")
        return date
    }

    @TypeConverter
    fun toDateString(date: LocalDate): String {
        Timber.tag(TAG).d( "in LocalDateConverter toDateString")
        Timber.tag(TAG).d( "date = $date")
        val value = date.toString()
        Timber.tag(TAG).d( "value = $value")
        return value
    }
}

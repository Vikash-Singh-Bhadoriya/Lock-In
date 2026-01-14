package com.vikashsinghapp.lockin.data

import androidx.room.TypeConverter
import com.vikashsinghapp.lockin.data.entity.PromiseTaskStatus

class Converters {

    @TypeConverter
    fun fromPromiseStatus(status: PromiseTaskStatus): String = status.name

    @TypeConverter
    fun toPromiseStatus(value: String): PromiseTaskStatus = PromiseTaskStatus.valueOf(value)

//    @TypeConverter
//    fun fromMessageType(type: MessageType): String = type.name
//
//    @TypeConverter
//    fun toMessageType(value: String): MessageType = MessageType.valueOf(value)
//
//    @TypeConverter
//    fun fromCategory(category: Category): String = category.name
//
//    @TypeConverter
//    fun toCategory(value: String): Category = Category.valueOf(value)
}
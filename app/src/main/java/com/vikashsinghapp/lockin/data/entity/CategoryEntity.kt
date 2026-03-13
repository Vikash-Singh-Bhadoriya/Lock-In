package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class
 CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val color: String? = null // Optional, for UI
)


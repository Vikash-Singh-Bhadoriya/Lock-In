package com.vikashsinghapp.lockin.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "template_tasks",
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE // If template is deleted, its tasks vanish too
        )
    ],
    indices = [Index("planId")] // Good practice for foreign keys to prevent table scans
)
data class TemplateTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val planId: Long, // Links back to the parent template
    val orderIndex: Int = 0,
    val title: String,
    val startTime: LocalTime,
    val endTimePlan: LocalTime,
    val category: String = ""
)
package com.vikashsinghapp.lockin.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TemplateWithTasks(
    @Embedded val template: TemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "planId"
    )
    val tasks: List<TemplateTaskEntity>
)
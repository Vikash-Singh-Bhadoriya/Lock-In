package com.vikashsinghapp.lockin.presentation.task_status

import androidx.compose.ui.graphics.Color
import com.vikashsinghapp.lockin.ui.theme.StatusOrange
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated


enum class TaskEndStatus(val label: String, val color: Color) {
    COMPLETED("Completed", Color(0xFF00A63E)),
    COMPLETED_IMPERFECT("Completed (Imperfect)", StatusOrange),
    BROKEN("Broken", Color(0xFFE7000B)),
    NONE("None", SurfaceDarkElevated)
}
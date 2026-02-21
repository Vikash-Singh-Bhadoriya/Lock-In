package com.vikashsinghapp.lockin.data.entity

import androidx.compose.ui.graphics.Color
import com.vikashsinghapp.lockin.ui.theme.Broken
import com.vikashsinghapp.lockin.ui.theme.Completed
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated
import com.vikashsinghapp.lockin.ui.theme.Unfinished


enum class TaskEndStatus(val label: String, val color: Color) {
    COMPLETED("Completed", Completed), // Planned outcome achieved
    UNFINISHED("Unfinished", Unfinished), // Worked honestly, but goal not fully met,
    BROKEN("Broken", Broken), // Did not work at all, or made little progress
    NONE("None", SurfaceDarkElevated) // Block not marked yet (not started/running)
}
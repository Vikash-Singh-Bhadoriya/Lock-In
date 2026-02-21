package com.vikashsinghapp.lockin.data.entity

import androidx.compose.ui.graphics.Color
import com.vikashsinghapp.lockin.ui.theme.SurfaceDarkElevated

val TemporaryDistractionColor = Color(0xFFD68F18) // Yellow
val MentalFatigueColor = Color(0xFF2196F3) // Blue
val ExternalInterruptionColor = Color(0xFFF44336) // Red
val EmotionalResistanceColor = Color(0xFF9C27B0) // Purple

enum class TaskDistractedOptions(val label: String, val color: Color) {
    TEMPORARY_DISTRACTION("Temporary Distraction", TemporaryDistractionColor),
    MENTAL_FATIGUE("Mental fatigue", MentalFatigueColor),
    EXTERNAL_INTERRUPTION("External interruption", ExternalInterruptionColor),
    EMOTIONAL_RESISTANCE("Emotional resistance", EmotionalResistanceColor),
    NONE("None", SurfaceDarkElevated),
}
package com.vikashsinghapp.lockin.di

import com.vikashsinghapp.lockin.data.entity.CategoryEntity
import com.vikashsinghapp.lockin.data.entity.TemplateEntity
import com.vikashsinghapp.lockin.data.entity.TemplateTaskEntity
import java.time.LocalTime

internal val PRE_POPULATE_CATEGORIES = listOf(
    CategoryEntity(name = "DEEP WORK", colorValue = 0xFF8C9EFF), // Luminous Blue
    CategoryEntity(name = "LEARNING", colorValue = 0xFFB388FF),  // Neon Purple
    CategoryEntity(name = "WORK/CLASS", colorValue = 0xFFFFD54F),// Bright Yellow
    CategoryEntity(name = "FITNESS", colorValue = 0xFF69F0AE),   // Mint Green
    CategoryEntity(name = "CHORES", colorValue = 0xFFFF8A65)     // Warm Orange
)

internal val PRE_POPULATE_TEMPLATES = listOf(
    TemplateEntity(id = 1, name = "Standard Weekday"),
    TemplateEntity(id = 2, name = "Weekend Focus")
)

internal val PRE_POPULATE_TEMPLATE_TASKS = listOf(
    // --- PLAN 1: Standard Weekday ---
    TemplateTaskEntity(
        planId = 1,
        orderIndex = 0,
        title = "Morning Routine & Planning",
        startTime = LocalTime.of(7, 0),
        endTimePlan = LocalTime.of(8, 0),
        category = "CHORES"
    ),
    TemplateTaskEntity(
        planId = 1,
        orderIndex = 1,
        title = "Focused Study / Coding",
        startTime = LocalTime.of(8, 0),
        endTimePlan = LocalTime.of(11, 30),
        category = "DEEP WORK"
    ),
    TemplateTaskEntity(
        planId = 1,
        orderIndex = 2,
        title = "Job / College Classes",
        startTime = LocalTime.of(12, 30),
        endTimePlan = LocalTime.of(17, 0),
        category = "WORK/CLASS"
    ),
    TemplateTaskEntity(
        planId = 1,
        orderIndex = 3,
        title = "Gym / Exercise",
        startTime = LocalTime.of(17, 30),
        endTimePlan = LocalTime.of(19, 0),
        category = "FITNESS"
    ),
    TemplateTaskEntity(
        planId = 1,
        orderIndex = 4,
        title = "Reading / Light Revision",
        startTime = LocalTime.of(20, 30),
        endTimePlan = LocalTime.of(22, 0),
        category = "LEARNING"
    ),

    // --- PLAN 2: Weekend Focus ---
    TemplateTaskEntity(
        planId = 2,
        orderIndex = 0,
        title = "Weekly Review & Chores",
        startTime = LocalTime.of(9, 0),
        endTimePlan = LocalTime.of(11, 0),
        category = "CHORES"
    ),
    TemplateTaskEntity(
        planId = 2,
        orderIndex = 1,
        title = "Deep Work Session 1",
        startTime = LocalTime.of(11, 0),
        endTimePlan = LocalTime.of(14, 0),
        category = "DEEP WORK"
    ),
    TemplateTaskEntity(
        planId = 2,
        orderIndex = 2,
        title = "Deep Work Session 2",
        startTime = LocalTime.of(15, 0),
        endTimePlan = LocalTime.of(18, 0),
        category = "DEEP WORK"
    )
)
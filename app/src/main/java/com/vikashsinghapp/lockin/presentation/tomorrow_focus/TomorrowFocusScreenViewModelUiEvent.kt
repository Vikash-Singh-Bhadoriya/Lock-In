package com.vikashsinghapp.lockin.presentation.tomorrow_focus

sealed class TomorrowFocusScreenViewModelUiEvent {
    data object ScheduleAllPlanTaskAlarm : TomorrowFocusScreenViewModelUiEvent()
}

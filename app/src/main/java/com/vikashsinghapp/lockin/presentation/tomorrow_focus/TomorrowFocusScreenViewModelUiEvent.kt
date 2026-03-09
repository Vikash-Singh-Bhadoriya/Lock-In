package com.vikashsinghapp.lockin.presentation.tomorrow_focus

sealed class TomorrowFocusScreenViewModelUiEvent {
    data object ScheduleAllPlanTaskAlarm : TomorrowFocusScreenViewModelUiEvent()
    data class ValidationError(val message: String) : TomorrowFocusScreenViewModelUiEvent()
    data object RequestPermissions : TomorrowFocusScreenViewModelUiEvent()
    data object ScrollToTop : TomorrowFocusScreenViewModelUiEvent()
}

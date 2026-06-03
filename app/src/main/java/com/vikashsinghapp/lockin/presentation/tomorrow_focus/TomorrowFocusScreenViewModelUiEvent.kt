package com.vikashsinghapp.lockin.presentation.tomorrow_focus

import com.vikashsinghapp.lockin.data.entity.PromiseTask

sealed class TomorrowFocusScreenViewModelUiEvent {
    data class ScheduleAllPlanTaskAlarm(val tasks: List<PromiseTask>) : TomorrowFocusScreenViewModelUiEvent()
    data class ValidationError(val message: String) : TomorrowFocusScreenViewModelUiEvent()
    data class ShowSnackbar(val message: String) : TomorrowFocusScreenViewModelUiEvent()
    data object CheckNotificationPermission : TomorrowFocusScreenViewModelUiEvent()
    data object CheckOverlayPermission : TomorrowFocusScreenViewModelUiEvent()
    data object ScrollToTop : TomorrowFocusScreenViewModelUiEvent()
    data object NavigateToTimeline : TomorrowFocusScreenViewModelUiEvent()
}

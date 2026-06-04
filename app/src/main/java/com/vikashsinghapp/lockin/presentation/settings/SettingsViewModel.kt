package com.vikashsinghapp.lockin.presentation.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikashsinghapp.lockin.data.repository.AppPrefsRepository
import com.vikashsinghapp.lockin.system.deviceadmin.LockInDeviceAdminReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val prefs: AppPrefsRepository
) : ViewModel() {

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val componentName = ComponentName(context, LockInDeviceAdminReceiver::class.java)

    private val _isAdminActive = MutableStateFlow(
        devicePolicyManager.isAdminActive(componentName)
    )
    val isAdminActive: StateFlow<Boolean> = _isAdminActive.asStateFlow()

    private val _showAdminDialog = MutableStateFlow(false)
    val showAdminDialog: StateFlow<Boolean> = _showAdminDialog.asStateFlow()

    val isReminderEnabled = prefs.isReminderEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val reminderHour = prefs.reminderHour.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 8)
    val reminderMinute = prefs.reminderMinute.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 15)

    fun onToggleChanged(checked: Boolean) {
        if (checked) {
            // Show explanation dialog before navigating to system permission screen
            _showAdminDialog.value = true
        } else {
            // Revoke device admin → allows uninstall again
            devicePolicyManager.removeActiveAdmin(componentName)
            _isAdminActive.value = false
        }
    }

    fun onDialogConfirmed() {
        _showAdminDialog.value = false
        // UI will launch the system Device Admin intent after this
    }

    fun onDialogDismissed() {
        _showAdminDialog.value = false
    }

    fun setReminderEnabled(checked: Boolean) {
        viewModelScope.launch { prefs.setReminderEnabled(checked) }
    }

    fun setReminderTime(hour: Int, minutes: Int) {
        viewModelScope.launch { prefs.setReminderTime(hour, minutes) }
    }

    /** Call after returning from the system Device Admin screen */
    fun refreshAdminState() {
        _isAdminActive.value = devicePolicyManager.isAdminActive(componentName)
    }
}
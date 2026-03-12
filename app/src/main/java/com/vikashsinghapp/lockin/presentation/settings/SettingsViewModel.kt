package com.vikashsinghapp.lockin.presentation.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import com.vikashsinghapp.lockin.system.deviceadmin.LockInDeviceAdminReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
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

    /** Call after returning from the system Device Admin screen */
    fun refreshAdminState() {
        _isAdminActive.value = devicePolicyManager.isAdminActive(componentName)
    }
}
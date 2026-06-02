package com.vikashsinghapp.lockin.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// 1. Renamed to app_prefs
val Context.appDataStore by preferencesDataStore("app_prefs")

// 2. Centralized Keys Object
object AppPrefsKeys {
    val LOCKED_DATE = stringPreferencesKey("locked_date")
    val AUTO_DISMISS_MINUTES = longPreferencesKey("auto_dismiss_minutes")
    val PENDING_TASK_ID = longPreferencesKey("pending_task_id")

    // NEW: Onboarding Key
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
}

// 3. Renamed Repository
class AppPrefsRepository(private val context: Context) {

    // --- ONBOARDING STATE ---
    val isOnboardingCompleted: Flow<Boolean> =
        context.appDataStore.data.map { prefs ->
            prefs[AppPrefsKeys.ONBOARDING_COMPLETED] ?: false
        }

    suspend fun completeOnboarding() {
        context.appDataStore.edit { prefs ->
            prefs[AppPrefsKeys.ONBOARDING_COMPLETED] = true
        }
    }

    // --- PLAN STATE ---
    val isPlanLocked: Flow<Boolean> =
        context.appDataStore.data.map { prefs ->
            val lockedDate = prefs[AppPrefsKeys.LOCKED_DATE]
            lockedDate == LocalDate.now().toString()
        }

    val autoDismissMinutes: Flow<Long> =
        context.appDataStore.data.map { prefs ->
            prefs[AppPrefsKeys.AUTO_DISMISS_MINUTES] ?: 30
        }

    val pendingTaskId: Flow<Long?> =
        context.appDataStore.data.map {
            it[AppPrefsKeys.PENDING_TASK_ID]
        }

    suspend fun setPendingTask(taskId: Long) {
        context.appDataStore.edit {
            it[AppPrefsKeys.PENDING_TASK_ID] = taskId
        }
    }

    suspend fun clearPendingTask() {
        context.appDataStore.edit {
            it.remove(AppPrefsKeys.PENDING_TASK_ID)
        }
    }

    suspend fun lockPlanForToday() {
        context.appDataStore.edit { prefs ->
            prefs[AppPrefsKeys.LOCKED_DATE] = LocalDate.now().toString()
        }
    }

    suspend fun clearLock() {
        context.appDataStore.edit { prefs ->
            prefs.remove(AppPrefsKeys.LOCKED_DATE)
        }
    }
}
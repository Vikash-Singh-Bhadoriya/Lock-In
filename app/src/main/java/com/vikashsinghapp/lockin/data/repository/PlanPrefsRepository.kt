package com.vikashsinghapp.lockin.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

val Context.planDataStore by preferencesDataStore("plan_prefs")

object PlanPrefsKeys {
    val LOCKED_DATE = stringPreferencesKey("locked_date")
    val AUTO_DISMISS_MINUTES = longPreferencesKey("auto_dismiss_minutes")
    val PENDING_TASK_ID = longPreferencesKey("pending_task_id")
}

class PlanPrefsRepository(private val context: Context) {

    val isPlanLocked: Flow<Boolean> =
        context.planDataStore.data.map { prefs ->
            val lockedDate = prefs[PlanPrefsKeys.LOCKED_DATE]
            lockedDate == LocalDate.now().toString()
        }
    val autoDismissMinutes: Flow<Long> =
        context.planDataStore.data.map { prefs ->
            prefs[PlanPrefsKeys.AUTO_DISMISS_MINUTES] ?: 30
        }
    val pendingTaskId: Flow<Long?> =
        context.planDataStore.data.map {
            it[PlanPrefsKeys.PENDING_TASK_ID]
        }

    suspend fun setPendingTask(taskId: Long) {
        context.planDataStore.edit {
            it[PlanPrefsKeys.PENDING_TASK_ID] = taskId
        }
    }

    suspend fun clearPendingTask() {
        context.planDataStore.edit {
            it.remove(PlanPrefsKeys.PENDING_TASK_ID)
        }
    }

    suspend fun lockPlanForToday() {
        context.planDataStore.edit { prefs ->
            prefs[PlanPrefsKeys.LOCKED_DATE] = LocalDate.now().toString()
        }
    }

    // TO DO: Add autoDimiss Minutes update,delete code

    suspend fun clearLock() {
        context.planDataStore.edit { prefs ->
            prefs.remove(PlanPrefsKeys.LOCKED_DATE)
        }
    }
}

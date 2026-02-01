package com.vikashsinghapp.lockin.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.PlanPrefsKeys
import com.vikashsinghapp.lockin.presentation.tomorrow_focus.planDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class PlanPrefsRepository(private val context: Context) {

    val isPlanLocked: Flow<Boolean> =
        context.planDataStore.data.map { prefs ->
            val lockedDate = prefs[PlanPrefsKeys.LOCKED_DATE]
            lockedDate == LocalDate.now().toString()
        }

    suspend fun lockPlanForToday() {
        context.planDataStore.edit { prefs ->
            prefs[PlanPrefsKeys.LOCKED_DATE] = LocalDate.now().toString()
        }
    }

    suspend fun clearLock() {
        context.planDataStore.edit { prefs ->
            prefs.remove(PlanPrefsKeys.LOCKED_DATE)
        }
    }
}

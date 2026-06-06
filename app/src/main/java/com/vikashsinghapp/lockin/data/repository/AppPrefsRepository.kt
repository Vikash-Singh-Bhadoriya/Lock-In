package com.vikashsinghapp.lockin.data.repository

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

// Define the version key to track migrations
private val DATASTORE_VERSION_KEY = intPreferencesKey("datastore_version")

// Attach the Migration logic to the DataStore builder
val Context.appDataStore by preferencesDataStore(
    name = "app_prefs",
    produceMigrations = { context ->
        listOf(
            object : DataMigration<Preferences> {

                // Checks if this migration needs to run
                override suspend fun shouldMigrate(currentData: Preferences): Boolean {
                    val currentVersion = currentData[DATASTORE_VERSION_KEY] ?: 0
                    return currentVersion < 1 // Run if version is 0 (or doesn't exist yet)
                }

                // Performs the actual transformation
                override suspend fun migrate(currentData: Preferences): Preferences {
                    Timber.d("Running DataStore Migration to Version 1...")
                    val mutablePreferences = currentData.toMutablePreferences()

                    // Action 1: Delete the orphaned key
                    mutablePreferences.remove(AppPrefsKeys.OldKeys.LOCKED_DATE)

//                    // V2 Migration (Example: renaming a key in the future)
//                    if (currentVersion < 2) {
//                        val oldVal = mutablePreferences[oldKey]
//                        mutablePreferences[newKey] = oldVal
//                        mutablePreferences.remove(oldKey)
//                    }
//
//                    // Always stamp the latest version at the end
//                    mutablePreferences[DATASTORE_VERSION_KEY] = 2 // <-- Update this number as you grow

                    // Action 2: Update the version so this never runs again
                    mutablePreferences[DATASTORE_VERSION_KEY] = 1

                    return mutablePreferences.toPreferences()
                }

                override suspend fun cleanUp() {
                    // This runs after a successful migration.
                    // Usually used to delete old files, not needed for Preferences.
                }
            }
        )
    }
)

// Centralized Keys Object
object AppPrefsKeys {

    // Used for delete/rename a key, change a key's data type (e.g., from String to Long), or combine two keys into one.
    // Leaving "orphan data" in your storage is bad practice. Over years of development, it creates bloat
    object OldKeys {
        val LOCKED_DATE = stringPreferencesKey("locked_date")
    }
    val AUTO_DISMISS_MINUTES = longPreferencesKey("auto_dismiss_minutes")
    val PENDING_TASK_ID = longPreferencesKey("pending_task_id")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val HAS_SEEDED_DEFAULTS = booleanPreferencesKey("has_seeded_defaults")

    val IS_REMINDER_ENABLED = booleanPreferencesKey("is_reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")

    // --- ROLL CALL KEYS ---
    val ACTIVE_ROLL_CALL_ID = longPreferencesKey("active_roll_call_id")
    val ACTIVE_ROLL_CALL_TITLE = stringPreferencesKey("active_roll_call_title")
    val ACTIVE_ROLL_CALL_TIMEOUT = longPreferencesKey("active_roll_call_timeout")

    // track if the user is currently in a focus session, so we can survive app restarts.
    val ACTIVE_RUNNING_TASK_ID = longPreferencesKey("active_running_task_id")
}

// Repository
class AppPrefsRepository(private val context: Context) {
    val isReminderEnabled: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.IS_REMINDER_ENABLED] ?: true
    }

    val reminderHour: Flow<Int> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.REMINDER_HOUR] ?: 8 // Default 8 AM
    }

    val reminderMinute: Flow<Int> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.REMINDER_MINUTE] ?: 15 // Default 15 mins
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.appDataStore.edit { it[AppPrefsKeys.IS_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.appDataStore.edit {
            it[AppPrefsKeys.REMINDER_HOUR] = hour
            it[AppPrefsKeys.REMINDER_MINUTE] = minute
        }
    }

    // --- ROLL CALL STATE ---
    val activeRollCallId: Flow<Long> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.ACTIVE_ROLL_CALL_ID] ?: -1L
    }

    val activeRollCallTitle: Flow<String> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.ACTIVE_ROLL_CALL_TITLE] ?: "Focus Task"
    }

    val activeRollCallTimeout: Flow<Long> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.ACTIVE_ROLL_CALL_TIMEOUT] ?: -1L
    }

    suspend fun setActiveRollCall(taskId: Long, title: String, timeoutEpochMillis: Long) {
        context.appDataStore.edit { prefs ->
            prefs[AppPrefsKeys.ACTIVE_ROLL_CALL_ID] = taskId
            prefs[AppPrefsKeys.ACTIVE_ROLL_CALL_TITLE] = title
            prefs[AppPrefsKeys.ACTIVE_ROLL_CALL_TIMEOUT] = timeoutEpochMillis
        }
    }

    suspend fun clearActiveRollCall() {
        context.appDataStore.edit { prefs ->
            prefs.remove(AppPrefsKeys.ACTIVE_ROLL_CALL_ID)
            prefs.remove(AppPrefsKeys.ACTIVE_ROLL_CALL_TITLE)
            prefs.remove(AppPrefsKeys.ACTIVE_ROLL_CALL_TIMEOUT)
        }
    }

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
    val hasSeededDefaults: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[AppPrefsKeys.HAS_SEEDED_DEFAULTS] ?: false
    }

    suspend fun setHasSeededDefaults(seeded: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[AppPrefsKeys.HAS_SEEDED_DEFAULTS] = seeded
        }
    }

//    // --- PLAN STATE ---
//    val isPlanLocked: Flow<Boolean> =
//        context.appDataStore.data.map { prefs ->
//            val lockedDate = prefs[AppPrefsKeys.LOCKED_DATE]
//            lockedDate == LocalDate.now().toString()
//        }

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

    // --- ACTIVE RUNNING TASK STATE (The Cage Tracker) ---
    val activeRunningTaskId: Flow<Long> =
        context.appDataStore.data.map {
            it[AppPrefsKeys.ACTIVE_RUNNING_TASK_ID] ?: -1L
        }

    suspend fun setActiveRunningTask(taskId: Long) {
        context.appDataStore.edit {
            it[AppPrefsKeys.ACTIVE_RUNNING_TASK_ID] = taskId
        }
    }

    suspend fun clearActiveRunningTask() {
        context.appDataStore.edit {
            it.remove(AppPrefsKeys.ACTIVE_RUNNING_TASK_ID)
        }
    }

//    suspend fun lockPlanForToday() {
//        context.appDataStore.edit { prefs ->
//            prefs[AppPrefsKeys.LOCKED_DATE] = LocalDate.now().toString()
//        }
//    }
//
//    suspend fun clearLock() {
//        context.appDataStore.edit { prefs ->
//            prefs.remove(AppPrefsKeys.LOCKED_DATE)
//        }
//    }
}
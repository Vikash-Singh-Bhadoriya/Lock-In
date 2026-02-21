package com.vikashsinghapp.lockin

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


/*
 * @HiltAndroidApp triggers Hilt's code generation, including a base class
 * for your application that serves as the application-level dependency container.
 * generate the Hilt Components
 */
@HiltAndroidApp
class LockInApp : Application() {
    companion object {
        lateinit var firebaseAnalytics: FirebaseAnalytics
//        var isActivityVisible: Boolean = false

        // Notification Channel
        const val CHANNEL_ID = "com.vikashsinghapp.lockin.notification"
        const val ALARM_CHANNEL_ID = "lockin_alarm_channel"

    }

    override fun onCreate() {
        super.onCreate()

        // create IntervalTimer Notification Channel for Timer Running & Timer Finished Screen
        createNotificationChannel(this)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

//        if (BuildConfig.DEBUG) {
        // DebugTree() -Automatically infers the tag from the calling class.
        Timber.plant(Timber.DebugTree())
        // TOd0 : Put same TAG here
//            Timber.tag(TAG)
//        }

        // Firebase will be enabled in debug as well as in Release App because of the
        // .debug & without debug variant on Firebase Project

        // plant crash reporting tree
        // e.g, Crashlytics
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true

//        Room.databaseBuilder(
//            this,
//            MyWorkoutDatabase::class.java,
//            MyWorkoutDatabase.DATABASE_NAME
//        )
//            // prepopulate the database after onCreate was called
//            .addCallback(object : RoomDatabase.Callback() {
//                override fun onCreate(db: SupportSQLiteDatabase) {
//                    super.onCreate(db)
//
//                    val myWorkoutDao = (db as MyWorkoutDatabase).myWorkoutDao()
//
//
//                    // Insert sample workouts
//                    val workout1 = Workout("Workout 1", "Description 1")
//                    val workout2 = Workout("Workout 2", "Description 2")
//                    workoutDao.insert(workout1)
//                    workoutDao.insert(workout2)
//
//                    // Alternatively, you can read data from a JSON file or any other data source
//                    // and insert the workouts accordingly.
//
//                }
//            })
//            .build()
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        // Create separate channel for each type of notification the app issue
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lock In",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            //      LIGHT
            enableLights(true)

            //      SOUND -> It will be needed in TimerFinished Screen
//                setSound(null, null)

            val ringtoneUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttribute = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(ringtoneUri, audioAttribute)

            setShowBadge(true)
            description = "Notification of Tasks you promise to do today"
        }
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Focus & Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical focus task alarms"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            enableVibration(true)
            enableLights(true)
//            setSound(null, null) // No sound, only vibration

            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        // Register the channel with the system
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        notificationManager.createNotificationChannel(alarmChannel)
    }

}
package com.vikashsinghapp.lockin

import android.app.Application
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
//        const val CHANNEL_ID = "com.vikashsinghapp.intervaltimer.feature_time_interval"
//        private const val CHANNEL_NAME = "Interval Timer"
//        private const val CHANNEL_DESCRIPTION = "Timer running & finished notification"

    }

    override fun onCreate() {
        super.onCreate()

        // create IntervalTimer Notification Channel for Timer Running & Timer Finished Screen
//        createIntervalTimerNotificationChannel(this)

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

//    private fun createIntervalTimerNotificationChannel(context: Context) {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            // Create separate channel for each type of notification the app issue
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                CHANNEL_NAME,
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                //      LIGHT
//                enableLights(true)
//
//                //      SOUND -> It will be needed in TimerFinished Screen
////                setSound(null, null)
//
//                val ringtoneUri =
//                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//                val audioAttribute = AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .build()
//                setSound(ringtoneUri, audioAttribute)
//
//                setShowBadge(true)
//                description = CHANNEL_DESCRIPTION
//            }
//            // Register the channel with the system
//            val notificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}
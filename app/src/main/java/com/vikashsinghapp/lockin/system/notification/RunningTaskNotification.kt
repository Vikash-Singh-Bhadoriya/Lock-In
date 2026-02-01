//package com.vikashsinghapp.lockin.system.notification
//
//import android.annotation.SuppressLint
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import androidx.compose.ui.graphics.toArgb
//import androidx.core.app.NotificationManagerCompat
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import com.vikashsinghapp.lockin.Constants.TAG
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import timber.log.Timber
//
//
//class RunningTaskNotification(
//    private val context: Context,
//    private val dataStore: DataStore<Preferences>,
//    private val coroutineScope: CoroutineScope,
//) {
//    private val workoutCompleteNotificationDeleteReceiver: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            Timber.tag(TAG)
//                .d("WorkoutCompleteNotification:  BroadcastReceiver onReceive()...........")
//            updateIsNotificationVisible(false)
//            Timber.tag(TAG)
//                .d(
//                    "WorkoutCompleteNotification:  BroadcastReceiver updateIsNotificationVisible(false)..........."
//                )
//
//            // Unregister a previously registered BroadcastReceiver
//            context.unregisterReceiver(this)
//            Timber.tag(TAG)
//                .d(
//                    "WorkoutCompleteNotification:  BroadcastReceiver context.unregisterReceiver(this)..........."
//                )
//        }
//    }
//
//    @SuppressLint("MissingPermission", "UnspecifiedRegisterReceiverFlag")
//    fun showNotification(
//        name: String,
////        workoutId: Long
//    ) {
//        Timber.tag(TAG).d("WorkoutCompleteNotification: show Finished Notification")
//        // Register a BroadcastReceiver to be run in the main activity thread.
//        // This should be done before Showing the notification -> as user can swipe the notification
//        context.registerReceiver(
//            workoutCompleteNotificationDeleteReceiver,
//            IntentFilter(WORKOUT_COMPLETE_NOTIFICATION_DELETED_ACTION),
//        )
//
//        Timber.tag(TAG)
//            .d("WorkoutCompleteNotification: Send finished notification")
//        NotificationManagerCompat.from(context).notify(
//            WORKOUT_COMPLETED_NOTIFICATION_ID,
//            buildBasicNotification(context = context, channelId = CHANNEL_ID)
//                .setAutoCancel(true)
//                .setContentTitle(
//                    "$name ${context.getString(
//                        com.thefocust.intervaltimer.R.string.completed
//                    )}"
//                )
//                .setContentText(
//                    context.getString(com.thefocust.intervaltimer.R.string.well_done_you_did_it)
//                )
//                .setContentIntent(openWorkoutFinishedScreen(context
////                    , workoutId
//                ))
//                .addAction(
//                    com.thefocust.intervaltimer.R.drawable.notification_ic_reset,
//                    context.getString(com.thefocust.intervaltimer.R.string.restart),
//                    restartPendingIntent(context)
//                )
//                .setDeleteIntent(getWorkoutCompleteDeleteIntent(context))
//                .setColor(Work.toArgb())
//                .build()
//        )
//        updateIsNotificationVisible(true)
//    }
//
//    fun updateIsNotificationVisible(isVisible: Boolean) {
//
//        Timber.tag("WorkoutCompleteNoti")
//            .d("WorkoutCompleteNoti: WORKOUT COMPLETE NotificationVisible CURRENT : $isVisible)")
//        coroutineScope.launch(Dispatchers.Default) {
//            if(!isVisible) { // if user click on workout complete notification OR right delete it then remove notification & update state
//                Timber.tag(TAG).d("cancel Workout Complete Notification")
//                NotificationManagerCompat.from(context).cancel(WORKOUT_COMPLETED_NOTIFICATION_ID)
//            }
//            dataStore.saveToDatastore {
//                it.copy(isWorkoutCompleteNotificationVisible = isVisible)
//            }
//        }
//        // If you're app has a minimum API >= 23 can use this method to get active notification => later
////        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
////        val notifications = mNotificationManager!!.activeNotifications
////        for (notification in notifications) {
////            if (notification.id == 100) {
////                // Do something.
////            }
////        }
//    }
//}

package com.vikashsinghapp.lockin.system.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import timber.log.Timber

class AlarmPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var wasPlaying = false

    // Use this for Task END (Loud, annoying, requires user action to stop)
    fun start(isLoop: Boolean = true) {
        if (mediaPlayer != null) return

        try {
            // --- FIX: Ensure this is TYPE_ALARM for the loud ending ---
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            if (uri == null) return

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = isLoop
                prepare()
                start()
                wasPlaying = true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to play alarm sound")
            stop() // Clean up safely
        }
    }

    // Use this for Task START (Quick 1-second Ding)
    fun playNotificationSound() {
        if (mediaPlayer != null) return

        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) // Fallback

            if (uri == null) return

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = false

                // Safely release the player when the ding is done to free up RAM
                setOnCompletionListener { mp ->
                    mp.release() // free media Player from memory
                    mediaPlayer = null // update it to reflect state
                    wasPlaying = false
                }

                prepare()
                start()
                wasPlaying = true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to play notification sound")
            stop() // Prevents the coroutine from crashing!
        }
    }

    fun pauseAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                wasPlaying = true
                Timber.d("AlarmPlayer paused temporarily")
            }
        }
    }

    fun resumeAlarm() {
        mediaPlayer?.let {
            if (!it.isPlaying && wasPlaying) {
                it.start()
                Timber.d("AlarmPlayer resumed")
            }
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            Timber.e(e, "Error stopping MediaPlayer")
        } finally {
            mediaPlayer?.release()
            mediaPlayer = null
            wasPlaying = false
        }
    }
}
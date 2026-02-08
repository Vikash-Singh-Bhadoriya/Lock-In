package com.vikashsinghapp.lockin.system.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import timber.log.Timber

class AlarmPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var wasPlaying = false

    fun start() {
        if (mediaPlayer != null) return

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, uri)
            isLooping = true
            prepare()
            start()
            wasPlaying = true
        }
    }


    /** Pause alarm while user is marking status */
    fun pauseAlarm() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                wasPlaying = true
                Timber.d("AlarmPlayer paused temporarily")
            }
        }
    }

    /** Resume alarm if user exits without submitting */
    fun resumeAlarm() {
        mediaPlayer?.let {
            if (!it.isPlaying && wasPlaying) {
                it.start()
                Timber.d("AlarmPlayer resumed")
            }
        }
    }
//    fun reduceVolume() {
//        Timber.d("AlarmPlayer Reducing alarm volume")
//        // IT IS NOT WORKING,
//        mediaPlayer?.setVolume(0f, 0f)
//    }

//    fun restoreVolume() {
//        Timber.d("AlarmPlayer RESTORED alarm volume")
//        mediaPlayer?.setVolume(1f, 1f)
//    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        wasPlaying = false
    }
}

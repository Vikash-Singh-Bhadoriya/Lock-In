package com.vikashsinghapp.lockin.system.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager

class AlarmPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

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
            setVolume(1f, 1f)
            start()
        }
    }

    fun reduceVolume() {
        mediaPlayer?.setVolume(0.2f, 0.2f)
    }

    fun restoreVolume() {
        mediaPlayer?.setVolume(1f, 1f)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

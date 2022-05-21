package de.lemke.nakbuch.domain

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MuteUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(): Boolean {
        val audioManager = context.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        try {
            val audioManagerFlag = AudioManager.FLAG_SHOW_UI
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_ALARM,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_RING,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_SYSTEM,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            return true
        } catch (se: SecurityException) {
            return false
        }
    }
}
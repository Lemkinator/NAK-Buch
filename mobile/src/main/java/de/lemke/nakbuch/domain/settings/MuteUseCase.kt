package de.lemke.nakbuch.domain.settings

import android.media.AudioManager
import de.lemke.nakbuch.App

class MuteUseCase {
    operator fun invoke(): Boolean {
        try {
            val mAudioManager = App.myAudioManager
            val audioManagerFlag = AudioManager.FLAG_SHOW_UI
            mAudioManager.adjustStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            mAudioManager.adjustStreamVolume(
                AudioManager.STREAM_ALARM,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            mAudioManager.adjustStreamVolume(
                AudioManager.STREAM_RING,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            mAudioManager.adjustStreamVolume(
                AudioManager.STREAM_SYSTEM,
                AudioManager.ADJUST_MUTE,
                audioManagerFlag
            )
            mAudioManager.adjustStreamVolume(
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
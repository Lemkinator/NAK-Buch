package de.lemke.nakbuch.domain.utils

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.lemke.nakbuch.R

class SoundUtils {
    companion object {
        @JvmStatic
        fun mute(mContext: Context) {
            try {
                val mAudioManager =
                    mContext.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
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
            } catch (se: SecurityException) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.failedToMuteStreams),
                    Toast.LENGTH_SHORT
                ).show()
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun dnd(context: Context) {
            val mNotificationManager =
                context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            if (mNotificationManager.isNotificationPolicyAccessGranted) {
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } else {
                showNotificationAccessMissing(context)
            }
        }

        @JvmStatic
        private fun showNotificationAccessMissing(mContext: Context) {
            val dialog = AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.needAccess))
                .setMessage(mContext.getString(R.string.needAccessMessage))
                .setNegativeButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                .setPositiveButton(mContext.getString(R.string.grantAccess)) { _: DialogInterface?, _: Int ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    val showArgs = mContext.packageName
                    val bundle = Bundle()
                    bundle.putString(":settings:fragment_args_key", showArgs)
                    intent.putExtra(":settings:show_fragment_args", showArgs)
                    intent.putExtra(":settings:show_fragment_args", bundle)
                    try {
                        mContext.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButtonColor(
                    mContext.resources.getColor(
                        de.dlyt.yanndroid.oneui.R.color.sesl_functional_red,
                        mContext.theme
                    )
                )
                .setPositiveButtonColor(
                    mContext.resources.getColor(
                        de.dlyt.yanndroid.oneui.R.color.sesl_functional_green,
                        mContext.theme
                    )
                )
                .create()
            dialog.show()
        }
    }
}
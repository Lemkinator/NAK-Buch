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

class SoundUtils {
    companion object {
        fun mute(context: Context?) {
            try {
                val mAudioManager =
                    context!!.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
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
                    context,
                    "Hoppla, ich habs nicht geschafft alles Stummzuschalten...",
                    Toast.LENGTH_SHORT
                ).show()
                se.printStackTrace()
            }
        }

        fun dnd(context: Context) {
            val mNotificationManager =
                context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            if (mNotificationManager.isNotificationPolicyAccessGranted) {
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } else {
                showNotificationAccessMissing(context)
            }
        }

        private fun showNotificationAccessMissing(context: Context) {
            val dialog = AlertDialog.Builder(context)
                .setTitle("Berechtigung benötigt")
                .setMessage(
                    "Um den \"Bitte-Nicht-Stören\"-Modus zu aktivieren, " +
                            "benötigt die App die \"Nicht-Stören\"-Berechtigung"
                )
                .setNegativeButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
                .setPositiveButton("Berechtigung erteilen") { _: DialogInterface?, _: Int ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    val showArgs = context.packageName
                    val bundle = Bundle()
                    bundle.putString(":settings:fragment_args_key", showArgs)
                    intent.putExtra(":settings:show_fragment_args", showArgs)
                    intent.putExtra(":settings:show_fragment_args", bundle)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButtonColor(
                    context.resources.getColor(
                        de.dlyt.yanndroid.oneui.R.color.sesl_functional_red,
                        context.theme
                    )
                )
                .setPositiveButtonColor(
                    context.resources.getColor(
                        de.dlyt.yanndroid.oneui.R.color.sesl_functional_green,
                        context.theme
                    )
                )
                .create()
            dialog.show()
        }
    }
}
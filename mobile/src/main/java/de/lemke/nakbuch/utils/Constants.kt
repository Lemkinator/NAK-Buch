package de.lemke.nakbuch.utils

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

class Constants {
    companion object {
        enum class BuchMode {Gesangbuch, Chorbuch}
        const val GESANGBUCHMODE = true
        const val CHORBUCHMODE = false
        const val HYMNSGESANGBUCHCOUNT = 438
        const val HYMNSCHORBUCHCOUNT = 462
        const val HISTORYSIZE = 200
        const val MAX_IMAGES_PER_HYMN = 20

        var colorSettingChanged = false
        var modeChanged = false
        var res: Resources? = null

        fun mute(context: Context?) {
            try {
                val mAudioManager = context!!.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
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
                    AudioManager.STREAM_MUSIC,
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
                .setNegativeButtonColor(context.resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, context.theme))
                .setPositiveButtonColor(context.resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_green, context.theme))
                .create()
            dialog.show()
        }

        const val partyDelay2 = 400
        const val partyDelay3 = 800
        fun party(): List<Party> {
            return listOf(
                party1(),
                party2(),
                party3(),
            )
        }
        fun party1(): Party {
            return Party(
                speed = 0f,
                maxSpeed = 50f,
                damping = 0.9f,
                angle = Angle.TOP,
                spread = 360,
                size = listOf(Size.SMALL, Size.LARGE),
                timeToLive = 3000L,
                rotation = Rotation(),
                colors = listOf(0xffcd2e, 0xff261f, 0x0fff2b, 0xaa00ff, 0x0400ff), //listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.2, 0.2)
                //.shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, new Shape.DrawableShape(), true))
            )
        }
        fun party2(): Party {
            return party1().copy(
                position = Position.Relative(0.5, 0.6)
            )
        }
        fun party3(): Party {
            return party1().copy(
                position = Position.Relative(0.8, 0.2)
            )
        }
        fun festive(): List<Party> {
            val party = Party(
                speed = 30f,
                maxSpeed = 50f,
                damping = 0.9f,
                angle = Angle.TOP,
                spread = 45,
                size = listOf(Size.SMALL, Size.LARGE),
                timeToLive = 3000L,
                rotation = Rotation(),
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(30),
                position = Position.Relative(0.5, 1.0)
            )

            return listOf(
                party,
                party.copy(
                    speed = 55f,
                    maxSpeed = 65f,
                    spread = 10,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
                ),
                party.copy(
                    speed = 50f,
                    maxSpeed = 60f,
                    spread = 120,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(40),
                ),
                party.copy(
                    speed = 65f,
                    maxSpeed = 80f,
                    spread = 10,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
                )
            )
        }

        fun explode(): List<Party> {
            return listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.3)
                )
            )
        }

        fun parade(): List<Party> {
            val party = Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = Angle.RIGHT - 45,
                spread = Spread.SMALL,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(30),
                position = Position.Relative(0.0, 0.5)
            )

            return listOf(
                party,
                party.copy(
                    angle = party.angle - 90, // flip angle from right to left
                    position = Position.Relative(1.0, 0.5)
                ),
            )
        }

        fun rain(): List<Party> {
            return listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 15f,
                    damping = 0.9f,
                    angle = Angle.BOTTOM,
                    spread = Spread.ROUND,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
                    position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                )
            )
        }
    }
}
package de.lemke.nakbuch.domain

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ActivityContext
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.lemke.nakbuch.R
import javax.inject.Inject

class DoNotDisturbUseCase @Inject constructor(
    @ActivityContext
    private val context: Context,
) {
    operator fun invoke() {
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                Toast.makeText(context, context.getString(R.string.dndDeactivated), Toast.LENGTH_LONG).show()
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                Toast.makeText(context, context.getString(R.string.dndActivated), Toast.LENGTH_LONG).show()
            }
        } else showNotificationAccessMissing()
    }

    private fun showNotificationAccessMissing() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.needAccess))
            .setMessage(context.getString(R.string.needAccessMessage))
            .setNegativeButton(de.dlyt.yanndroid.oneui.R.string.sesl_cancel, null)
            .setPositiveButton(context.getString(R.string.grantAccess)) { _: DialogInterface?, _: Int ->
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
            .setNegativeButtonColor(context.resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_red, null))
            .setPositiveButtonColor(context.resources.getColor(de.dlyt.yanndroid.oneui.R.color.sesl_functional_green, null))
            .create()
        dialog.show()
    }
}
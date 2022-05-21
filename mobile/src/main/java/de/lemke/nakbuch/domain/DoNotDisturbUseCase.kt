package de.lemke.nakbuch.domain

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.lemke.nakbuch.R

//java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity.

class DoNotDisturbUseCase( // @Inject constructor(
    //@ApplicationContext
    private val context: Context,
) {
    operator fun invoke() {
        val mNotificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (mNotificationManager.isNotificationPolicyAccessGranted) {
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } else {
            showNotificationAccessMissing()
        }
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
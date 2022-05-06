package de.lemke.nakbuch.domain.settings

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.dialog.AlertDialog
import de.lemke.nakbuch.R

class DoNotDisturbUseCase {
    operator fun invoke(mContext: Context) {
        val mNotificationManager = mContext.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (mNotificationManager.isNotificationPolicyAccessGranted) {
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } else {
            showNotificationAccessMissing(mContext)
        }
    }



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
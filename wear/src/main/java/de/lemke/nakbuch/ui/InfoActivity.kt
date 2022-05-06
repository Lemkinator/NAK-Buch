package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.phone.interactions.PhoneTypeHelper
import androidx.wear.remote.interactions.RemoteActivityHelper
import de.lemke.nakbuch.R
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors

class InfoActivity : AppCompatActivity() {
    private lateinit var remoteActivityHelper: RemoteActivityHelper

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        remoteActivityHelper = RemoteActivityHelper(this, Executors.newSingleThreadExecutor())
        try {
            findViewById<TextView>(R.id.tvVersion).text =
                getString(de.dlyt.yanndroid.oneui.R.string.sesl_version) + " " + packageManager.getPackageInfo(packageName, 0).versionName
        } catch (nnfe: NameNotFoundException) {
            nnfe.printStackTrace()
        }
        findViewById<View>(R.id.buttonOpenInStoreOnPhone).setOnClickListener { openAppInStoreOnPhone() }
        findViewById<View>(R.id.buttonOpenInStore).setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse("market://details?id=de.lemke.nakbuch"))
            )
        }
    }

    private fun openAppInStoreOnPhone() {
        if (PhoneTypeHelper.getPhoneDeviceType(this) != PhoneTypeHelper.DEVICE_TYPE_ANDROID) return
        val remoteIntent: Intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("market://details?id=de.lemke.nakbuch"))
        try {
            remoteActivityHelper.startRemoteActivity(remoteIntent)
            startActivity(
                Intent(this, ConfirmationActivity::class.java)
                    .putExtra(
                        ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.OPEN_ON_PHONE_ANIMATION
                    )
                    .putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Auf Telefon geöffnet")
            )
        } catch (cancellationException: CancellationException) {
            startActivity(
                Intent(this, ConfirmationActivity::class.java)
                    .putExtra(
                        ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.FAILURE_ANIMATION
                    ).putExtra(ConfirmationActivity.EXTRA_MESSAGE, cancellationException.toString())
            )
        }
    }
}
package de.lemke.nakbuch.ui

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.OpenAppUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import dev.oneuiproject.oneui.layout.AppInfoLayout
import dev.oneuiproject.oneui.layout.AppInfoLayout.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : AppCompatActivity(), OnClickListener {
    private lateinit var appInfoLayout: AppInfoLayout
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>
    private var clicks = 0

    @Inject
    lateinit var openApp: OpenAppUseCase

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appInfoLayout = findViewById(R.id.app_info_layout)
        appInfoLayout.addOptionalText(getString(R.string.aboutPageOptionalText))
        appInfoLayout.isExpandable = true
        appInfoLayout.status = LOADING
        //status: LOADING NO_UPDATE UPDATE_AVAILABLE NOT_UPDATEABLE NO_CONNECTION
        appInfoLayout.setMainButtonClickListener(this)
        val version: View = appInfoLayout.findViewById(dev.oneuiproject.oneui.R.id.app_info_version)
        version.setOnClickListener {
            clicks++
            if (clicks > 5) {
                clicks = 0
                lifecycleScope.launch {
                    val newDevModeEnabled = !getUserSettings().devModeEnabled
                    updateUserSettings { it.copy(devModeEnabled = newDevModeEnabled) }
                }
                startActivity(Intent().setClass(applicationContext, SplashActivity::class.java))
            }
        }
        findViewById<View>(R.id.about_btn_open_in_store).setOnClickListener { openApp(packageName, false) }
        findViewById<View>(R.id.about_btn_open_oneui_github).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.oneUIGithubLink))))
        }
        findViewById<View>(R.id.about_btn_help).setOnClickListener { startActivity(Intent(this@AboutActivity, HelpActivity::class.java)) }
        findViewById<View>(R.id.about_btn_about_me).setOnClickListener {
            startActivity(
                Intent(
                    this@AboutActivity,
                    AboutMeActivity::class.java
                )
            )
        }
        checkUpdate()
    }

    // Checks that the update is not stalled during 'onResume()'.
    // However, you should execute this check at all entry points into the app.
    override fun onResume() {
        super.onResume()
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    startUpdateFlow()
                }
            }
    }

    override fun onUpdateClicked(v: View?) {
        startUpdateFlow()
    }

    override fun onRetryClicked(v: View?) {
        appInfoLayout.status = LOADING
        checkUpdate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATEREQUESTCODE) {
            if (resultCode != RESULT_OK) {
                Log.e("Update: ", "Update flow failed! Result code: $resultCode")
                Toast.makeText(this@AboutActivity, "Fehler beim Update-Prozess: $resultCode", Toast.LENGTH_LONG).show()
                appInfoLayout.status = NO_CONNECTION
            }
        }
    }

    private fun checkUpdate() {
        // Returns an intent object that you use to check for an update.
        appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                //&& appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                this.appUpdateInfo = appUpdateInfo
                appInfoLayout.status = UPDATE_AVAILABLE
            }
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                this.appUpdateInfo = appUpdateInfo
                appInfoLayout.status = NO_UPDATE
            }
        }
        appUpdateInfoTask.addOnFailureListener { appUpdateInfo: Exception ->
            Toast.makeText(this@AboutActivity, appUpdateInfo.message, Toast.LENGTH_LONG).show()
            appInfoLayout.status = NO_CONNECTION
        }
    }

    private fun startUpdateFlow() {
        try {
            appUpdateManager.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                appUpdateInfo,  //AppUpdateType.FLEXIBLE,
                AppUpdateType.IMMEDIATE,
                this,
                UPDATEREQUESTCODE
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val UPDATEREQUESTCODE = 5
    }
}
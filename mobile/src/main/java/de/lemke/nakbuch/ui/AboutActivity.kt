package de.lemke.nakbuch.ui

import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.layout.AboutPage
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.OpenAppUseCase
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var aboutPage: AboutPage
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>

    @Inject
    lateinit var openApp: OpenAppUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        context = this
        aboutPage = findViewById(R.id.about_page)
        aboutPage.setToolbarExpandable(true)
        aboutPage.setUpdateState(AboutPage.LOADING)
        //LOADING NO_UPDATE UPDATE_AVAILABLE NOT_UPDATEABLE NO_CONNECTION
        appUpdateManager = AppUpdateManagerFactory.create(context)
        aboutPage.setUpdateButtonOnClickListener { startUpdateFlow() }
        aboutPage.setRetryButtonOnClickListener {
            aboutPage.setUpdateState(AboutPage.LOADING)
            checkUpdate()
        }
        findViewById<View>(R.id.about_btn_open_in_store).setOnClickListener { openApp(packageName, false) }
        findViewById<View>(R.id.about_btn_open_oneui_github).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.oneUIGithubLink))))
        }
        findViewById<View>(R.id.about_btn_help).setOnClickListener { startActivity(Intent(context, HelpActivity::class.java)) }
        findViewById<View>(R.id.about_btn_about_me).setOnClickListener { startActivity(Intent(context, AboutMeActivity::class.java)) }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATEREQUESTCODE) {
            if (resultCode != RESULT_OK) {
                Log.e("Update: ", "Update flow failed! Result code: $resultCode")
                Toast.makeText(context, "Fehler beim Update-Prozess: $resultCode", Toast.LENGTH_LONG).show()
                aboutPage.setUpdateState(AboutPage.NO_CONNECTION)
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
                aboutPage.setUpdateState(AboutPage.UPDATE_AVAILABLE)
            }
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                this.appUpdateInfo = appUpdateInfo
                aboutPage.setUpdateState(AboutPage.NO_UPDATE)
            }
        }
        appUpdateInfoTask.addOnFailureListener { appUpdateInfo: Exception ->
            Toast.makeText(context, appUpdateInfo.message, Toast.LENGTH_LONG).show()
            aboutPage.setUpdateState(AboutPage.NO_CONNECTION)
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
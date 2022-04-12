package de.lemke.nakbuch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import de.dlyt.yanndroid.oneui.layout.AboutPage
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.utils.Constants
import nl.dionsegijn.konfetti.xml.KonfettiView

class AboutActivity : AppCompatActivity() {
    private lateinit var mActivity: Activity
    private lateinit var mContext: Context
    private lateinit var aboutPage: AboutPage
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>
    private val UPDATEREQUESTCODE = 5
    private lateinit var sp: SharedPreferences
    private lateinit var konfettiView: KonfettiView
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        mContext = this
        sp = mContext.getSharedPreferences(getString(R.string.preference_file_default), MODE_PRIVATE)
        konfettiView = findViewById(R.id.konfettiViewAboutPage)
        aboutPage = findViewById(R.id.about_page)
        aboutPage.setToolbarExpandable(true)
        aboutPage.setUpdateState(AboutPage.LOADING)
        //LOADING NO_UPDATE UPDATE_AVAILABLE NOT_UPDATEABLE NO_CONNECTION
        appUpdateManager = AppUpdateManagerFactory.create(mContext)
        aboutPage.setUpdateButtonOnClickListener {
            try {
                appUpdateManager.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,  //AppUpdateType.IMMEDIATE,
                    AppUpdateType.FLEXIBLE,
                    mActivity,
                    UPDATEREQUESTCODE
                )
            } catch (e: SendIntentException) {
                e.printStackTrace()
            }
        }
        aboutPage.setRetryButtonOnClickListener {
            aboutPage.setUpdateState(AboutPage.LOADING)
            checkUpdate()
        }
        checkUpdate()
        findViewById<View>(R.id.about_btn_open_in_store).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            try {
                startActivity(intent)
            } catch (anfe: ActivityNotFoundException) {
                intent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                startActivity(intent)
            }
        }
        findViewById<View>(R.id.about_btn_open_oneui_github).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Yanndroid/OneUI-Design-Library")
                )
            )
        }
        findViewById<View>(R.id.about_btn_help).setOnClickListener {
            startActivity(
                Intent(mContext, HelpActivity::class.java)
            )
        }
        findViewById<View>(R.id.about_btn_support_me).setOnClickListener {
            startActivity(
                Intent(mContext, SupportMeActivity::class.java)
            )
        }
        findViewById<View>(R.id.about_btn_about_me).setOnClickListener {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)))
            )
        }
        findViewById<View>(R.id.about_btn_tiktk_troll).setOnClickListener {
            //Toast.makeText(mContext, "TikTok gehört weggebannt...", Toast.LENGTH_SHORT).show();
            val s: MutableSet<String> = HashSet(sp.getStringSet("discoveredEasterEggs", HashSet())!!)
            if (!s.contains(getString(R.string.easterEggEntryTikTok))) {
                s.add(getString(R.string.easterEggEntryTikTok))
                sp.edit().putStringSet("discoveredEasterEggs", s).apply()
                konfettiView.start(Constants.party1())
                Handler(Looper.getMainLooper()).postDelayed(
                    { konfettiView.start(Constants.party2()) },
                    Constants.partyDelay2.toLong()
                )
                Handler(Looper.getMainLooper()).postDelayed(
                    { konfettiView.start(Constants.party3()) },
                    Constants.partyDelay3.toLong()
                )
                Toast.makeText(
                    mContext,
                    getString(R.string.easterEggDiscovered) + getString(R.string.easterEggEntryTikTok),
                    Toast.LENGTH_SHORT
                ).show()
            }
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    //Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                    Uri.parse("https://www.youtube.com/watch?v=o-YBDTqX_ZU")
                )
            )
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
            Toast.makeText(mContext, appUpdateInfo.message, Toast.LENGTH_LONG).show()
            aboutPage.setUpdateState(AboutPage.NO_CONNECTION)
        }
    }
}
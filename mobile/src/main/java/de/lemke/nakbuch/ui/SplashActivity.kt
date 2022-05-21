package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.layout.SplashView
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.InitDataBaseUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val coroutineContext: CoroutineContext = Dispatchers.Main
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
    private val mContext = this
    private lateinit var initDatabaseJob: Job
    private lateinit var splashView: SplashView
    //private var launchCanceled = false

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var initDataBase: InitDataBaseUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this, "4099ff")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashView = findViewById(R.id.splash)
        coroutineScope.launch {
            when (intent.getStringExtra("buchModeString")) {
                BuchMode.Gesangbuch.toString() -> {
                    updateUserSettings{ it.copy(buchMode = BuchMode.Gesangbuch) }
                    splashView.setImage(
                        AppCompatResources.getDrawable(mContext, R.drawable.ic_launcher_foreground2),
                        AppCompatResources.getDrawable(mContext, R.drawable.ic_launcher_background)
                    )
                    splashView.text = BuchMode.Gesangbuch.toString()
                }
                BuchMode.Chorbuch.toString() -> {
                    updateUserSettings{ it.copy(buchMode = BuchMode.Chorbuch) }
                    splashView.setImage(
                        AppCompatResources.getDrawable(mContext, R.drawable.ic_launcher_foreground2),
                        AppCompatResources.getDrawable(mContext, R.drawable.ic_launcher_background)
                    )
                    splashView.text = BuchMode.Chorbuch.toString()
                }
            }
        }
        splashView.setSplashAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (!initDatabaseJob.isCompleted)
                    Handler(Looper.getMainLooper()).postDelayed({ splashView.startSplashAnimation() }, 300)
                else
                    //if (!launchCanceled)
                        launchApp()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        initDatabaseJob = initDataBase()
        /*initDatabaseJob.invokeOnCompletion {
            splashView.startSplashAnimation()
        }*/

    }

    private fun launchApp() {
        startActivity(Intent().setClass(applicationContext, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onPause() {
        super.onPause()
        //launchCanceled = true
    }

    override fun onResume() {
        super.onResume()
        //if (launchCanceled) launchApp()
        Handler(Looper.getMainLooper()).postDelayed({ splashView.startSplashAnimation() }, 300)
    }
}

@SuppressLint("CustomSplashScreen")
class SplashActivityGesangbuch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        startActivity(
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchModeString", BuchMode.Gesangbuch.toString())
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

@SuppressLint("CustomSplashScreen")
class SplashActivityChorbuch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        startActivity(
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchModeString", BuchMode.Chorbuch.toString())
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

@SuppressLint("CustomSplashScreen")
class SplashActivityJugendliederbuch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        startActivity(
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchModeString", BuchMode.Jugendliederbuch.toString())
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
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
import de.lemke.nakbuch.domain.InitDatabaseUseCase
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
    private lateinit var initDatabaseJob: Job
    private lateinit var splashView: SplashView
    //private var launchCanceled = false

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var initDatabase: InitDatabaseUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this, "4099ff")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashView = findViewById(R.id.splash)
        coroutineScope.launch {
            val buchMode = BuchMode.fromInt(intent.getIntExtra("buchMode", -1))
            if (buchMode != null) {
                updateUserSettings { it.copy(buchMode = buchMode) }
                splashView.setImage(
                    AppCompatResources.getDrawable(this@SplashActivity, R.drawable.ic_launcher_foreground2),
                    AppCompatResources.getDrawable(this@SplashActivity, R.drawable.ic_launcher_background)
                )
                splashView.text = buchMode.toString()
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

        initDatabaseJob = initDatabase()
        /*initDatabaseJob.invokeOnCompletion {
            splashView.startSplashAnimation()
        }*/

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

    private fun launchApp() {
        startActivity(Intent().setClass(applicationContext, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

@SuppressLint("CustomSplashScreen")
class SplashActivityGesangbuch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        startActivity(
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchMode", BuchMode.Gesangbuch.toInt())
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
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchMode", BuchMode.Chorbuch.toInt())
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
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchMode", BuchMode.Jugendliederbuch.toInt())
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
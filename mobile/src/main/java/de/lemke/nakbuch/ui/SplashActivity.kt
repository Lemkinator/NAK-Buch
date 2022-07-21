package de.lemke.nakbuch.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.InitDatabaseUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import dev.oneuiproject.oneui.layout.SplashLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var splashView: SplashLayout
    private var initDatabaseJob: Job? = null
    private var launchCanceled = false

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var initDatabase: InitDatabaseUseCase

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashView = findViewById(R.id.splash)
        lifecycleScope.launch {
            val buchMode = BuchMode.fromInt(intent.getIntExtra("buchMode", -1))
            if (buchMode != null) {
                updateUserSettings { it.copy(buchMode = buchMode) }
                splashView.setImage(
                    AppCompatResources.getDrawable(this@SplashActivity, R.drawable.ic_launcher_foreground2),
                    AppCompatResources.getDrawable(this@SplashActivity, R.drawable.ic_launcher_background)
                )
                splashView.text = buchMode.toString()
            } else splashView.text = getString(R.string.app_name)
            if (getUserSettings().devModeEnabled) {
                val devText: Spannable = SpannableString(" Dev")
                devText.setSpan(
                    ForegroundColorSpan(getColor(R.color.orange)),
                    0,
                    devText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                splashView.text = splashView.text + devText
                //TODO: ((TextView) splashView.findViewById(R.id.oui_splash_text)).append(dev_text);
            }
        }
        splashView.setSplashAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (!initDatabaseJob!!.isCompleted)
                    lifecycleScope.launch {
                        delay(300)
                        splashView.startSplashAnimation()
                    }
                else if (!launchCanceled) launchApp()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        launchCanceled = true
    }

    override fun onResume() {
        super.onResume()
        launchCanceled = false
        initDatabaseJob?.cancel()
        initDatabaseJob = initDatabase()
        lifecycleScope.launch {
            delay(400)
            splashView.startSplashAnimation()
        }
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
        startActivity(
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchMode", BuchMode.Jugendliederbuch.toInt())
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

@SuppressLint("CustomSplashScreen")
class SplashActivityJBErgaenzungsheft : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent().setClass(applicationContext, SplashActivity::class.java).putExtra("buchMode", BuchMode.JBErgaenzungsheft.toInt())
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
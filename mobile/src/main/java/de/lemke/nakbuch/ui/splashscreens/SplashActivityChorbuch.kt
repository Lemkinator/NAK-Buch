package de.lemke.nakbuch.ui.splashscreens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.layout.SplashView
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.ui.MainActivity

class SplashActivityChorbuch : AppCompatActivity() {
    private var launchCanceled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this, "4099ff")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_chorbuch)

        val splashView = findViewById<SplashView>(R.id.splash)
        Handler(Looper.getMainLooper()).postDelayed({ splashView.startSplashAnimation() }, 300)

        splashView.setSplashAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (!launchCanceled) launchApp()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private fun launchApp() {
        startActivity(
            Intent().setClass(applicationContext, MainActivity::class.java)
                .putExtra("gesangbuchSelected", false)
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onPause() {
        super.onPause()
        launchCanceled = true
    }

    override fun onResume() {
        super.onResume()
        if (launchCanceled) launchApp()
    }
}
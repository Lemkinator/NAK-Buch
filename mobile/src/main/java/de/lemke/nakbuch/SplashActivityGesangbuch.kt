package de.lemke.nakbuch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import de.dlyt.yanndroid.oneui.layout.SplashView
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.utils.Constants

class SplashActivityGesangbuch : AppCompatActivity() {
    private var launchCanceled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this, "4099ff")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_gesangbuch)

        val splashView = findViewById<SplashView>(R.id.splash)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ splashView.startSplashAnimation() }, 500)

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
                .putExtra("Modus", Constants.GESANGBUCHMODE)
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
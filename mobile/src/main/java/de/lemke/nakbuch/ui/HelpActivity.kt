package de.lemke.nakbuch.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.OpenBischoffAppUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import dev.oneuiproject.oneui.layout.DrawerLayout
import javax.inject.Inject

@AndroidEntryPoint
class HelpActivity : AppCompatActivity() {
    @Inject
    lateinit var openBischoffApp: OpenBischoffAppUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_help)
        drawerLayout.setNavigationButtonIcon(AppCompatResources.getDrawable(this, R.drawable.ic_baseline_oui_back_24))
        drawerLayout.setNavigationButtonOnClickListener { finish() }
        findViewById<View>(R.id.contactMeButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            intent.putExtra(Intent.EXTRA_TEXT, "")
            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.noEmailAppInstalled), Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<View>(R.id.openPlaystoreBischoffGesangbuch).setOnClickListener { openBischoffApp(BuchMode.Gesangbuch, false) }
        findViewById<View>(R.id.openPlaystoreBischoffChorbuch).setOnClickListener { openBischoffApp(BuchMode.Chorbuch, false) }
    }
}
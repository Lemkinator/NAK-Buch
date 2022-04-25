package de.lemke.nakbuch.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.utils.AppUtils

class HelpActivity : AppCompatActivity() {
    private lateinit var mContext: Context
    private lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.activity_help)
        drawerLayout = findViewById(R.id.drawer_help)
        drawerLayout.setNavigationButtonIcon(
            AppCompatResources.getDrawable(
                mContext,
                de.dlyt.yanndroid.oneui.R.drawable.ic_oui_back
            )
        )
        drawerLayout.setNavigationButtonOnClickListener { finish() }
        findViewById<View>(R.id.contactMeButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            intent.putExtra(Intent.EXTRA_SUBJECT, "NAK Buch App")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(mContext, "Keine E-mail-App installiert...", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        findViewById<View>(R.id.openPlaystoreBischoffGesangbuch).setOnClickListener {
            AppUtils.openBischoffApp(mContext, BuchMode.Gesangbuch)
        }
        findViewById<View>(R.id.openPlaystoreBischoffChorbuch).setOnClickListener {
            AppUtils.openBischoffApp(mContext, BuchMode.Chorbuch)
        }
    }
}
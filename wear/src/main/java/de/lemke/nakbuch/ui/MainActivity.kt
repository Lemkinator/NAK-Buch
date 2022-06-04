package de.lemke.nakbuch.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.activity.ConfirmationActivity
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var buchMode: BuchMode
    private lateinit var hymnNrInput: String
    private lateinit var tvHymnNrTitle: TextView
    private lateinit var buttonSwitchMode: MaterialButton
    private var inputOngoing = false
    private var refreshHandler: Handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable = Runnable {
        inputOngoing = false
        previewHymn(hymnNrInput)
    }

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var getHymnCount: GetHymnCountUseCase

    @Inject
    lateinit var getHymn: GetHymnUseCase

    @Inject
    lateinit var setPrivateTexts: SetPrivateTextsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this, resources.getString(R.color.primary_color))
        ThemeUtil.setDarkMode(this, ThemeUtil.DARK_MODE_ENABLED)
        setContentView(R.layout.activity_main)
        tvHymnNrTitle = findViewById(R.id.hymnTitlePreview)
        buttonSwitchMode = findViewById(R.id.buttonSwitchMode)
        lifecycleScope.launch {
            buchMode = getUserSettings().buchMode
            hymnNrInput = getUserSettings().number
            previewHymn(hymnNrInput)
            findViewById<View>(R.id.b_0).setOnClickListener { addToHymnNrInput("0") }
            findViewById<View>(R.id.b_1).setOnClickListener { addToHymnNrInput("1") }
            findViewById<View>(R.id.b_2).setOnClickListener { addToHymnNrInput("2") }
            findViewById<View>(R.id.b_3).setOnClickListener { addToHymnNrInput("3") }
            findViewById<View>(R.id.b_4).setOnClickListener { addToHymnNrInput("4") }
            findViewById<View>(R.id.b_5).setOnClickListener { addToHymnNrInput("5") }
            findViewById<View>(R.id.b_6).setOnClickListener { addToHymnNrInput("6") }
            findViewById<View>(R.id.b_7).setOnClickListener { addToHymnNrInput("7") }
            findViewById<View>(R.id.b_8).setOnClickListener { addToHymnNrInput("8") }
            findViewById<View>(R.id.b_9).setOnClickListener { addToHymnNrInput("9") }
            findViewById<View>(R.id.b_z).setOnClickListener {
                hymnNrInput = if (hymnNrInput.isNotEmpty()) hymnNrInput.substring(0, hymnNrInput.length - 1) else ""
                inputOngoing = true
                previewInput()
            }
            findViewById<View>(R.id.b_ok).setOnClickListener {
                showHymn(hymnNrInput)
                refreshHandler.removeCallbacks(refreshRunnable)
                inputOngoing = false
                previewHymn(hymnNrInput)
            }
            updateButtonSwitchMode()
            buttonSwitchMode.setOnClickListener {
                lifecycleScope.launch {
                    buchMode = if (buchMode == BuchMode.Gesangbuch) BuchMode.Chorbuch else BuchMode.Gesangbuch
                    updateUserSettings {it.copy(buchMode = buchMode)}
                    updateButtonSwitchMode()
                    startActivity(
                        Intent(this@MainActivity, ConfirmationActivity::class.java)
                            .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                            .putExtra(
                                ConfirmationActivity.EXTRA_MESSAGE,
                                getString(if (buchMode == BuchMode.Gesangbuch) R.string.titleGesangbuch else R.string.titleChorbuch)
                            )
                    )
                    previewHymn(hymnNrInput)
                }
            }
            findViewById<MaterialButton>(R.id.buttonInfo).setOnClickListener {
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(Receiver(), IntentFilter(Intent.ACTION_SEND))

        //TODO better handling of square/round
        //if (resources.configuration.isScreenRound) { }
    }

    private fun updateButtonSwitchMode() {
        buttonSwitchMode.text = getString(if (buchMode == BuchMode.Gesangbuch) R.string.titleChorbuch else R.string.titleGesangbuch)
    }

    private fun addToHymnNrInput(s: String) {
        if (!inputOngoing) {
            inputOngoing = true
            hymnNrInput = s
        } else {
            if (hymnNrInput.length < 3) hymnNrInput += s
            else hymnNrInput = s
        }
        previewInput()
    }

    private fun previewInput() {
        refreshHandler.removeCallbacks(refreshRunnable)
        tvHymnNrTitle.text = hymnNrInput
        //TODO replace handler with coroutine + delay
        refreshHandler.postDelayed(refreshRunnable, 1500)
    }

    private fun previewHymn(nr: String) {
        lifecycleScope.launch {
            val hymnNr = nr.toIntOrNull()
            if (hymnNr != null && hymnNr > 0 && hymnNr < getHymnCount(buchMode)) {
                    val hymn = getHymn(buchMode, hymnNr)
                    tvHymnNrTitle.text = hymn.numberAndTitle
                    updateUserSettings {it.copy(number = nr)}
            } else {
                tvHymnNrTitle.text = ""
                updateUserSettings {it.copy(number = "")}
            }
        }
    }


    private fun showHymn(nr: String) {
            val hymnNr = nr.toIntOrNull()
            if (hymnNr != null && hymnNr > 0 && hymnNr < getHymnCount(buchMode)) {
                lifecycleScope.launch {
                    val hymn = getHymn(buchMode, hymnNr)
                    startActivity(
                        Intent(this@MainActivity, TextviewActivity::class.java)
                            .putExtra("nrAndTitle", hymn.numberAndTitle)
                            .putExtra("text", hymn.text)
                            .putExtra("copyright", hymn.copyright)
                    )
                }
            }
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lifecycleScope.launch { if (setPrivateTexts(intent)) recreate() }
        }
    }
}
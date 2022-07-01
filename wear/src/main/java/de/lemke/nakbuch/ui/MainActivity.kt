package de.lemke.nakbuch.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
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
import de.lemke.nakbuch.domain.GetHymnUseCase
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.SetPrivateTextsUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var buchMode: BuchMode
    private lateinit var tvHymnNrTitle: TextView
    private lateinit var buttonSwitchMode: MaterialButton
    private lateinit var inputOngoingJob: Job
    private var inputOngoing = false
    private var hymnNrInput: String = ""

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

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
            val userSettings = getUserSettings()
            buchMode = userSettings.buchMode
            hymnNrInput = userSettings.number
            inputOngoingJob = CoroutineScope(Dispatchers.Default).launch { inputOngoing = false }
            previewHymn()
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
                previewHymn()
            }
            findViewById<View>(R.id.b_ok).setOnClickListener { openHymn() }
            updateButtonSwitchMode()
            buttonSwitchMode.setOnClickListener {
                lifecycleScope.launch {
                    buchMode = when (buchMode) {
                        BuchMode.Gesangbuch -> BuchMode.Chorbuch
                        BuchMode.Chorbuch -> BuchMode.Jugendliederbuch
                        BuchMode.Jugendliederbuch -> BuchMode.JBErgaenzungsheft
                        BuchMode.JBErgaenzungsheft -> BuchMode.Gesangbuch
                    }
                    updateButtonSwitchMode()
                    startActivity(
                        Intent(this@MainActivity, ConfirmationActivity::class.java)
                            .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                            .putExtra(
                                ConfirmationActivity.EXTRA_MESSAGE, when (buchMode) {
                                    BuchMode.Gesangbuch -> getString(R.string.titleGesangbuch)
                                    BuchMode.Chorbuch -> getString(R.string.titleChorbuch)
                                    BuchMode.Jugendliederbuch -> getString(R.string.titleJugendliederbuch)
                                    BuchMode.JBErgaenzungsheft -> getString(R.string.titleJBErgaenzungsheft)
                                }
                            )
                    )
                    updateUserSettings { it.copy(buchMode = buchMode) }
                    previewHymn()
                }
            }
            findViewById<MaterialButton>(R.id.buttonInfo).setOnClickListener {
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(Receiver(), IntentFilter(Intent.ACTION_SEND))

        //better handling of square/round screens
        //if (resources.configuration.isScreenRound) { }
    }

    private fun updateButtonSwitchMode() {
        buttonSwitchMode.text = when (buchMode) {
            BuchMode.Gesangbuch -> getString(R.string.titleGesangbuch)
            BuchMode.Chorbuch -> getString(R.string.titleChorbuch)
            BuchMode.Jugendliederbuch -> getString(R.string.titleJugendliederbuch)
            BuchMode.JBErgaenzungsheft -> getString(R.string.titleJBErgaenzungsheft)
        }
    }

    private fun addToHymnNrInput(s: String) {
        if (!inputOngoing) {
            inputOngoing = true
            hymnNrInput = s
        } else {
            if (hymnNrInput.length < 3) hymnNrInput += s
            else hymnNrInput = s
        }
        previewHymn()
    }

    private fun previewHymn() {
        inputOngoingJob.cancel()
        inputOngoingJob = CoroutineScope(Dispatchers.Default).launch {
            delay(3000)
            inputOngoing = false
        }
        tvHymnNrTitle.text = hymnNrInput
        lifecycleScope.launch {
            val hymnNr = hymnNrInput.toIntOrNull()
            if (hymnNr != null && hymnNr > 0 && hymnNr <= buchMode.hymnCount) {
                val hymn = getHymn(buchMode, hymnNr)
                tvHymnNrTitle.text = hymn.numberAndTitle
                updateUserSettings { it.copy(number = hymnNrInput) }
            } else {
                tvHymnNrTitle.text = ""
                hymnNrInput = ""
                updateUserSettings { it.copy(number = "") }
            }
        }
    }

    private fun openHymn() {
        val hymnNr = hymnNrInput.toIntOrNull()
        if (hymnNr != null && hymnNr > 0 && hymnNr <= buchMode.hymnCount) {
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
        inputOngoing = false
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lifecycleScope.launch {
                if (setPrivateTexts(intent)) {
                    Log.d("Private Text on wear", "succesful")
                    //recreate()
                }
            }
        }
    }
}
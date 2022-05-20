package de.lemke.nakbuch.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.activity.ConfirmationActivity
import com.google.android.material.button.MaterialButton
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.*
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var buchMode: BuchMode
    private lateinit var mContext: Context
    private lateinit var hymnNrInput: String
    private lateinit var tvHymnNrTitle: TextView
    private lateinit var buttonSwitchMode: MaterialButton
    private var inputOngoing = false
    private var refreshHandler: Handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable = Runnable {
        inputOngoing = false
        previewHymn(hymnNrInput)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil(this)
        ThemeUtil.setDarkMode(this, ThemeUtil.DARK_MODE_ENABLED)
        setContentView(R.layout.activity_main)
        mContext = this
        buchMode = GetBuchModeUseCase()()
        tvHymnNrTitle = findViewById(R.id.hymnTitlePreview)
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
        buttonSwitchMode = findViewById(R.id.buttonSwitchMode)
        updateButtonSwitchMode()
        buttonSwitchMode.setOnClickListener {
            buchMode = if (buchMode == BuchMode.Gesangbuch) BuchMode.Chorbuch else BuchMode.Gesangbuch
            SetBuchModeUseCase()(buchMode)
            updateButtonSwitchMode()
            startActivity(
                Intent(mContext, ConfirmationActivity::class.java)
                    .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                    .putExtra(
                        ConfirmationActivity.EXTRA_MESSAGE,
                        getString(if (buchMode == BuchMode.Gesangbuch) R.string.titleGesangbuch else R.string.titleChorbuch)
                    )
            )
            previewHymn(hymnNrInput)
        }
        findViewById<MaterialButton>(R.id.buttonInfo).setOnClickListener {
            startActivity(Intent(mContext, InfoActivity::class.java))
        }
        hymnNrInput = GetNumberUseCase()()
        previewHymn(hymnNrInput)

        LocalBroadcastManager.getInstance(this).registerReceiver(Receiver(), IntentFilter(Intent.ACTION_SEND))

        //if (resources.configuration.isScreenRound) { }
    }

    private fun updateButtonSwitchMode() {
        buttonSwitchMode.text = getString(if (buchMode == BuchMode.Gesangbuch) R.string.titleChorbuch else R.string.titleGesangbuch)
    }

    private fun addToHymnNrInput(s: String) {
        if (inputOngoing) {
            if (s.length == 1 && hymnNrInput.length < 3) {
                hymnNrInput += s
            }
        } else {
            if (s.length == 1) {
                inputOngoing = true
                hymnNrInput = s
            }
        }
        previewInput()
    }

    private fun previewInput() {
        refreshHandler.removeCallbacks(refreshRunnable)
        tvHymnNrTitle.text = hymnNrInput
        refreshHandler.postDelayed(refreshRunnable, 1500)
    }

    private fun previewHymn(nr: String) {
        val hymnNr: Int
        try {
            hymnNr = nr.toInt()
            if (hymnNr > 0 && hymnNr < GetHymnCountUseCase()(buchMode)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val hymn = GetHymnUseCase()(buchMode, hymnNr)
                    withContext(Dispatchers.Main){
                        tvHymnNrTitle.text = hymn.numberAndTitle
                    }
                }
                SetNumberUseCase()(nr)
            } else {
                tvHymnNrTitle.text = ""
                SetNumberUseCase()("")
            }
        } catch (e: NumberFormatException) {
            Log.d("InvalidHymNr: ", e.toString())
            tvHymnNrTitle.text = ""
            SetNumberUseCase()("")
        }
    }


    private fun showHymn(nr: String) {
        val hymnNr: Int
        try {
            hymnNr = nr.toInt()
            if (hymnNr > 0 && hymnNr < GetHymnCountUseCase()(buchMode)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val hymn = GetHymnUseCase()(buchMode, hymnNr)
                    withContext(Dispatchers.Main){
                        startActivity(
                            Intent(mContext, TextviewActivity::class.java)
                                .putExtra("nrAndTitle", hymn.numberAndTitle)
                                .putExtra("text", hymn.text)
                                .putExtra("copyright", hymn.copyright)
                        )
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Log.d("InvalidHymNr: ", e.toString())
        }

    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SetPrivateTextsUseCase()(intent)) recreate()
        }
    }
}
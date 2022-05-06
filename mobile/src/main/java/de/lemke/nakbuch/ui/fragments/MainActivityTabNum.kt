package de.lemke.nakbuch.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.hymns.GetHymnUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.settings.*
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.xml.KonfettiView


class MainActivityTabNum : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mActivity: AppCompatActivity
    private lateinit var mContext: Context
    private lateinit var konfettiView: KonfettiView
    private lateinit var buchMode: BuchMode
    private var inputOngoing = false
    private var hymnNrInput: String = ""
    private lateinit var tvHymnNrTitle: TextView
    private lateinit var tvHymnText: TextView
    private lateinit var refreshHandler: Handler
    private lateinit var refreshRunnable: Runnable
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = activity as AppCompatActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mRootView = inflater.inflate(R.layout.fragment_tab_num, container, false)
        return mRootView
    }

    @SuppressLint("RtlHardcoded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buchMode = GetBuchModeUseCase()()
        konfettiView = mActivity.findViewById(R.id.konfettiViewTab0)
        tvHymnNrTitle = mActivity.findViewById(R.id.hymnTitlePreview)
        tvHymnText = mActivity.findViewById(R.id.hymnTextPreview)

        val switchSideButton1 = mActivity.findViewById<MaterialButton>(R.id.switchSideButton1)
        val switchSideButton2 = mActivity.findViewById<MaterialButton>(R.id.switchSideButton2)
        switchSideButton1.setOnClickListener {
            SetBooleanSettingUseCase()("numberFieldSideRight", false)
            switchSideButton1.visibility = View.GONE
            switchSideButton2.visibility = View.VISIBLE
        }
        switchSideButton2.setOnClickListener {
            SetBooleanSettingUseCase()("numberFieldSideRight", true)
            switchSideButton2.visibility = View.GONE
            switchSideButton1.visibility = View.VISIBLE
        }
        if (GetBooleanSettingUseCase()("numberFieldSideRight", true)) {
            switchSideButton2.visibility = View.GONE
            switchSideButton1.visibility = View.VISIBLE
        } else {
            switchSideButton1.visibility = View.GONE
            switchSideButton2.visibility = View.VISIBLE
        }
        mActivity.findViewById<View>(R.id.b_0)
            .setOnClickListener { addToHymnNrInput("0") }
        mActivity.findViewById<View>(R.id.b_1)
            .setOnClickListener { addToHymnNrInput("1") }
        mActivity.findViewById<View>(R.id.b_2)
            .setOnClickListener { addToHymnNrInput("2") }
        mActivity.findViewById<View>(R.id.b_3)
            .setOnClickListener { addToHymnNrInput("3") }
        mActivity.findViewById<View>(R.id.b_4)
            .setOnClickListener { addToHymnNrInput("4") }
        mActivity.findViewById<View>(R.id.b_5)
            .setOnClickListener { addToHymnNrInput("5") }
        mActivity.findViewById<View>(R.id.b_6)
            .setOnClickListener { addToHymnNrInput("6") }
        mActivity.findViewById<View>(R.id.b_7)
            .setOnClickListener { addToHymnNrInput("7") }
        mActivity.findViewById<View>(R.id.b_8)
            .setOnClickListener { addToHymnNrInput("8") }
        mActivity.findViewById<View>(R.id.b_9)
            .setOnClickListener { addToHymnNrInput("9") }
        mActivity.findViewById<View>(R.id.b_z).setOnClickListener {
            hymnNrInput = if (hymnNrInput.isNotEmpty()) hymnNrInput.substring(
                0,
                hymnNrInput.length - 1
            ) else ""
            inputOngoing = true
            previewInput()
        }
        mActivity.findViewById<View>(R.id.b_ok).setOnClickListener {
            showHymn(hymnNrInput)
            refreshHandler.removeCallbacks(refreshRunnable)
            inputOngoing = false
            previewHymn(hymnNrInput)
        }
        tvHymnText.setOnClickListener {
            showHymn(hymnNrInput)
            refreshHandler.removeCallbacks(refreshRunnable)
            inputOngoing = false
            previewHymn(hymnNrInput)
        }
        refreshHandler = Handler(Looper.getMainLooper())
        refreshRunnable = Runnable {
            inputOngoing = false
            previewHymn(hymnNrInput)
        }
        hymnNrInput = GetNumberUseCase()()
        previewHymn(hymnNrInput)
    }

    @Synchronized
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
        tvHymnText.text = ""
        refreshHandler.postDelayed(refreshRunnable, 1500L)
    }

    private fun previewHymn(number: String) {
        val hymnNr = validHymnNumber(number)
        if (hymnNr > 0) {
            lateinit var hymn: Hymn
            CoroutineScope(Dispatchers.IO).launch {
                SetNumberUseCase()(number)
                hymn = GetHymnUseCase()(buchMode, hymnNr)
                withContext((Dispatchers.Main)) {
                    tvHymnNrTitle.text = hymn.numberAndTitle
                    tvHymnText.text = hymn.text
                }
            }
        } else {
            tvHymnNrTitle.text = ""
            tvHymnText.text = ""
            hymnNrInput = ""
            SetNumberUseCase()("")
        }
    }

    private fun showHymn(number: String) {
        val hymnNr = validHymnNumber(number)
        if (hymnNr > 0) {
            startActivity(
                Intent(mContext, TextviewActivity::class.java).putExtra("nr", hymnNr)
            )
        }
    }

    private fun validHymnNumber(hymnNr: String): Int {
        if (hymnNr.isBlank()) return -1
        if (hymnNr == "999") {
            DiscoverEasterEggUseCase()(mContext, konfettiView, R.string.easterEggEntry999)
            return -1
        }
        if (hymnNr == "0" || hymnNr == "00" || hymnNr == "000") {
            DiscoverEasterEggUseCase()(mContext, konfettiView, R.string.easterEggEntry0)
            return -1
        }
        try {
            val result = hymnNr.toInt()
            if (buchMode == BuchMode.Chorbuch && 0 < result && result <= 462 || buchMode == BuchMode.Gesangbuch && 0 < result && result <= 438) return result
        } catch (e: Exception) {
            Log.d("validHymnNumber: Exception: ", e.toString())
        }
        return -1
    }
}
package de.lemke.nakbuch.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.DiscoverEasterEggUseCase
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import de.lemke.nakbuch.domain.hymnUseCases.GetHymnUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.ui.TextviewActivity
import kotlinx.coroutines.*
import nl.dionsegijn.konfetti.xml.KonfettiView
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityTabNum : Fragment() {
    private lateinit var rootView: View
    private lateinit var konfettiView: KonfettiView
    private lateinit var buchMode: BuchMode
    private var inputOngoing = false
    private var hymnNrInput: String = ""
    private lateinit var tvHymnNrTitle: TextView
    private lateinit var tvHymnText: TextView
    private lateinit var inputOngoingJob: Job

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    @Inject
    lateinit var getHymn: GetHymnUseCase

    @Inject
    lateinit var discoverEasterEgg: DiscoverEasterEggUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tab_num, container, false)
        return rootView
    }

    @SuppressLint("RtlHardcoded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        konfettiView = rootView.findViewById(R.id.konfettiViewTab0)
        tvHymnNrTitle = rootView.findViewById(R.id.hymnTitlePreview)
        tvHymnText = rootView.findViewById(R.id.hymnTextPreview)
        val switchSideButton1 = rootView.findViewById<MaterialButton>(R.id.switchSideButton1)
        val switchSideButton2 = rootView.findViewById<MaterialButton>(R.id.switchSideButton2)
        lifecycleScope.launch {
            buchMode = getUserSettings().buchMode
            if (getUserSettings().numberFieldRightSide) {
                switchSideButton2.visibility = View.GONE
                switchSideButton1.visibility = View.VISIBLE
            } else {
                switchSideButton1.visibility = View.GONE
                switchSideButton2.visibility = View.VISIBLE
            }
            hymnNrInput = getUserSettings().number
            tvHymnNrTitle.text = hymnNrInput
            inputOngoingJob = CoroutineScope(Dispatchers.Default).launch { inputOngoing = false }
            inputOngoingJob.invokeOnCompletion { lifecycleScope.launch { previewHymn() } }
            switchSideButton1.setOnClickListener {
                lifecycleScope.launch { updateUserSettings { it.copy(numberFieldRightSide = false) } }
                switchSideButton1.visibility = View.GONE
                switchSideButton2.visibility = View.VISIBLE
            }
            switchSideButton2.setOnClickListener {
                lifecycleScope.launch { updateUserSettings { it.copy(numberFieldRightSide = true) } }
                switchSideButton2.visibility = View.GONE
                switchSideButton1.visibility = View.VISIBLE
            }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_0)
                .setOnClickListener { addToHymnNrInput("0") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_1)
                .setOnClickListener { addToHymnNrInput("1") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_2)
                .setOnClickListener { addToHymnNrInput("2") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_3)
                .setOnClickListener { addToHymnNrInput("3") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_4)
                .setOnClickListener { addToHymnNrInput("4") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_5)
                .setOnClickListener { addToHymnNrInput("5") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_6)
                .setOnClickListener { addToHymnNrInput("6") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_7)
                .setOnClickListener { addToHymnNrInput("7") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_8)
                .setOnClickListener { addToHymnNrInput("8") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_9)
                .setOnClickListener { addToHymnNrInput("9") }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_z).setOnClickListener {
                hymnNrInput = if (hymnNrInput.isNotEmpty()) hymnNrInput.substring(0, hymnNrInput.length - 1) else ""
                inputOngoing = true
                previewHymn()
            }
            this@MainActivityTabNum.rootView.findViewById<View>(R.id.b_ok).setOnClickListener { lifecycleScope.launch { openHymn() } }
            tvHymnText.setOnClickListener { lifecycleScope.launch { openHymn() } }
        }
    }

    private fun openHymn() {
        val hymnId = HymnId.create(hymnNrInput.toIntOrNull() ?: -1, buchMode)
        if (hymnId != null) startActivity(Intent(activity, TextviewActivity::class.java).putExtra("hymnId", hymnId.toInt()))
        inputOngoing = false
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
            delay(3000) //TODO timing
            inputOngoing = false
        }
        tvHymnNrTitle.text = hymnNrInput
        lifecycleScope.launch {
            if (hymnNrInput == "666") discoverEasterEgg(konfettiView, R.string.easterEggEntry666)
            if (hymnNrInput == "999") discoverEasterEgg(konfettiView, R.string.easterEggEntry999)
            if (hymnNrInput == "0" || hymnNrInput == "00" || hymnNrInput == "000") discoverEasterEgg(konfettiView, R.string.easterEggEntry0)
            val hymnId = HymnId.create(hymnNrInput.toIntOrNull() ?: -1, buchMode)
            if (hymnId != null) {
                val hymn = getHymn(hymnId)
                updateUserSettings { it.copy(number = hymnNrInput) }
                tvHymnNrTitle.text = hymn.numberAndTitle
                tvHymnText.text = hymn.text
            } else {
                updateUserSettings { it.copy(number = "") }
                tvHymnNrTitle.text = ""
                tvHymnText.text = ""
                this@MainActivityTabNum.hymnNrInput = ""
            }
        }
    }
}
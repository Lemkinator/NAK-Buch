package de.lemke.nakbuch.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import de.dlyt.yanndroid.oneui.layout.DrawerLayout
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.utils.AssetsHelper
import de.lemke.nakbuch.domain.utils.AssetsHelper.getHymnArrayList
import de.lemke.nakbuch.domain.utils.Constants
import de.lemke.nakbuch.domain.utils.PartyUtils.Companion.discoverEasterEgg
import de.lemke.nakbuch.ui.TextviewActivity
import nl.dionsegijn.konfetti.xml.KonfettiView


class MainActivityTabNum : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mActivity: AppCompatActivity
    private lateinit var mContext: Context
    private lateinit var konfettiView: KonfettiView
    private lateinit var sp: SharedPreferences
    private lateinit var buchMode: BuchMode
    private var inputOngoing = false
    private var hymnNrInput: String = ""
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvHymnNrTitle: TextView
    private lateinit var tvHymnText: TextView
    private lateinit var hymns: ArrayList<HashMap<String, String>>
    private lateinit var refreshHandler: Handler
    private lateinit var refreshRunnable: Runnable
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = activity as AppCompatActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mRootView = inflater.inflate(R.layout.fragment_tab_num, container, false)
        return mRootView
    }

    @SuppressLint("RtlHardcoded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = mContext.getSharedPreferences(
            getString(R.string.preferenceFileDefault),
            Context.MODE_PRIVATE
        )
        drawerLayout = mActivity.findViewById(R.id.drawer_view)
        buchMode = if (sp.getBoolean("gesangbuchSelected", true)) BuchMode.Gesangbuch else BuchMode.Chorbuch
        konfettiView = mActivity.findViewById(R.id.konfettiViewTab0)
        tvHymnNrTitle = mActivity.findViewById(R.id.hymnTitlePreview)
        tvHymnText = mActivity.findViewById(R.id.hymnTextPreview)
        //val nestedScrollView: NestedScrollView = mActivity.findViewById(R.id.nestedScrollViewTabNum)
        //nestedScrollView.isNestedScrollingEnabled = false
        //tvHymnText.height = (drawerLayout.height - 5 * resources.getDimension(R.dimen.number_button_height)).toInt()

        val switchSideButton1 = mActivity.findViewById<MaterialButton>(R.id.switchSideButton1)
        val switchSideButton2 = mActivity.findViewById<MaterialButton>(R.id.switchSideButton2)
        switchSideButton1.setOnClickListener {
            sp.edit().putBoolean("numberFieldSideRight", false).apply()
            switchSideButton1.visibility = View.GONE
            switchSideButton2.visibility = View.VISIBLE
        }
        switchSideButton2.setOnClickListener {
            sp.edit().putBoolean("numberFieldSideRight", true).apply()
            switchSideButton2.visibility = View.GONE
            switchSideButton1.visibility = View.VISIBLE
        }
        if (sp.getBoolean("numberFieldSideRight", true)) {
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
        hymns = getHymnArrayList(mContext, sp, buchMode == BuchMode.Gesangbuch)
        hymnNrInput = sp.getString("nr", "-1").toString()
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
        refreshHandler.postDelayed(refreshRunnable, Constants.DELAY_BEFORE_PREVIEW)
    }

    private fun previewHymn(nr: String) {
        val hymnNr = validHymnr(buchMode == BuchMode.Gesangbuch, nr)
        if (hymnNr > 0) {
            tvHymnNrTitle.text = hymns[hymnNr - 1]["hymnNrAndTitle"]
            tvHymnText.text = hymns[hymnNr - 1]["hymnText"]
                ?.replace("</p><p>", "\n\n") ?: getText(R.string.notFound)
            sp.edit().putString("nr", nr).apply()
        } else {
            tvHymnNrTitle.text = ""
            tvHymnText.text = ""
            sp.edit().putString("nr", "").apply()
        }
    }

    private fun showHymn(nr: String) {
        val hymnNr = validHymnr(buchMode == BuchMode.Gesangbuch, nr)
        if (hymnNr > 0) {
            startActivity(
                Intent(mRootView.context, TextviewActivity::class.java).putExtra("nr", hymnNr)
            )
        }
    }

    private fun validHymnr(buchMode: Boolean, hymnNr: String): Int {
        if (hymnNr == "999") {
            discoverEasterEgg(mContext, konfettiView, R.string.easterEggEntry999)
        }
        if (hymnNr == "0" || hymnNr == "00" || hymnNr == "000") {
            discoverEasterEgg(mContext, konfettiView, R.string.easterEggEntry0)
        }
        val result: Int = AssetsHelper.validHymnr(buchMode, hymnNr)
        if (result < 0) hymnNrInput = ""
        return result
    }
}
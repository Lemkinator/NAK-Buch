package de.lemke.nakbuch.fragments

import de.lemke.nakbuch.utils.AssetsHelper.getHymnArrayList
import android.view.View
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import de.lemke.nakbuch.R
import java.lang.Runnable
import android.annotation.SuppressLint
import android.os.Handler
import com.google.android.material.button.MaterialButton
import android.os.Looper
import android.widget.*
import androidx.fragment.app.Fragment
import de.lemke.nakbuch.utils.AssetsHelper
import java.util.*

class MainActivityTabNum : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mActivity: AppCompatActivity
    private lateinit var mContext: Context
    //private lateinit var konfettiView: KonfettiView
    private lateinit var sp: SharedPreferences
    private var gesangbuchSelected = false
    private var inputOngoing = false
    private var hymnNrInput: String = ""
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
            getString(R.string.preference_file_default),
            Context.MODE_PRIVATE
        )
        gesangbuchSelected = sp.getBoolean("gesangbuchSelected", true)
        //konfettiView = mActivity.findViewById(R.id.konfettiViewTab0)
        tvHymnNrTitle = mActivity.findViewById(R.id.hymnTitlePreview)
        tvHymnText = mActivity.findViewById(R.id.hymnTextPreview)
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
        refreshHandler = Handler(Looper.getMainLooper())
        refreshRunnable = Runnable {
            inputOngoing = false
            previewHymn(hymnNrInput)
        }

        /*TipPopup b_z_tipPopup = new TipPopup(b_z, TipPopup.MODE_TRANSLUCENT);
        b_z_tipPopup.setMessage("Hier tippen um die zuletzt eingegebene Ziffer zu löschen.");
        b_z_tipPopup.setAction("Ok", new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                sp.edit().putBoolean("show_b_z_tip_popup", false).apply();
            }
        });
        TipPopup b_ok_tipPopup = new TipPopup(b_z, TipPopup.MODE_TRANSLUCENT);
        b_ok_tipPopup.setMessage("Hier tippen um die Lied-Ansicht für die eingegebene Nummer zu öffnen");
        b_ok_tipPopup.setAction("Ok", new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                sp.edit().putBoolean("show_b_ok_tip_popup", false).apply();
            }
        });
        if (sp.getBoolean("show_b_z_tip_popup", true)) {
            refreshHandler.postDelayed(() -> {
                b_z_tipPopup.show(TipPopup.DIRECTION_BOTTOM_LEFT);
            }, 500);
        }
        if (sp.getBoolean("show_b_ok_tip_popup", true)) {
            refreshHandler.postDelayed(() -> {
                b_ok_tipPopup.show(TipPopup.DIRECTION_BOTTOM_LEFT);
            }, 500);
        }*/initAssets()
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
        refreshHandler.postDelayed(refreshRunnable, DELAY_BEFORE_PREVIEW.toLong())
    }

    private fun previewHymn(nr: String) {
        val hymnNr = validHymnr(gesangbuchSelected, nr)
        if (hymnNr > 0) {
            tvHymnNrTitle.text = hymns[hymnNr - 1]["hymnNrAndTitle"]
            tvHymnText.text = hymns[hymnNr - 1]["hymnText"]
                ?.replace("</p><p>".toRegex(), "\n\n") ?: getText(R.string.notFound)
            sp.edit().putString("nr", nr).apply( )
        } else {
            tvHymnNrTitle.text = ""
            tvHymnText.text = ""
            sp.edit().putString("nr", "").apply()
        }
    }

    private fun showHymn(nr: String) {
        val hymnNr = validHymnr(gesangbuchSelected, nr)
        if (hymnNr > 0) {
            /* TODO startActivity(
                Intent(mRootView.context, TextviewActivity::class).putExtra("nr", hymnNr)
            )*/
        }
    }

    private fun validHymnr(buchMode: Boolean, hymnNr: String): Int {
        if (sp.getBoolean("easterEggs", false)) {
            if (hymnNr == "999") {
                val set: MutableSet<String> =
                    HashSet(sp.getStringSet("discoveredEasterEggs", HashSet())!!)
                if (!set.contains(getString(R.string.easterEggEntry999))) {
                    set.add(getString(R.string.easterEggEntry999))
                    sp.edit().putStringSet("discoveredEasterEggs", set).apply()
                    /*konfettiView.start(MainActivity.party1)
                    Handler().postDelayed(
                        { konfettiView.start(MainActivity.party2) },
                        MainActivity.partyDelay2.toLong()
                    )
                    Handler().postDelayed(
                        { konfettiView.start(MainActivity.party3) },
                        MainActivity.partyDelay3.toLong()
                    )*/
                    Toast.makeText(
                        mContext,
                        getString(R.string.easterEggDiscovered) + getString(R.string.easterEggEntry999),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (hymnNr == "0") {
                val set: MutableSet<String> =
                    HashSet(sp.getStringSet("discoveredEasterEggs", HashSet())!!)
                if (!set.contains(getString(R.string.easterEggEntry0))) {
                    set.add(getString(R.string.easterEggEntry0))
                    sp.edit().putStringSet("discoveredEasterEggs", set).apply()
                    /*konfettiView.start(MainActivity.party1)
                    Handler().postDelayed(
                        { konfettiView.start(MainActivity.party2) },
                        MainActivity.partyDelay2.toLong()
                    )
                    Handler().postDelayed(
                        { konfettiView.start(MainActivity.party3) },
                        MainActivity.partyDelay3.toLong()
                    )*/
                    Toast.makeText(
                        mContext,
                        getString(R.string.easterEggDiscovered) + getString(R.string.easterEggEntry0),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        val result: Int = AssetsHelper.validHymnr(buchMode, hymnNr)
        if (result < 0) hymnNrInput = ""
        return result
    }

    private fun initAssets() {
        hymns = getHymnArrayList(mContext, sp, gesangbuchSelected)
    }

    companion object {
        private const val DELAY_BEFORE_PREVIEW = 1500
    }
}
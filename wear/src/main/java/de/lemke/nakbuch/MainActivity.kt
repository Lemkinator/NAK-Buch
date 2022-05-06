package de.lemke.nakbuch

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.activity.ConfirmationActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import de.dlyt.yanndroid.oneui.utils.ThemeUtil
import de.lemke.nakbuch.utils.AssetsHelper
import java.io.ByteArrayInputStream
import java.io.ObjectInput
import java.io.ObjectInputStream

class MainActivity : AppCompatActivity() {
    private var gesangbuchSelected = false
    private var inputOngoing = false
    private lateinit var mContext: Context
    private lateinit var hymnNrInput: String
    private lateinit var tvHymnNrTitle: TextView
    //private lateinit var tvHymnNrTitle: CurvedTextView
    private lateinit var hymns: ArrayList<HashMap<String, String>>
    private lateinit var sp: SharedPreferences
    private var refreshHandler: Handler = Handler(Looper.getMainLooper())
    private var refreshRunnable: Runnable = Runnable {
        inputOngoing = false
        previewHymn(hymnNrInput)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtil(this)
        ThemeUtil.setDarkMode(this, ThemeUtil.DARK_MODE_ENABLED)
        super.onCreate(savedInstanceState)
        mContext = this
        sp = getSharedPreferences(getString(R.string.preference_file_default), MODE_PRIVATE)
        gesangbuchSelected = sp.getBoolean("gesangbuchSelected", true)
        setContentView(R.layout.activity_main)
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
        val buttonSwitchMode = findViewById<MaterialButton>(R.id.buttonSwitchMode)
        //buttonSwitchMode.setIcon(AppCompatResources.getDrawable(mContext, R.drawable.ic_samsung_convert));
        buttonSwitchMode.text = getString(if (gesangbuchSelected) R.string.title_Chorbuch else R.string.title_Gesangbuch)
        buttonSwitchMode.setOnClickListener {
            gesangbuchSelected = !gesangbuchSelected
            sp.edit().putBoolean("gesangbuchSelected", gesangbuchSelected).apply()
            buttonSwitchMode.text = getString(if (gesangbuchSelected) R.string.title_Chorbuch else R.string.title_Gesangbuch)
            startActivity(Intent(mContext, ConfirmationActivity::class.java)
                    .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                    .putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(if (gesangbuchSelected) R.string.title_Gesangbuch else R.string.title_Chorbuch))
            )
            initAssets()
            previewHymn(hymnNrInput)
        }
        findViewById<MaterialButton>(R.id.buttonInfo).setOnClickListener {
            startActivity(Intent(mContext, InfoActivity::class.java))
        }
        initAssets()
        hymnNrInput = sp.getString("nr", "")!!
        previewHymn(hymnNrInput)

        LocalBroadcastManager.getInstance(this).registerReceiver(Receiver(), IntentFilter(Intent.ACTION_SEND))

        //if (resources.configuration.isScreenRound) { }
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
        val hymnNr = AssetsHelper.validHymnr(gesangbuchSelected, nr)
        if (hymnNr > 0) {
            tvHymnNrTitle.text = hymns[hymnNr - 1]["hymnNrAndTitle"]
            sp.edit().putString("nr", nr).apply()
        } else {
            tvHymnNrTitle.text = ""
            sp.edit().putString("nr", "").apply()
        }
    }

    private fun showHymn(nr: String) {
        val hymnNr = AssetsHelper.validHymnr(gesangbuchSelected, nr)
        if (hymnNr > 0) {
            val myIntent = Intent(mContext, TextviewActivity::class.java)
            myIntent.putExtra("gesangbuchSelected", gesangbuchSelected)
            myIntent.putExtra("nr", hymnNr)
            myIntent.putExtra("nrAndTitle", hymns[hymnNr - 1]["hymnNrAndTitle"])
            myIntent.putExtra("text", hymns[hymnNr - 1]["hymnText"])
            myIntent.putExtra("copyright", hymns[hymnNr - 1]["hymnCopyright"])
            startActivity(myIntent)
        }
    }

    private fun initAssets() {
        hymns = AssetsHelper.getHymnArrayList(mContext, getString(if (gesangbuchSelected) R.string.filename_hymnsGesangbuch else R.string.filename_hymnsChorbuch), sp)
    }

    @Suppress("UNCHECKED_CAST")
    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { //Display the following when a new message is received//
            var bArray = intent.getByteArrayExtra("privateTextGesangbuch")
            var spKey = "privateTextGesangbuch"
            if (bArray == null) {
                bArray = intent.getByteArrayExtra("privateTextChorbuch")
                spKey = "privateTextChorbuch"
            }
            if (bArray == null) return
            val bis = ByteArrayInputStream(bArray)
            val `in`: ObjectInput
            val result: ArrayList<HashMap<String, String>>
            try {
                `in` = ObjectInputStream(bis)
                result = `in`.readObject() as ArrayList<HashMap<String, String>>
                sp.edit().putString(spKey, Gson().toJson(result)).apply()
                `in`.close()
                recreate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
package de.lemke.nakbuch

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TextviewActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var tvText: TextView
    private lateinit var tvCopyright: TextView
    private lateinit var spHymns: SharedPreferences
    companion object {
        private const val textSizeStep = 1
        private const val defaultTextSize = 12
        private const val textSizeMin = 8
        private const val textSizeMax = 25
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textview)
        spHymns = getSharedPreferences(getString(R.string.preference_file_hymns), MODE_PRIVATE)
        tvTitle = findViewById(R.id.tvTitle)
        tvText = findViewById(R.id.tvText)
        tvCopyright = findViewById(R.id.tvCopyright)
        findViewById<View>(R.id.buttonPlus).setOnClickListener { increaseTextSize() }
        findViewById<View>(R.id.buttonMinus).setOnClickListener { decreaseTextSize() }
        if (intent.getIntExtra("nr", -1) < 1) {
            Toast.makeText(this, "Invalid Number...", Toast.LENGTH_LONG).show()
            finish()
        }
        setTextSize(spHymns.getInt("textSize", defaultTextSize))
        tvText.text = intent.getStringExtra("text")!!.replace("</p><p>", "\n\n")
        tvTitle.text = intent.getStringExtra("nrAndTitle")
        tvCopyright.text = intent.getStringExtra("copyright")!!.replace("<br>", "\n")
    }

    private fun increaseTextSize() {
        val currentTextSize = spHymns.getInt("textSize", defaultTextSize)
        if (currentTextSize < textSizeMax) {
            spHymns.edit().putInt("textSize", currentTextSize + textSizeStep).apply()
            setTextSize(currentTextSize + textSizeStep)
        }
    }
    private fun decreaseTextSize() {
        val currentTextSize = spHymns.getInt("textSize", defaultTextSize)
        if (currentTextSize > textSizeMin) {
            spHymns.edit().putInt("textSize", currentTextSize - textSizeStep).apply()
            setTextSize(currentTextSize - textSizeStep)
        }
    }
    private fun setTextSize(textSize: Int) {
        tvText.textSize = textSize.toFloat()
        tvCopyright.textSize = (textSize - 3).toFloat()
        tvTitle.textSize = (textSize + 3).toFloat()
    }


}
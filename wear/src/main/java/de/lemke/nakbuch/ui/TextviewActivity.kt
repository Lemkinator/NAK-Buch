package de.lemke.nakbuch.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.DecreaseTextSizeUseCase
import de.lemke.nakbuch.domain.GetTextSizeUseCase
import de.lemke.nakbuch.domain.IncreaseTextSizeUseCase

class TextviewActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var tvText: TextView
    private lateinit var tvCopyright: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textview)
        tvTitle = findViewById(R.id.tvTitle)
        tvText = findViewById(R.id.tvText)
        tvCopyright = findViewById(R.id.tvCopyright)
        findViewById<View>(R.id.buttonPlus).setOnClickListener { setTextSize(IncreaseTextSizeUseCase()()) }
        findViewById<View>(R.id.buttonMinus).setOnClickListener { setTextSize(DecreaseTextSizeUseCase()()) }
        setTextSize(GetTextSizeUseCase()())
        tvText.text = intent.getStringExtra("text")!!
        tvTitle.text = intent.getStringExtra("nrAndTitle")
        tvCopyright.text = intent.getStringExtra("copyright")!!
    }

    private fun setTextSize(textSize: Int) {
        tvText.textSize = textSize.toFloat()
        tvCopyright.textSize = (textSize - 3).toFloat()
        tvTitle.textSize = (textSize + 3).toFloat()
    }

}
package de.lemke.nakbuch.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import de.lemke.nakbuch.domain.UpdateUserSettingsUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TextviewActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var tvText: TextView
    private lateinit var tvCopyright: TextView

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textview)
        tvTitle = findViewById(R.id.tvTitle)
        tvText = findViewById(R.id.tvText)
        tvCopyright = findViewById(R.id.tvCopyright)
        findViewById<View>(R.id.buttonPlus).setOnClickListener {
            lifecycleScope.launch {
                val newTextSize = getUserSettings().textSize + TEXTSIZE_STEP
                setTextSize(updateUserSettings { it.copy(textSize = newTextSize.coerceIn(TEXTSIZE_MIN, TEXTSIZE_MAX)) }.textSize)
            }
        }
        findViewById<View>(R.id.buttonMinus).setOnClickListener {
            lifecycleScope.launch {
                val newTextSize = getUserSettings().textSize - TEXTSIZE_STEP
                setTextSize(updateUserSettings { it.copy(textSize = newTextSize.coerceIn(TEXTSIZE_MIN, TEXTSIZE_MAX)) }.textSize)
            }
        }
        lifecycleScope.launch { setTextSize(getUserSettings().textSize) }
        tvText.text = intent.getStringExtra("text")!!
        tvTitle.text = intent.getStringExtra("nrAndTitle")
        tvCopyright.text = intent.getStringExtra("copyright")!!
    }

    private fun setTextSize(textSize: Int) {
        tvText.textSize = textSize.toFloat()
        tvCopyright.textSize = (textSize - 3).toFloat()
        tvTitle.textSize = (textSize + 3).toFloat()
    }

    companion object {
        private const val TEXTSIZE_STEP = 1
        private const val TEXTSIZE_MIN = 8
        private const val TEXTSIZE_MAX = 30
    }
}
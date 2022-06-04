package de.lemke.nakbuch.domain.utils

import android.text.Editable
import android.text.TextWatcher

abstract class TextChangedListener<T>(private val target: T) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    abstract fun onTextChanged(target: T, s: Editable)
    override fun afterTextChanged(s: Editable) {
        this.onTextChanged(target, s)
    }
}
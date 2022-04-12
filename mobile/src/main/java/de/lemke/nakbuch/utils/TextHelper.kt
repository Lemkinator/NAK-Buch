package de.lemke.nakbuch.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.google.android.material.color.MaterialColors
import de.lemke.nakbuch.R

object TextHelper {
    fun makeSectionOfTextBold(mContext: Context, sp: SharedPreferences, text: String, textToBold: String): SpannableStringBuilder {
        return makeSectionOfTextBold(mContext, sp, text, textToBold, -1)
    }
    fun makeSectionOfTextBold(mContext: Context, sp: SharedPreferences, text: String, textToBold: String, lengthBefore: Int): SpannableStringBuilder {
        if (textToBold.isNotEmpty()) {
            if (textToBold.startsWith("\"") && textToBold.endsWith("\"")) {
                if (textToBold.length > 2) {
                    val s = textToBold.substring(1, textToBold.length - 1)
                    return if (sp.getBoolean("searchAlternativeMode", false)) {
                        makeSectionOfTextBold(mContext, SpannableStringBuilder(text), hashSetOf(s), lengthBefore)
                    } else {
                        makeSectionOfTextBold(mContext, SpannableStringBuilder(text), HashSet(s.trim().split(" ")), lengthBefore)
                    }
                }
            } else {
                return if (sp.getBoolean("searchAlternativeMode", false)) {
                    makeSectionOfTextBold(mContext, SpannableStringBuilder(text), HashSet(textToBold.trim().split(" ")), lengthBefore)
                } else {
                    makeSectionOfTextBold(mContext, SpannableStringBuilder(text), hashSetOf(textToBold), lengthBefore)
                }
            }
        }
        return SpannableStringBuilder(text)
    }

    private fun makeSectionOfTextBold(mContext: Context, builder: SpannableStringBuilder, textToBold: String): SpannableStringBuilder {
        if (textToBold.isEmpty() || textToBold.trim() == "") {
            return builder
        }
        var testText = builder.toString()
        if (!testText.contains(textToBold, ignoreCase = true)) {
            return builder
        }
        var startingIndex = testText.indexOf(textToBold, ignoreCase = true)
        var endingIndex = startingIndex + textToBold.length
        var offset = 0 //for multiple replaces
        var firstSearchIndex = testText.length
        while (startingIndex >= 0) {
            builder.setSpan(StyleSpan(Typeface.BOLD_ITALIC), offset + startingIndex, offset + endingIndex, 0)
            builder.setSpan(ForegroundColorSpan(MaterialColors.getColor(mContext, de.dlyt.yanndroid.oneui.R.attr.colorPrimary, mContext.resources.getColor(R.color.primary_color, mContext.theme))),
                offset + startingIndex, offset + endingIndex, 0) //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            if (startingIndex < firstSearchIndex) firstSearchIndex = startingIndex
            testText = testText.substring(endingIndex)
            offset += endingIndex
            startingIndex = testText.indexOf(textToBold, ignoreCase = true)
            endingIndex = startingIndex + textToBold.length
        }
        return builder
    }

    private fun makeSectionOfTextBold(mContext: Context, spannableStringBuilder: SpannableStringBuilder, textsToBold: HashSet<String>, lengthBefore: Int): SpannableStringBuilder {
        var builder = spannableStringBuilder
        val text = builder.toString()
        var firstSearchIndex = text.length
        for (textItem in textsToBold) {
            if (text.contains(textItem, ignoreCase = true)) {
                firstSearchIndex = text.indexOf(textItem, ignoreCase = true)
                builder = makeSectionOfTextBold(mContext, builder, textItem)
            }
        }
        return if (firstSearchIndex != text.length && lengthBefore >= 0) builder.delete(0, 0.coerceAtLeast(firstSearchIndex - lengthBefore)) else builder
    }
}
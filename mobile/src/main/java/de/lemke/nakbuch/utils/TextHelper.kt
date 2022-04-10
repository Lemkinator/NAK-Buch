package de.lemke.nakbuch.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.google.android.material.color.MaterialColors
import de.lemke.nakbuch.R
import java.util.*

object TextHelper {
    fun makeSectionOfTextBold(mContext: Context, builder: SpannableStringBuilder, textToBold: String, lengthBefore: Int): SpannableStringBuilder {
        if (textToBold.isEmpty() || textToBold.trim { it <= ' ' } == "") {
            return builder
        }
        var testText = builder.toString().lowercase(Locale.getDefault())
        val testTextToBold = textToBold.lowercase(Locale.getDefault())
        if (!testText.contains(testTextToBold)) {
            return builder
        }
        var startingIndex = testText.indexOf(testTextToBold)
        var endingIndex = startingIndex + testTextToBold.length
        var offset = 0 //for multiple replaces
        var firstSearchIndex = testText.length
        while (startingIndex >= 0) {
            builder.setSpan(
                StyleSpan(Typeface.BOLD_ITALIC),
                offset + startingIndex,
                offset + endingIndex,
                0
            )
            builder.setSpan(ForegroundColorSpan(MaterialColors.getColor(mContext, de.dlyt.yanndroid.oneui.R.attr.colorPrimary, mContext.resources.getColor(R.color.primary_color, mContext.theme))),
                offset + startingIndex, offset + endingIndex, 0) //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            if (startingIndex < firstSearchIndex) firstSearchIndex = startingIndex
            testText = testText.substring(endingIndex)
            offset += endingIndex
            startingIndex = testText.indexOf(testTextToBold)
            endingIndex = startingIndex + testTextToBold.length
        }
        return if (lengthBefore == -1) builder else builder.delete(
            0,
            0.coerceAtLeast(firstSearchIndex - lengthBefore)
        )
    }

    fun makeSectionOfTextBold(mContext: Context, spannableStringBuilder: SpannableStringBuilder, textsToBold: HashSet<String>, lengthBefore: Int): SpannableStringBuilder {
        var builder = spannableStringBuilder
        val text = builder.toString().lowercase(Locale.getDefault())
        var firstSearchIndex = text.length
        for (textItem in textsToBold) {
            if (text.contains(textItem, ignoreCase = true)) {
                firstSearchIndex = text.indexOf(textItem)
                builder = makeSectionOfTextBold(mContext, builder, textItem, -1)
            }
        }
        return if (firstSearchIndex != text.length && lengthBefore >= 0) builder.delete(
            0,
            0.coerceAtLeast(firstSearchIndex - lengthBefore)
        ) else builder
    }
}
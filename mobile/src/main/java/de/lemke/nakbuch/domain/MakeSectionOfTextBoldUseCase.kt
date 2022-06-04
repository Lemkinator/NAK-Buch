package de.lemke.nakbuch.domain

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import javax.inject.Inject

class MakeSectionOfTextBoldUseCase @Inject constructor() {
    operator fun invoke(text: String, textToBold: String?, color: Int, alternativeSearchModeEnabled: Boolean): SpannableStringBuilder =
        invoke(text, textToBold?.trim(), color, -1, alternativeSearchModeEnabled)

    operator fun invoke(
        text: String,
        textToBold: String?,
        color: Int,
        lengthBefore: Int,
        alternativeSearchModeEnabled: Boolean
    ): SpannableStringBuilder {
        if (textToBold != null && textToBold.isNotEmpty()) {
            if (textToBold.trim().startsWith("\"") && textToBold.trim().endsWith("\"")) {
                if (textToBold.length > 2) {
                    val s = textToBold.substring(1, textToBold.length - 1)
                    return if (alternativeSearchModeEnabled)
                        makeSectionOfTextBold(SpannableStringBuilder(text), hashSetOf(s.trim()), color, lengthBefore)
                    else
                        makeSectionOfTextBold(SpannableStringBuilder(text), HashSet(s.trim().split(" ")), color, lengthBefore)
                }
            } else {
                return if (alternativeSearchModeEnabled)
                    makeSectionOfTextBold(SpannableStringBuilder(text), HashSet(textToBold.trim().split(" ")), color, lengthBefore)
                else
                    makeSectionOfTextBold(SpannableStringBuilder(text), hashSetOf(textToBold.trim()), color, lengthBefore)
            }
        }
        return SpannableStringBuilder(text)
    }

    private fun makeSectionOfTextBold(builder: SpannableStringBuilder, textToBold: String, color: Int): SpannableStringBuilder {
        var text = builder.toString()
        if (textToBold.isEmpty() || !text.contains(textToBold, ignoreCase = true)) return builder
        var startingIndex = text.indexOf(textToBold, ignoreCase = true)
        var endingIndex = startingIndex + textToBold.length
        var offset = 0 //for multiple replaces
        var firstSearchIndex = text.length
        while (startingIndex >= 0) {
            builder.setSpan(StyleSpan(Typeface.BOLD_ITALIC), offset + startingIndex, offset + endingIndex, 0)
            builder.setSpan(ForegroundColorSpan(color), offset + startingIndex, offset + endingIndex, 0)
            //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            if (startingIndex < firstSearchIndex) firstSearchIndex = startingIndex
            text = text.substring(endingIndex)
            offset += endingIndex
            startingIndex = text.indexOf(textToBold, ignoreCase = true)
            endingIndex = startingIndex + textToBold.length
        }
        return builder
    }

    private fun makeSectionOfTextBold(
        spannableStringBuilder: SpannableStringBuilder,
        textsToBold: HashSet<String>,
        color: Int,
        lengthBefore: Int
    ): SpannableStringBuilder {
        var builder = spannableStringBuilder
        val text = builder.toString()
        var firstSearchIndex = text.length
        for (textItem in textsToBold) {
            if (text.contains(textItem, ignoreCase = true)) {
                firstSearchIndex = text.indexOf(textItem, ignoreCase = true)
                builder = makeSectionOfTextBold(builder, textItem, color)
            }
        }
        return if (firstSearchIndex != text.length && lengthBefore >= 0)
            builder.delete(0, 0.coerceAtLeast(firstSearchIndex - lengthBefore))
        else builder
    }
}
package de.lemke.nakbuch.domain.settings

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

class MakeSectionOfTextBoldUseCase {

    suspend operator fun invoke(text: String, textToBold: String, color: Int): SpannableStringBuilder {
        return invoke(text, textToBold, color, -1)
    }

    suspend operator fun invoke(text: String, textToBold: String, color:Int, lengthBefore: Int): SpannableStringBuilder {
        if (textToBold.isNotEmpty()) {
            if (textToBold.startsWith("\"") && textToBold.endsWith("\"")) {
                if (textToBold.length > 2) {
                    val s = textToBold.substring(1, textToBold.length - 1)
                    return if (IsSearchModeAlternativeUseCase()()) {
                        makeSectionOfTextBold(SpannableStringBuilder(text), hashSetOf(s),color, lengthBefore)
                    } else {
                        makeSectionOfTextBold(SpannableStringBuilder(text), HashSet(s.trim().split(" ")),color, lengthBefore)
                    }
                }
            } else {
                return if (IsSearchModeAlternativeUseCase()()) {
                    makeSectionOfTextBold(SpannableStringBuilder(text), HashSet(textToBold.trim().split(" ")), color,  lengthBefore)
                } else {
                    makeSectionOfTextBold(SpannableStringBuilder(text), hashSetOf(textToBold), color,  lengthBefore)
                }
            }
        }
        return SpannableStringBuilder(text)
    }

    private fun makeSectionOfTextBold(builder: SpannableStringBuilder, textToBold: String, color: Int): SpannableStringBuilder {
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
            builder.setSpan(
                ForegroundColorSpan(color),
                offset + startingIndex, offset + endingIndex, 0) //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
            if (startingIndex < firstSearchIndex) firstSearchIndex = startingIndex
            testText = testText.substring(endingIndex)
            offset += endingIndex
            startingIndex = testText.indexOf(textToBold, ignoreCase = true)
            endingIndex = startingIndex + textToBold.length
        }
        return builder
    }

    private fun makeSectionOfTextBold(spannableStringBuilder: SpannableStringBuilder, textsToBold: HashSet<String>, color: Int, lengthBefore: Int): SpannableStringBuilder {
        var builder = spannableStringBuilder
        val text = builder.toString()
        var firstSearchIndex = text.length
        for (textItem in textsToBold) {
            if (text.contains(textItem, ignoreCase = true)) {
                firstSearchIndex = text.indexOf(textItem, ignoreCase = true)
                builder = makeSectionOfTextBold(builder, textItem, color)
            }
        }
        return if (firstSearchIndex != text.length && lengthBefore >= 0) builder.delete(0, 0.coerceAtLeast(firstSearchIndex - lengthBefore)) else builder
    }
}
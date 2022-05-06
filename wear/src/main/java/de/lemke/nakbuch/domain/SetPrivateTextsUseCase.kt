package de.lemke.nakbuch.domain

import android.content.Intent
import de.lemke.nakbuch.data.hymnsRepo

class SetPrivateTextsUseCase {
    operator fun invoke(intent: Intent): Boolean {
        var bArray = intent.getByteArrayExtra("privateTextGesangbuch")
        var spKey = "privateTextGesangbuch"
        if (bArray == null) {
            bArray = intent.getByteArrayExtra("privateTextChorbuch")
            spKey = "privateTextChorbuch"
        }
        if (bArray == null) return false
        return hymnsRepo.setPrivateTexts(spKey, bArray)
    }


}
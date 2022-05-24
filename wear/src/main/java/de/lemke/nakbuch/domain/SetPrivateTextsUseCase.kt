package de.lemke.nakbuch.domain

import android.content.Intent
import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetPrivateTextsUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository
) {
    suspend operator fun invoke(intent: Intent): Boolean {
        return withContext(Dispatchers.Default) {
            var buchMode = BuchMode.Gesangbuch
            var bArray = intent.getByteArrayExtra(buchMode.toString())
            if (bArray == null) {
                buchMode = BuchMode.Chorbuch
                bArray = intent.getByteArrayExtra(buchMode.toString())
            }
            if (bArray == null) {
                buchMode = BuchMode.Jugendliederbuch
                bArray = intent.getByteArrayExtra(buchMode.toString())
            }
            if (bArray == null) return@withContext false
            return@withContext hymnsRepository.setPrivateTexts(buchMode, bArray)
        }
    }
}
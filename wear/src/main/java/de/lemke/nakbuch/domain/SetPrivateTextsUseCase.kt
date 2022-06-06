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
    suspend operator fun invoke(intent: Intent): Boolean = withContext(Dispatchers.Default) {
        var byteArray: ByteArray?
        for (buchMode in BuchMode.values()) {
            byteArray = intent.getByteArrayExtra(buchMode.toString())
            if (byteArray != null) return@withContext hymnsRepository.setPrivateTexts(buchMode, byteArray)
        }
        return@withContext false
    }
}
package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetHintsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke(hints: Set<String>) = withContext(Dispatchers.Default) {
        userSettingsRepository.deleteHints()
        userSettingsRepository.setHints(hints)
    }
}
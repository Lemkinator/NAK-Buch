package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) {
    suspend operator fun invoke() = withContext(Dispatchers.Default) {
        return@withContext userSettingsRepository.getSettings()
    }
}
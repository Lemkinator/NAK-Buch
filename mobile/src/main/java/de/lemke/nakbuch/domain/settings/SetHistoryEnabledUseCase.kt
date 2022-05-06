package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetHistoryEnabledUseCase {
    operator fun invoke(enabled: Boolean) = settingsRepo.setHistoryEnabled(enabled)
}
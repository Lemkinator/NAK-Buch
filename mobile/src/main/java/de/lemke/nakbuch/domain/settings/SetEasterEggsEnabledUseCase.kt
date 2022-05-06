package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetEasterEggsEnabledUseCase {
    operator fun invoke(enabled:Boolean) = settingsRepo.setEasterEggsEnabled(enabled)
}
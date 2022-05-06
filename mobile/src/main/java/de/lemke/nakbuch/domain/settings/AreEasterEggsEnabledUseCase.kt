package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class AreEasterEggsEnabledUseCase {
    operator fun invoke(): Boolean = settingsRepo.areEasterEggsEnabled()
}
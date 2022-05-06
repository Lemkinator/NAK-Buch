package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class ResetEasterEggsUseCase {
    operator fun invoke() = settingsRepo.resetEasterEggs()
}
package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetDiscoveredEasterEggsUseCase {
    operator fun invoke() = settingsRepo.getDiscoveredEasterEggs()
}
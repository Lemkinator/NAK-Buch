package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class IsSearchModeAlternativeUseCase {
    operator fun invoke(): Boolean = settingsRepo.isSearchModeAlternative()
}
package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetSearchModeAlternativeUseCase {
    operator fun invoke(alternative: Boolean) = settingsRepo.setSearchModeAlternative(alternative)
}
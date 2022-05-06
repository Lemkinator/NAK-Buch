package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetBuchModeUseCase {
    operator fun invoke() = settingsRepo.getBuchMode()
}
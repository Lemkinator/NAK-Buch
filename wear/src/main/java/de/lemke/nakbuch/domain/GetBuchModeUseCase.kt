package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.settingsRepo

class GetBuchModeUseCase {
    operator fun invoke() = settingsRepo.getBuchMode()
}
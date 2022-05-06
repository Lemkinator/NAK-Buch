package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.settingsRepo
import de.lemke.nakbuch.domain.model.BuchMode

class SetBuchModeUseCase {
    operator fun invoke(buchMode: BuchMode) = settingsRepo.setBuchMode(buchMode)
}
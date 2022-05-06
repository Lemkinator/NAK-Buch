package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class IsSungOnVisibleUseCase {
    operator fun invoke(): Boolean = settingsRepo.isSungOnVisible()
}
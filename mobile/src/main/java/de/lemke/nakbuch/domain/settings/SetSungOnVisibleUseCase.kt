package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetSungOnVisibleUseCase {
    operator fun invoke(visible: Boolean) = settingsRepo.setSungOnVisible(visible)
}
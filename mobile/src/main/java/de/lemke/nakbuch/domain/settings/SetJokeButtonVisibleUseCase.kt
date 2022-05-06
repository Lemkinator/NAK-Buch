package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetJokeButtonVisibleUseCase {
    operator fun invoke(visible: Boolean) = settingsRepo.setJokeButtonVisible(visible)
}
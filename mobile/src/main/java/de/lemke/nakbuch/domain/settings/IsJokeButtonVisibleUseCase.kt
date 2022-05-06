package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class IsJokeButtonVisibleUseCase {
    operator fun invoke(): Boolean = settingsRepo.isJokeButtonVisible()
}
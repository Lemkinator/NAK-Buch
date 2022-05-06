package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.settingsRepo

class IncreaseTextSizeUseCase {
    operator fun invoke() = settingsRepo.increaseTextSize()
}
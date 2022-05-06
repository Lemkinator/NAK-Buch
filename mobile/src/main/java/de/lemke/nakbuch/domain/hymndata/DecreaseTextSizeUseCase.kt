package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.settingsRepo

class DecreaseTextSizeUseCase {
    operator fun invoke() = settingsRepo.decreaseTextSize()
}
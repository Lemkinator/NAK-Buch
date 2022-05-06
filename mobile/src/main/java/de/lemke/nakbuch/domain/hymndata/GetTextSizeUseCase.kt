package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.settingsRepo

class GetTextSizeUseCase {
    operator fun invoke() = settingsRepo.getTextSize()
}
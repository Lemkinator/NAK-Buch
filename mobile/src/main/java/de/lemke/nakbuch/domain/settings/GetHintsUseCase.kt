package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetHintsUseCase {
    operator fun invoke(): HashSet<String> = settingsRepo.getHints()
}
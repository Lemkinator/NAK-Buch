package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetHintsUseCase {
    operator fun invoke(hintsSet: HashSet<String>) = settingsRepo.setHints(hintsSet)
}
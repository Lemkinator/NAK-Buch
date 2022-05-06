package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class AreNotesVisibleUseCase {
    operator fun invoke(): Boolean = settingsRepo.areNotesVisible()
}
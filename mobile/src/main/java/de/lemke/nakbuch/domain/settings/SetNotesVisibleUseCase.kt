package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetNotesVisibleUseCase {
    operator fun invoke(visible: Boolean) = settingsRepo.setNotesVisible(visible)
}
package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class IsHistroyEnabledUseCase {
    operator fun invoke(): Boolean = settingsRepo.isHistoryEnabled()
}
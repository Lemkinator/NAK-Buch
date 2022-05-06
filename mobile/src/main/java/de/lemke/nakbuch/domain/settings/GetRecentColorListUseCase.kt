package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetRecentColorListUseCase {
    operator fun invoke(): ArrayList<Int> = settingsRepo.getRecentColorList()
}
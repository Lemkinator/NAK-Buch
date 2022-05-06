package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetSearchUseCase {
    operator fun invoke(): String = settingsRepo.getSearch()
}
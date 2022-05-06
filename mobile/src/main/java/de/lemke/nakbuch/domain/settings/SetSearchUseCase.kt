package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetSearchUseCase {
    operator fun invoke(search: String) = settingsRepo.setSearch(search)
}
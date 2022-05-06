package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.settingsRepo

class GetNumberUseCase {
    operator fun invoke(): String = settingsRepo.getNumber()
}
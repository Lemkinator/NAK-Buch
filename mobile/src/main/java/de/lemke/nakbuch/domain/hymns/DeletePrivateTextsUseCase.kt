package de.lemke.nakbuch.domain.hymns

import de.lemke.nakbuch.data.hymnsRepo

class DeletePrivateTextsUseCase {
    suspend operator fun invoke() = hymnsRepo.deletePrivateTexts()
}
package de.lemke.nakbuch.domain.hymndataUseCases

import de.lemke.nakbuch.data.HymnDataRepository
import de.lemke.nakbuch.domain.model.PersonalHymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetPersonalHymnUseCase @Inject constructor(
    private val hymnDataRepository: HymnDataRepository,
) {
    suspend operator fun invoke(personalHymn: PersonalHymn) = withContext(Dispatchers.Default) {
        hymnDataRepository.setPersonalHymn(personalHymn)
    }
}
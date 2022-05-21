package de.lemke.nakbuch.domain.hymndataUseCases

import de.lemke.nakbuch.domain.GetAllPersonalHymnsUseCase
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.PersonalHymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFavoriteHymnsUseCase @Inject constructor(
    private val getAllPersonalHymns: GetAllPersonalHymnsUseCase
) {
    suspend operator fun invoke(buchMode: BuchMode): List<PersonalHymn> =
        withContext(Dispatchers.Default) {
            return@withContext getAllPersonalHymns(buchMode).filter { it.favorite }
        }

}
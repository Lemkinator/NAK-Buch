package de.lemke.nakbuch.domain

import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.BuchMode
import javax.inject.Inject

class GetHymnCountUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
) {
    operator fun invoke(buchMode: BuchMode): Int = hymnsRepository.hymnCount(buchMode)
}
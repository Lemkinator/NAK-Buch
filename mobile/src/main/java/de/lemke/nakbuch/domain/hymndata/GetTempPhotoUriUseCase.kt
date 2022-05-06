package de.lemke.nakbuch.domain.hymndata

import de.lemke.nakbuch.data.hymnDataRepo

class GetTempPhotoUriUseCase {
    operator fun invoke() = hymnDataRepo.getTempPhotoUri()
}
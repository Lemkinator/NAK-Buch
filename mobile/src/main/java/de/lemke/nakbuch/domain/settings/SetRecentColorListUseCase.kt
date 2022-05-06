package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetRecentColorListUseCase {
    operator fun invoke(colorList: ArrayList<Int>) = settingsRepo.setRecentColorList(colorList)
}
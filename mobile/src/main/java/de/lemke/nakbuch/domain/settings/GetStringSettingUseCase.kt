package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetStringSettingUseCase {
    operator fun invoke(tipsName: String, default: String): String = settingsRepo.getStringSetting(tipsName, default)
}
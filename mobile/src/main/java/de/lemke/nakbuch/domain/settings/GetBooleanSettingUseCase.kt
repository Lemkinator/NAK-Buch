package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class GetBooleanSettingUseCase {
    operator fun invoke(tipsName: String, default: Boolean): Boolean = settingsRepo.getBooleanSetting(tipsName, default)
}
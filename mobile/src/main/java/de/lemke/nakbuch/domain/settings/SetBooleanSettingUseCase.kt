package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetBooleanSettingUseCase {
    operator fun invoke(name: String, setting: Boolean) = settingsRepo.setBooleanSetting(name, setting)
}
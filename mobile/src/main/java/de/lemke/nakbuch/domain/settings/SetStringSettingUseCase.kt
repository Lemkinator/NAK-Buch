package de.lemke.nakbuch.domain.settings

import de.lemke.nakbuch.data.settingsRepo

class SetStringSettingUseCase {
    operator fun invoke(name: String, setting: String) = settingsRepo.setStringSetting(name, setting)
}
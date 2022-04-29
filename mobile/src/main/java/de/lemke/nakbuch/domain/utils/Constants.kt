package de.lemke.nakbuch.domain.utils

import de.lemke.nakbuch.domain.model.BuchMode


class Constants {
    companion object {
        const val GESANGBUCHMODE = true
        const val CHORBUCHMODE = false
        const val HYMNSGESANGBUCHCOUNT = 438
        const val HYMNSCHORBUCHCOUNT = 462
        const val HISTORYSIZE = 200
        const val DELAY_BEFORE_PREVIEW = 1500L
        const val MAX_IMAGES_PER_HYMN = 20

        var colorSettingChanged = false
        var modeChanged = false

        @JvmStatic
        fun hymnCount(buchMode: BuchMode) = if (buchMode == BuchMode.Gesangbuch) HYMNSGESANGBUCHCOUNT else HYMNSCHORBUCHCOUNT
    }
}
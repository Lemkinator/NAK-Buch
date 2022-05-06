package de.lemke.nakbuch.domain.settings

import android.util.Log
import de.lemke.nakbuch.BuildConfig
import de.lemke.nakbuch.data.settingsRepo

class CheckAppStartUseCase {
    enum class AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL, OLD_ARCHITECTURE
    }

    operator fun invoke(): AppStart {
        val lastVersionName: String = settingsRepo.getLastAppVersionName()
        val lastVersionCode: Int = settingsRepo.getLastAppVersionCode()
        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName: String = BuildConfig.VERSION_NAME
        settingsRepo.setAppVersionCode(versionCode)
        settingsRepo.setAppVersionName(versionName)
        Log.d("CheckAppStart", "Current version code: $versionCode , last version code: $lastVersionCode")
        Log.d("CheckAppStart", "Current version name: $versionName , last version name: $lastVersionName")
        return when {
            lastVersionCode == -1 -> {
                AppStart.FIRST_TIME
            }
            lastVersionCode <= 60 -> {
                AppStart.OLD_ARCHITECTURE
            }
            lastVersionCode < versionCode -> {
                AppStart.FIRST_TIME_VERSION
            }
            lastVersionCode > versionCode -> {
                Log.w(
                    "checkAppStart",
                    "Current version code ($versionCode) is less then the one recognized on last startup ($lastVersionCode). Defensively assuming normal app start."
                )
                AppStart.NORMAL
            }
            else -> {
                AppStart.NORMAL
            }
        }
    }
}
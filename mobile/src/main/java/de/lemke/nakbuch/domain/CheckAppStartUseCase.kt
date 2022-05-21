package de.lemke.nakbuch.domain

import android.util.Log
import de.lemke.nakbuch.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckAppStartUseCase @Inject constructor(
    private val getUserSettings: GetUserSettingsUseCase,
    private val updateUserSettings: UpdateUserSettingsUseCase,
) {
    suspend operator fun invoke(): AppStart =
        withContext(Dispatchers.Default) {
            val lastVersionName: String = getUserSettings().lastVersionName
            val lastVersionCode: Int = getUserSettings().lastVersionCode
            val versionCode: Int = BuildConfig.VERSION_CODE
            val versionName: String = BuildConfig.VERSION_NAME
            updateUserSettings { it.copy(lastVersionCode = versionCode, lastVersionName = versionName) }
            Log.d("CheckAppStart", "Current version code: $versionCode , last version code: $lastVersionCode")
            Log.d("CheckAppStart", "Current version name: $versionName , last version name: $lastVersionName")
            return@withContext when {
                lastVersionCode == -1 -> AppStart.FIRST_TIME
                lastVersionCode <= 60 -> AppStart.OLD_ARCHITECTURE
                lastVersionCode < versionCode -> AppStart.FIRST_TIME_VERSION
                lastVersionCode > versionCode -> {
                    Log.w(
                        "checkAppStart",
                        "Current version code ($versionCode) is less then the one recognized on last startup ($lastVersionCode). Defensively assuming normal app start."
                    )
                    AppStart.NORMAL
                }
                else -> AppStart.NORMAL
            }
        }

}

enum class AppStart {
    FIRST_TIME, FIRST_TIME_VERSION, NORMAL, OLD_ARCHITECTURE
}

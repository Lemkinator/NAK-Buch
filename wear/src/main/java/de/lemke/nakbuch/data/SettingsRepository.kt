package de.lemke.nakbuch.data

import android.content.SharedPreferences
import android.content.res.Resources
import de.lemke.nakbuch.App
import de.lemke.nakbuch.domain.model.BuchMode
import kotlin.math.max
import kotlin.math.min

val settingsRepo = SettingsRepository(App.defaultSharedPreferences, App.myResources)

class SettingsRepository(private val sp: SharedPreferences, private val resources: Resources) {
    companion object {
        private const val TEXT_SIZE_STEP = 1
        private const val DEFAULT_TEXT_SIZE = 12
        private const val TEXT_SIZE_MIN = 8
        private const val TEXT_SIZE_MAX = 30
    }

    fun getBuchMode(): BuchMode = if (sp.getBoolean("gesangbuchSelected", true)) BuchMode.Gesangbuch else BuchMode.Chorbuch
    fun setBuchMode(buchMode: BuchMode) = sp.edit().putBoolean("gesangbuchSelected", buchMode == BuchMode.Gesangbuch).apply()

    fun getLastAppVersionCode(): Int = sp.getInt("lastAppVersionCode", -1)
    fun getLastAppVersionName(): String = sp.getString("lastAppVersionName", "undefined")!!

    fun setAppVersionCode(versionCode: Int) = sp.edit().putInt("lastAppVersionCode", versionCode).commit()
    fun setAppVersionName(versionName: String) = sp.edit().putString("lastAppVersionName", versionName).commit()

    fun getBooleanSetting(name: String, default: Boolean): Boolean = sp.getBoolean(name, default)
    fun setBooleanSetting(name: String, setting: Boolean) = sp.edit().putBoolean(name, setting).apply()

    fun getStringSetting(name: String, default: String): String = sp.getString(name, default)!!
    fun setStringSetting(name: String, setting: String) = sp.edit().putString(name, setting).apply()

    fun getNumber(): String = sp.getString("nr", "")!!
    fun setNumber(number: String) = sp.edit().putString("nr", number).apply()

    fun getTextSize() = sp.getInt("textSize", DEFAULT_TEXT_SIZE)
    fun increaseTextSize(): Int {
        val textSize = min(getTextSize() + TEXT_SIZE_STEP, TEXT_SIZE_MAX)
        sp.edit().putInt("textSize", textSize).apply()
        return textSize
    }
    fun decreaseTextSize(): Int {
        val textSize = max(getTextSize() - TEXT_SIZE_STEP, TEXT_SIZE_MIN)
        sp.edit().putInt("textSize", textSize).apply()
        return textSize
    }
}


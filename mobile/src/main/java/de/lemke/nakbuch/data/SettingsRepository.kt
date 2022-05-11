package de.lemke.nakbuch.data

import android.content.SharedPreferences
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.App
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import kotlin.math.max
import kotlin.math.min

val settingsRepo = SettingsRepository(App.defaultSharedPreferences, App.myResources)

class SettingsRepository(private val sp: SharedPreferences, private val resources: Resources) {
    companion object {
        const val TEXT_SIZE_STEP = 2
        const val DEFAULT_TEXT_SIZE = 20
        const val TEXT_SIZE_MIN = 10
        const val TEXT_SIZE_MAX = 50
    }

    fun getBuchMode(): BuchMode = if (sp.getBoolean("gesangbuchSelected", true)) BuchMode.Gesangbuch else BuchMode.Chorbuch
    fun setBuchMode(buchMode: BuchMode) = sp.edit().putBoolean("gesangbuchSelected", buchMode == BuchMode.Gesangbuch).apply()


    fun getLastAppVersionCode(): Int = sp.getInt("lastAppVersionCode", -1)
    fun getLastAppVersionName(): String = sp.getString("lastAppVersionName", "undefined")!!

    fun setAppVersionCode(versionCode: Int) = sp.edit().putInt("lastAppVersionCode", versionCode).commit()
    fun setAppVersionName(versionName: String) = sp.edit().putString("lastAppVersionName", versionName).commit()

    fun areEasterEggsEnabled(): Boolean = sp.getBoolean("easterEggs", true)
    fun setEasterEggsEnabled(enabled: Boolean) = sp.edit().putBoolean("easterEggs", enabled).apply()

    fun getDiscoveredEasterEggs() = ArrayList(sp.getStringSet("discoveredEasterEggs", HashSet())!!)

    fun resetEasterEggs() = sp.edit().putStringSet("discoveredEasterEggs", HashSet()).apply()

    fun discoverEasterEgg(easterEggEntryName: String): Boolean {
        val s: MutableSet<String> = HashSet(sp.getStringSet("discoveredEasterEggs", HashSet())!!)
        if (!s.contains(easterEggEntryName)) {
            s.add(easterEggEntryName)
            sp.edit().putStringSet("discoveredEasterEggs", s).apply()
            return true
        }
        return false
    }

    fun isHistoryEnabled(): Boolean = sp.getBoolean("historyEnabled", true)
    fun setHistoryEnabled(enabled: Boolean) = sp.edit().putBoolean("historyEnabled", enabled).apply()

    fun isSearchModeAlternative(): Boolean = sp.getBoolean("searchAlternativeMode", false)
    fun setSearchModeAlternative(alternative: Boolean) = sp.edit().putBoolean("searchAlternativeMode", alternative).apply()

    fun isJokeButtonVisible(): Boolean = sp.getBoolean("showJokeButton", true)
    fun setJokeButtonVisible(visible: Boolean) = sp.edit().putBoolean("showJokeButton", visible).apply()

    fun areNotesVisible(): Boolean = sp.getBoolean("noteVisible", false)
    fun setNotesVisible(visible: Boolean) = sp.edit().putBoolean("noteVisible", visible).apply()

    fun isSungOnVisible(): Boolean = sp.getBoolean("sungOnVisible", false)
    fun setSungOnVisible(visible: Boolean) = sp.edit().putBoolean("sungOnVisible", visible).apply()

    fun getBooleanSetting(name: String, default: Boolean): Boolean = sp.getBoolean(name, default)
    fun setBooleanSetting(name: String, setting: Boolean) = sp.edit().putBoolean(name, setting).apply()

    fun getStringSetting(name: String, default: String): String = sp.getString(name, default)!!
    fun setStringSetting(name: String, setting: String) = sp.edit().putString(name, setting).apply()

    fun getHints(): HashSet<String> = HashSet(sp.getStringSet("hints", mutableSetOf(*resources.getStringArray(R.array.hint_values)))!!)
    fun setHints(hintsSet: MutableSet<String>) = sp.edit().putStringSet("hints", hintsSet).apply()

    fun getSearch(): String = sp.getString("search", "")!!
    fun setSearch(search: String) = sp.edit().putString("search", search).apply()

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

    fun getRecentColorList(): ArrayList<Int> = Gson().fromJson(
        sp.getString(
            "recent_colors",
            Gson().toJson(intArrayOf(resources.getColor(R.color.primary_color)))
        ),
        object : TypeToken<ArrayList<Int?>?>() {}.type
    )

    fun setRecentColorList(recentColors: ArrayList<Int>) = sp.edit().putString("recent_colors", Gson().toJson(recentColors)).apply()

}


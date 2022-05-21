package de.lemke.nakbuch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Provides CRUD operations for user settings. */
class UserSettingsRepository@Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {


    /** Returns the current user settings. */
    suspend fun getSettings(): UserSettings = dataStore.data.map(::settingsFromPreferences).first()

    /**
     * Updates the current user settings and returns the new settings.
     * @param f Invoked with the current settings; The settings returned from this function will replace the current ones.
     */
    suspend fun updateSettings(f: (UserSettings) -> UserSettings): UserSettings {
        val prefs = dataStore.edit {
            val newSettings = f(settingsFromPreferences(it))
            it[KEY_BUCH_MODE] = newSettings.buchMode.toInt()
            it[KEY_NUMBER] = newSettings.number
            it[KEY_SEARCH] = newSettings.search
            it[KEY_LAST_VERSION_CODE] = newSettings.lastVersionCode
            it[KEY_LAST_VERSION_NAME] = newSettings.lastVersionName
            it[KEY_TEXTSIZE] = newSettings.textSize
            it[KEY_EASTER_EGGS_ENABLED] = newSettings.easterEggsEnabled
            it[KEY_EASTEREGG_TIPS_USED] = newSettings.easterEggTipsUsed
            it[KEY_HISTORY_ENABLED] = newSettings.historyEnabled
            it[KEY_ALTERNATIVE_SEARCHMODE_ENABLED] = newSettings.alternativeSearchModeEnabled
            it[KEY_JOKE_BUTTON_VISIBLE] = newSettings.jokeButtonVisible
            it[KEY_NOTES_VISIBLE] = newSettings.notesVisible
            it[KEY_SUNG_ON_VISIBLE] = newSettings.sungOnVisible
            it[KEY_RECENT_COLOR_LIST] = Gson().toJson(newSettings.recentColorList)
            it[KEY_HINT_SET] = Gson().toJson(newSettings.hintSet)
            it[KEY_DISCOVERED_EASTEREGGS] = Gson().toJson(newSettings.discoveredEasterEggs)
            it[KEY_SHOW_MAIN_TIPS] = newSettings.showMainTips
            it[KEY_SHOW_TEXTVIEW_TIPS] = newSettings.showTextViewTips
            it[KEY_SHOW_IMAGEVIEW_TIPS] = newSettings.showImageViewTips
            it[KEY_SHOW_APP_DISCLAIMER] = newSettings.showAppDisclaimer
            it[KEY_SHOW_EASTEREGG_HINT] = newSettings.showEasterEggHint
            it[KEY_NUMBERFIELD_RIGHT_SIDE] = newSettings.numberFieldRightSide
            it[KEY_PHOTO_QUALITY] = newSettings.photoQuality.value
            it[KEY_PHOTO_RESOLUTION] = newSettings.photoResolution.value
            it[KEY_CONFIRM_EXIT] = newSettings.confirmExit
        }
        return settingsFromPreferences(prefs)
    }

    private fun settingsFromPreferences(prefs: Preferences) = UserSettings(
        number = prefs[KEY_NUMBER] ?: "",
        buchMode = BuchMode.fromInt(prefs[KEY_BUCH_MODE] ?: BuchMode.Gesangbuch.toInt()),
        search = prefs[KEY_SEARCH] ?: "",
        lastVersionCode = prefs[KEY_LAST_VERSION_CODE] ?: -1,
        lastVersionName = prefs[KEY_LAST_VERSION_NAME] ?: "0.0",
        textSize = prefs[KEY_TEXTSIZE] ?: DEFAULT_TEXTSIZE,
        easterEggsEnabled = prefs[KEY_EASTER_EGGS_ENABLED] ?: true,
        easterEggTipsUsed = prefs[KEY_EASTEREGG_TIPS_USED] ?: false,
        historyEnabled = prefs[KEY_HISTORY_ENABLED] ?: true,
        alternativeSearchModeEnabled = prefs[KEY_ALTERNATIVE_SEARCHMODE_ENABLED] ?: false,
        jokeButtonVisible = prefs[KEY_JOKE_BUTTON_VISIBLE] ?: true,
        notesVisible = prefs[KEY_NOTES_VISIBLE] ?: false,
        sungOnVisible = prefs[KEY_SUNG_ON_VISIBLE] ?: false,
        recentColorList = Gson().fromJson(prefs[KEY_RECENT_COLOR_LIST], object : TypeToken<MutableList<Int>?>() {}.type)
            ?: mutableListOf(context.resources.getColor(R.color.primary_color, context.theme)),
        hintSet = Gson().fromJson(prefs[KEY_HINT_SET], object : TypeToken<MutableSet<String>?>() {}.type)
            ?: (context.resources.getStringArray(R.array.hint_values)).toMutableSet(),
        discoveredEasterEggs = Gson().fromJson(prefs[KEY_DISCOVERED_EASTEREGGS], object : TypeToken<MutableSet<String>?>() {}.type)
            ?: mutableSetOf(),
        showMainTips = prefs[KEY_SHOW_MAIN_TIPS] ?: true,
        showTextViewTips = prefs[KEY_SHOW_TEXTVIEW_TIPS] ?: true,
        showImageViewTips = prefs[KEY_SHOW_IMAGEVIEW_TIPS] ?: true,
        showAppDisclaimer = prefs[KEY_SHOW_APP_DISCLAIMER] ?: true,
        showEasterEggHint = prefs[KEY_SHOW_EASTEREGG_HINT] ?: true,
        numberFieldRightSide = prefs[KEY_NUMBERFIELD_RIGHT_SIDE] ?: true,
        photoQuality = Quality.fromInt(prefs[KEY_PHOTO_QUALITY]),
        photoResolution = Resolution.fromInt(prefs[KEY_PHOTO_RESOLUTION]),
        confirmExit = prefs[KEY_CONFIRM_EXIT] ?: true,
    )

    private companion object {
        private val KEY_BUCH_MODE = intPreferencesKey("buchMode")
        private val KEY_NUMBER = stringPreferencesKey("number")
        private val KEY_SEARCH = stringPreferencesKey("search")
        private val KEY_LAST_VERSION_CODE = intPreferencesKey("lastVersionCode")
        private val KEY_LAST_VERSION_NAME = stringPreferencesKey("lastVersionName")
        private val KEY_TEXTSIZE = intPreferencesKey("textsize")
        private const val DEFAULT_TEXTSIZE = 20
        private val KEY_EASTER_EGGS_ENABLED = booleanPreferencesKey("easterEggsEnabled")
        private val KEY_EASTEREGG_TIPS_USED = booleanPreferencesKey("easterEggTipsUsed")
        private val KEY_HISTORY_ENABLED = booleanPreferencesKey("historyEnabled")
        private val KEY_ALTERNATIVE_SEARCHMODE_ENABLED = booleanPreferencesKey("alternativeSearchModeEnabled")
        private val KEY_JOKE_BUTTON_VISIBLE = booleanPreferencesKey("jokeButtonVisible")
        private val KEY_NOTES_VISIBLE = booleanPreferencesKey("noteVisible")
        private val KEY_SUNG_ON_VISIBLE = booleanPreferencesKey("sungOnVisible")
        private val KEY_RECENT_COLOR_LIST = stringPreferencesKey("recentColorList")
        private val KEY_HINT_SET = stringPreferencesKey("hintSet")
        private val KEY_DISCOVERED_EASTEREGGS = stringPreferencesKey("discoveredEasterEggs")
        private val KEY_SHOW_MAIN_TIPS = booleanPreferencesKey("showMainTips")
        private val KEY_SHOW_TEXTVIEW_TIPS = booleanPreferencesKey("showTextViewTips")
        private val KEY_SHOW_IMAGEVIEW_TIPS = booleanPreferencesKey("showImageViewTips")
        private val KEY_SHOW_APP_DISCLAIMER = booleanPreferencesKey("showAppDisclaimer")
        private val KEY_SHOW_EASTEREGG_HINT = booleanPreferencesKey("showEasterEggHint")
        private val KEY_NUMBERFIELD_RIGHT_SIDE = booleanPreferencesKey("numberFieldRightSide")
        private val KEY_PHOTO_QUALITY = intPreferencesKey("photoQuality")
        private val KEY_PHOTO_RESOLUTION = intPreferencesKey("photoResolution")
        private val KEY_CONFIRM_EXIT = booleanPreferencesKey("confirmExit")
    }
}

/** Settings associated with the current user. */
data class UserSettings(
    /** The currently active BuchMode */
    val buchMode: BuchMode,
    /** Currently selected Number as String */
    val number: String,
    /** Current Search  */
    val search: String,
    /** Last App-Version-Code */
    val lastVersionCode: Int,
    /** Last App-Version-Name */
    val lastVersionName: String,
    /** Text Size */
    val textSize: Int,
    /** Easter Eggs enabled */
    val easterEggsEnabled: Boolean,
    /** true if EasterEgg-Tips were shown */
    val easterEggTipsUsed: Boolean,
    /** History enabled */
    val historyEnabled: Boolean,
    /** Is alternative Search-Mode enabled */
    val alternativeSearchModeEnabled: Boolean,
    /** Is Joke-Button visible*/
    val jokeButtonVisible: Boolean,
    /** Are Notes visible*/
    val notesVisible: Boolean,
    /** Is Sung-On visible*/
    val sungOnVisible: Boolean,
    /** Recent ColorList*/
    val recentColorList: MutableList<Int>,
    /** Set with Hints to show*/
    val hintSet: MutableSet<String>,
    /** Set with discovered EasterEggs*/
    val discoveredEasterEggs: MutableSet<String>,
    /** show MainView Tips*/
    val showMainTips: Boolean,
    /** show TextView Tips*/
    val showTextViewTips: Boolean,
    /** show ImageView Tips*/
    val showImageViewTips: Boolean,
    /** show AppDisclaimer Tips*/
    val showAppDisclaimer: Boolean,
    /** show EasterEggHint Tips*/
    val showEasterEggHint: Boolean,
    /** True if numberfield is on right side*/
    val numberFieldRightSide: Boolean,
    /** Photo Quality*/
    val photoQuality: Quality,
    /** Photo Resolution*/
    val photoResolution: Resolution,
    /** confirm Exit*/
    val confirmExit: Boolean,
)

enum class Resolution(val value: Int) {
    VERY_LOW(512),
    LOW(1024),
    MEDIUM(2048),
    HIGH(4096),
    VERY_HIGH(8192);

    companion object {
        fun fromInt(value: Int?): Resolution = when (value) {
            512 -> VERY_LOW
            1024 -> LOW
            2048 -> MEDIUM
            4096 -> HIGH
            8192 -> VERY_HIGH
            else -> MEDIUM
        }
    }
}

enum class Quality(val value: Int) {
    VERY_LOW(15),
    LOW(25),
    MEDIUM(50),
    HIGH(75),
    VERY_HIGH(100);

    companion object {
        fun fromInt(value: Int?): Quality = when (value) {
            15 -> VERY_LOW
            25 -> LOW
            50 -> MEDIUM
            75 -> HIGH
            100 -> VERY_HIGH
            else -> MEDIUM
        }
    }
}

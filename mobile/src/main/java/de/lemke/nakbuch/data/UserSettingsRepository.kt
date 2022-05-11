package de.lemke.nakbuch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Provides CRUD operations for user settings. */
class UserSettingsRepository(
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
            it[KEY_BUCH_MODE] = newSettings.buchMode
            it[KEY_NUMBER] = newSettings.number
            it[KEY_SEARCH] = newSettings.search
            it[KEY_LAST_VERSION_CODE] = newSettings.lastVersionCode
            it[KEY_LAST_VERSION_NAME] = newSettings.lastVersionName
            it[KEY_TEXTSIZE] = newSettings.textSize
            it[KEY_EASTER_EGGS_ENABLED] = newSettings.easterEggsEnabled
            it[KEY_HISTORY_ENABLED] = newSettings.historyEnabled
            it[KEY_ALTERNATIVE_SEARCHMODE_ENABLED] = newSettings.alternativeSearchModeEnabled
            it[KEY_JOKE_BUTTON_VISIBLE] = newSettings.jokeButtonVisible
            it[KEY_NOTES_VISIBLE] = newSettings.notesVisible
            it[KEY_SUNG_ON_VISIBLE] = newSettings.sungOnVisible
        }
        return settingsFromPreferences(prefs)
    }

    private fun settingsFromPreferences(prefs: Preferences) = UserSettings(
        buchMode = prefs[KEY_BUCH_MODE] ?: BuchMode.Gesangbuch.toInt(),
        search = prefs[KEY_SEARCH] ?: "",
        lastVersionCode = prefs[KEY_LAST_VERSION_CODE] ?: -1,
        lastVersionName = prefs[KEY_LAST_VERSION_NAME] ?: "0.0",
        textSize = prefs[KEY_TEXTSIZE] ?: SettingsRepository.DEFAULT_TEXT_SIZE,
        easterEggsEnabled = prefs[KEY_EASTER_EGGS_ENABLED] ?: true,
        historyEnabled = prefs[KEY_HISTORY_ENABLED] ?: true,
        alternativeSearchModeEnabled = prefs[KEY_ALTERNATIVE_SEARCHMODE_ENABLED] ?: false,
        jokeButtonVisible = prefs[KEY_JOKE_BUTTON_VISIBLE] ?: true,
        notesVisible = prefs[KEY_NOTES_VISIBLE] ?: false,
        sungOnVisible = prefs[KEY_SUNG_ON_VISIBLE] ?: false,
    )

    private companion object {
        private val KEY_BUCH_MODE = intPreferencesKey("buchMode")
        private val KEY_NUMBER = stringPreferencesKey("number")
        private val KEY_SEARCH = stringPreferencesKey("search")
        private val KEY_LAST_VERSION_CODE = intPreferencesKey("lastVersionCode")
        private val KEY_LAST_VERSION_NAME = stringPreferencesKey("lastVersionName")
        private val KEY_TEXTSIZE = intPreferencesKey("textsize")
        private val KEY_EASTER_EGGS_ENABLED = booleanPreferencesKey("easterEggsEnabled")
        private val KEY_HISTORY_ENABLED = booleanPreferencesKey("historyEnabled")
        private val KEY_ALTERNATIVE_SEARCHMODE_ENABLED = booleanPreferencesKey("alternativeSearchModeEnabled")
        private val KEY_JOKE_BUTTON_VISIBLE = booleanPreferencesKey("jokeButtonVisible")
        private val KEY_NOTES_VISIBLE = booleanPreferencesKey("noteVisible")
        private val KEY_SUNG_ON_VISIBLE = booleanPreferencesKey("sungOnVisible")
    }
}

/** Settings associated with the current user. */
data class UserSettings(
    /** The currently active BuchMode */
    val buchMode: Int = BuchMode.Gesangbuch.toInt(),
    /** Currently selected Number as String */
    val number: String = "",
    /** Current Search  */
    val search: String = "",
    /** Last App-Version-Code */
    val lastVersionCode: Int = -1,
    /** Last App-Version-Name */
    val lastVersionName: String = "0.0",
    /** Text Size */
    val textSize: Int = SettingsRepository.DEFAULT_TEXT_SIZE,
    /** Easter Eggs enabled */
    val easterEggsEnabled: Boolean = true,
    /** History enabled */
    val historyEnabled: Boolean = true,
    /** Is alternative Search-Mode enabled */
    val alternativeSearchModeEnabled: Boolean = false,
    /** Is Joke-Button visible*/
    val jokeButtonVisible: Boolean = true,
    /** Are Notes visible*/
    val notesVisible: Boolean = false,
    /** Is Sung-On visible*/
    val sungOnVisible: Boolean = false,

    /*
    * Easter Egg List
    * HistoryList
    * Recent Color List
    * test
    * */
)
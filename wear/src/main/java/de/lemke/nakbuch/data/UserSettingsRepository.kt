package de.lemke.nakbuch.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.lemke.nakbuch.domain.model.BuchMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Provides CRUD operations for user settings. */
class UserSettingsRepository@Inject constructor(
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
            it[KEY_LAST_VERSION_CODE] = newSettings.lastVersionCode
            it[KEY_LAST_VERSION_NAME] = newSettings.lastVersionName
            it[KEY_TEXTSIZE] = newSettings.textSize
        }
        return settingsFromPreferences(prefs)
    }

    private fun settingsFromPreferences(prefs: Preferences) = UserSettings(
        number = prefs[KEY_NUMBER] ?: "",
        buchMode = BuchMode.fromInt(prefs[KEY_BUCH_MODE]) ?: BuchMode.Gesangbuch,
        lastVersionCode = prefs[KEY_LAST_VERSION_CODE] ?: -1,
        lastVersionName = prefs[KEY_LAST_VERSION_NAME] ?: "0.0",
        textSize = prefs[KEY_TEXTSIZE] ?: DEFAULT_TEXTSIZE,
    )

    private companion object {
        private val KEY_BUCH_MODE = intPreferencesKey("buchMode")
        private val KEY_NUMBER = stringPreferencesKey("number")
        private val KEY_LAST_VERSION_CODE = intPreferencesKey("lastVersionCode")
        private val KEY_LAST_VERSION_NAME = stringPreferencesKey("lastVersionName")
        private val KEY_TEXTSIZE = intPreferencesKey("textsize")
        private const val DEFAULT_TEXTSIZE = 12
    }
}

/** Settings associated with the current user. */
data class UserSettings(
    /** The currently active BuchMode */
    val buchMode: BuchMode,
    /** Currently selected Number as String */
    val number: String,
    /** Last App-Version-Code */
    val lastVersionCode: Int,
    /** Last App-Version-Name */
    val lastVersionName: String,
    /** Text Size */
    val textSize: Int,
)
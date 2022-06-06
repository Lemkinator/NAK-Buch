package de.lemke.nakbuch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import javax.inject.Inject


class HymnsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {
    private var allHymnsGesangbuch: List<Hymn>? = null
    private var allHymnsChorbuch: List<Hymn>? = null
    private var allHymnsJugendliederbuch: List<Hymn>? = null
    private var allHymnsJBErgaenzungsheft: List<Hymn>? = null

    suspend fun getHymnByNumber(buchMode: BuchMode, number: Int): Hymn = getAllHymns(buchMode)[number.coerceIn(1, buchMode.hymnCount) - 1]

    suspend fun getAllHymns(buchMode: BuchMode): List<Hymn> = when (buchMode) {
        BuchMode.Gesangbuch -> {
            if (allHymnsGesangbuch == null) allHymnsGesangbuch = getAllHymnsFromAssets(BuchMode.Gesangbuch)
            allHymnsGesangbuch!!
        }
        BuchMode.Chorbuch -> {
            if (allHymnsChorbuch == null) allHymnsChorbuch = getAllHymnsFromAssets(BuchMode.Chorbuch)
            allHymnsChorbuch!!
        }
        BuchMode.Jugendliederbuch -> {
            if (allHymnsJugendliederbuch == null) allHymnsJugendliederbuch = getAllHymnsFromAssets(BuchMode.Jugendliederbuch)
            allHymnsJugendliederbuch!!
        }
        BuchMode.JBErgaenzungsheft -> {
            if (allHymnsJBErgaenzungsheft == null) allHymnsJBErgaenzungsheft = getAllHymnsFromAssets(BuchMode.JBErgaenzungsheft)
            allHymnsJBErgaenzungsheft!!
        }
    }

    @Suppress("unchecked_Cast", "BlockingMethodInNonBlockingContext")
    private suspend fun getAllHymnsFromAssets(buchMode: BuchMode): List<Hymn> {
        val fis: InputStream
        val ois: ObjectInputStream
        var list: java.util.ArrayList<java.util.HashMap<String, String>>? = null
        val privateTexts = getPrivateTexts(buchMode)
        if (privateTexts != null && privateTexts.isNotEmpty()) return privateTexts
        try {
            fis = context.resources.assets.open(buchMode.assetFileName)
            ois = ObjectInputStream(fis)
            list = ois.readObject() as java.util.ArrayList<java.util.HashMap<String, String>>
            ois.close()
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list!!.map {
            Hymn(
                buchMode,
                it["hymnNr"]!!.toInt(),
                it["hymnNrAndTitle"]!!,
                it["hymnTitle"]!!,
                it["hymnText"]!!.replace("</p><p>", "\n\n").replace("<br>", ""),
                it["hymnCopyright"]!!.replace("<br>", ""),
            )
        }
    }

    suspend fun deletePrivateTexts() {
        dataStore.edit { it[stringPreferencesKey(BuchMode.Gesangbuch.toString())] = "" }
        dataStore.edit { it[stringPreferencesKey(BuchMode.Chorbuch.toString())] = "" }
        dataStore.edit { it[stringPreferencesKey(BuchMode.Jugendliederbuch.toString())] = "" }
        dataStore.edit { it[stringPreferencesKey(BuchMode.JBErgaenzungsheft.toString())] = "" }
    }

    @Suppress("unchecked_Cast", "BlockingMethodInNonBlockingContext")
    suspend fun setPrivateTexts(buchMode: BuchMode, bArray: ByteArray): Boolean {
        val result: ArrayList<HashMap<String, String>>
        try {
            val bis = ByteArrayInputStream(bArray)
            val ois = ObjectInputStream(bis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            dataStore.edit { pref ->
                pref[stringPreferencesKey(buchMode.toString())] = Gson().toJson(
                    result.map {
                        Hymn(
                            buchMode,
                            it["hymnNr"]!!.toInt(),
                            it["hymnNrAndTitle"]!!,
                            it["hymnTitle"]!!,
                            it["hymnText"]!!.replace("</p><p>", "\n\n").replace("<br>", ""),
                            it["hymnCopyright"]!!.replace("<br>", ""),
                        )
                    }
                )
            }
            ois.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private suspend fun getPrivateTexts(buchMode: BuchMode): List<Hymn>? =
        Gson().fromJson(dataStore.data.map {
            it[stringPreferencesKey(buchMode.toString())]
        }.first(), object : TypeToken<List<Hymn>?>() {}.type)
}



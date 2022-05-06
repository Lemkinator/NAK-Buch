package de.lemke.nakbuch.data

import android.content.SharedPreferences
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.App
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream

val hymnsRepo = HymnsRepository(App.defaultSharedPreferences, App.myResources)

class HymnsRepository(private val sp: SharedPreferences, private val ressources: Resources) {
    private var allHymnsGesangbuch: ArrayList<Hymn>? = null
    private var allHymnsChorbuch: ArrayList<Hymn>? = null

    fun hymnCount(buchMode: BuchMode) = if (buchMode == BuchMode.Gesangbuch) 438 else 462

    suspend fun getHymnByNumber(buchMode: BuchMode, number: Int): Hymn = getAllHymns(buchMode)[number.coerceIn(1,hymnCount(buchMode)) - 1]

    suspend fun getAllHymns(buchMode: BuchMode): ArrayList<Hymn> =
        if (buchMode == BuchMode.Gesangbuch) {
            if (allHymnsGesangbuch == null) allHymnsGesangbuch = getAllHymnsFromAssets(buchMode)
            ArrayList(allHymnsGesangbuch!!)
        } else {
            if (allHymnsChorbuch == null) allHymnsChorbuch = getAllHymnsFromAssets(buchMode)
            ArrayList(allHymnsChorbuch!!)
        }

    @Suppress("unchecked_Cast") //, "BlockingMethodInNonBlockingContext")
    private suspend fun getAllHymnsFromAssets(buchMode: BuchMode): ArrayList<Hymn> {
        val fis: InputStream
        val ois: ObjectInputStream
        var list: java.util.ArrayList<java.util.HashMap<String, String>>? = null
        if (buchMode == BuchMode.Gesangbuch && sp.getString("privateTextGesangbuch", null) != null) {
            return Gson().fromJson(
                sp.getString("privateTextGesangbuch", null),
                object : TypeToken<java.util.ArrayList<java.util.HashMap<String, String>>>() {}.type
            )
        } else if (buchMode == BuchMode.Chorbuch && sp.getString("privateTextChorbuch", null) != null) {
            return Gson().fromJson(
                sp.getString("privateTextChorbuch", null),
                object : TypeToken<java.util.ArrayList<java.util.HashMap<String, String>>>() {}.type
            )
        }
        try {
            fis = if (buchMode == BuchMode.Gesangbuch) ressources.assets.open("hymnsGesangbuchNoCopyright.txt")
            else ressources.assets.open("hymnsChorbuchNoCopyright.txt")
            ois = ObjectInputStream(fis)
            list = ois.readObject() as java.util.ArrayList<java.util.HashMap<String, String>>
            ois.close()
            fis.close()
        } catch (c: IOException) {
            c.printStackTrace()
        } catch (c: ClassNotFoundException) {
            c.printStackTrace()
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
        } as ArrayList<Hymn>
    }

    fun deletePrivateTexts() {
        sp.edit().putStringSet("privateTextGesangbuch", null).apply()
        sp.edit().putStringSet("privateTextChorbuch", null).apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun setPrivateTexts(spKey: String, bArray: ByteArray): Boolean {
        val result: ArrayList<HashMap<String, String>>
        try {
            val bis = ByteArrayInputStream(bArray)
            val ois = ObjectInputStream(bis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            sp.edit().putString(spKey, Gson().toJson(result)).apply()
            ois.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}


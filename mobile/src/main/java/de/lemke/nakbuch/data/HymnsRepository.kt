package de.lemke.nakbuch.data

import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.App
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.Rubric
import de.lemke.nakbuch.domain.model.hymnPlaceholder
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream

class HymnsRepository(private val sp: SharedPreferences, private val ressources: Resources) {
    private var allHymnsGesangbuch: ArrayList<Hymn>? = null
    private var allHymnsChorbuch: ArrayList<Hymn>? = null
    private var allHymnsSortedAlphabeticGesangbuch: ArrayList<Hymn>? = null
    private var allHymnsSortedAlphabeticChorbuch: ArrayList<Hymn>? = null
    private var allRubricsGesangbuch: ArrayList<Rubric>? = null
    private var allRubricsChorbuch: ArrayList<Rubric>? = null

    fun hymnCount(buchMode: BuchMode) = if (buchMode == BuchMode.Gesangbuch) 438 else 462

    suspend fun getHymnByNumber(buchMode: BuchMode, number: Int): Hymn {
        if (0 <= number && number <= hymnCount(buchMode)) return getAllHymns(buchMode)[number - 1]
        return hymnPlaceholder //TODO sense?
    }

    suspend fun getAllHymnsSortedAlphabetic(buchMode: BuchMode): ArrayList<Hymn> =
        if (buchMode == BuchMode.Gesangbuch) {
            if (allHymnsSortedAlphabeticGesangbuch == null) {
                allHymnsSortedAlphabeticGesangbuch = getAllHymns(buchMode)
                allHymnsSortedAlphabeticGesangbuch!!.sortWith(compareBy { it.title })
            }
            ArrayList(allHymnsSortedAlphabeticGesangbuch!!)
        } else {
            if (allHymnsSortedAlphabeticChorbuch == null) {
                allHymnsSortedAlphabeticChorbuch = getAllHymns(buchMode)
                allHymnsSortedAlphabeticChorbuch!!.sortWith(compareBy { it.title })
            }
            ArrayList(allHymnsSortedAlphabeticChorbuch!!)
        }

    suspend fun getAllRubrics(buchMode: BuchMode): ArrayList<Rubric> {
        return if (buchMode == BuchMode.Gesangbuch) {
            if (allRubricsGesangbuch == null) {
                allRubricsGesangbuch = initRubricList(buchMode)
            }
            ArrayList(allRubricsGesangbuch!!)
        } else {
            if (allRubricsChorbuch == null) {
                allRubricsChorbuch = initRubricList(buchMode)
            }
            ArrayList(allRubricsChorbuch!!)
        }
    }

    private suspend fun initRubricList(buchMode: BuchMode): ArrayList<Rubric> {
        val rubList = ArrayList<Rubric>()
        for (i in 0 until if (buchMode == BuchMode.Gesangbuch) 29 else 31) rubList.add(Rubric(buchMode, i))
        return rubList
    }

    suspend fun getSearchList(buchMode: BuchMode, search: String): ArrayList<Hymn> {
        val result = ArrayList<Hymn>()
        if (search.isNotEmpty()) {
            if (search.startsWith("\"") && search.endsWith("\"")) {
                if (search.length > 2) {
                    val s = search.substring(1, search.length - 1)
                    if (sp.getBoolean("searchAlternativeMode", false)) {
                        addToSearchWithKeywords(buchMode, result, HashSet(listOf(s)))
                    } else {
                        addToSearchWithKeywords(buchMode, result, HashSet(s.trim().split(" ")))
                    }
                }
            } else {
                if (sp.getBoolean("searchAlternativeMode", false)) {
                    addToSearchWithKeywords(buchMode, result, HashSet(search.trim().split(" ")))
                } else {
                    addToSearchWithKeywords(buchMode, result, HashSet(listOf(search)))
                }
            }
        }
        return result
    }

    private suspend fun addToSearchWithKeywords(buchMode: BuchMode, hymnList: ArrayList<Hymn>, searchs: HashSet<String>) {
        for (s in searchs) {
            if (s.isNotBlank()) {
                for (hymn in getAllHymns(buchMode)) {
                    if (hymn.text.contains(s, ignoreCase = true) ||
                        hymn.title.contains(s, ignoreCase = true) ||
                        hymn.copyright.contains(s, ignoreCase = true)
                    ) {
                        hymnList.add(hymn)
                    }
                }
            }
        }
    }

    suspend fun getAllHymns(buchMode: BuchMode): ArrayList<Hymn> =
        if (buchMode == BuchMode.Gesangbuch) {
            if (allHymnsGesangbuch == null) allHymnsGesangbuch = getAllHymnsFromAssets(buchMode)
            ArrayList(allHymnsGesangbuch!!)
        } else {
            if (allHymnsChorbuch == null) allHymnsChorbuch = getAllHymnsFromAssets(buchMode)
            ArrayList(allHymnsChorbuch!!)
        }

    @Suppress("unchecked_Cast") //, "BlockingMethodInNonBlockingContext")
    private suspend fun getAllHymnsFromAssets(
        buchMode: BuchMode
    ): ArrayList<Hymn> {
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
        val result = ArrayList<Hymn>()
        for (hm: HashMap<String, String> in list!!)
            result.add(
                Hymn(
                    buchMode,
                    hm["hymnNr"]!!.toInt(),
                    Rubric(buchMode, hm["hymnRubricIndex"]!!.toInt()),
                    hm["hymnNrAndTitle"]!!,
                    hm["hymnTitle"]!!,
                    hm["hymnText"]!!.replace("</p><p>", "\n\n").replace("<br>", ""),
                    hm["hymnCopyright"]!!.replace("<br>", ""),
                )
            )
        return result
    }

    fun deletePrivateTexts() {
        sp.edit().putStringSet("privateTextGesangbuch", null).apply()
        sp.edit().putStringSet("privateTextChorbuch", null).apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun setPrivateTexts(uri: Uri, buchMode: BuchMode): Boolean {
        val fis: InputStream?
        val ois: ObjectInputStream
        val result: ArrayList<HashMap<String, String>>
        try {
            fis = App.myRepository.contentResolver.openInputStream(uri)
            ois = ObjectInputStream(fis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            sp.edit().putString(
                if (buchMode == BuchMode.Gesangbuch) "privateTextGesangbuch"
                else "privateTextChorbuch",
                Gson().toJson(result)
            ).apply()
            ois.close()
            fis!!.close()
        } catch (e: Exception) {
            Log.e("setPrivateHymnText", e.toString() + "\n" + e.message.toString())
            return false
        }
        return true
    }


    fun getRubricListItemArrayList(gesangbuchSelected: Boolean): ArrayList<Int> {
        return if (gesangbuchSelected) {
            ArrayList(listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1))
        } else {
            ArrayList(listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1))
        }
    }

    fun getRubricTitlesArrayList(gesangbuchSelected: Boolean): ArrayList<String> {
        return if (gesangbuchSelected) {
            ArrayList(
                listOf(
                    "Das geistliche Jahr",
                    "Advent",
                    "Weihnachten",
                    "Jahreswende",
                    "Palmsonntag",
                    "Karfreitag - Christi Leiden",
                    "Ostern",
                    "Christi Himmelfahrt",
                    "Pfingsten",
                    "Bußtag - Einsicht und Umkehr",
                    "Gottesdienst",
                    "Einladung - Heilsverlangen - Heiligung",
                    "Glaube - Vertrauen - Trost",
                    "Gottes Liebe - Nächstenliebe",
                    "Vergebung - Gnade",
                    "Lob - Dank - Anbetung",
                    "Sakramente",
                    "Heilige Taufe",
                    "Heiliges Abendmahl",
                    "Heilige Versiegelung",
                    "Segenshandlungen",
                    "Konfirmation",
                    "Trauung",
                    "Den Glauben leben",
                    "Morgen und Abend",
                    "Gemeinde - Gemeinschaft",
                    "Sendung - Nachfolge - Bekenntnis",
                    "Verheißung - Erwartung - Erfüllung",
                    "Sterben - Ewiges Leben"
                )
            )
        } else {
            ArrayList(
                listOf(
                    "Das geistliche Jahr",
                    "Advent",
                    "Weihnachten",
                    "Jahreswechsel",
                    "Palmsonntag",
                    "Passion",
                    "Ostern",
                    "Himmelfahrt",
                    "Pfingsten",
                    "Erntedank",
                    "Gottesdienst",
                    "Einladung - Heilsverlangen - Heiligung",
                    "Anbetung",
                    "Glaube - Vertrauen",
                    "Trost - Mut - Frieden",
                    "Gnade - Vergebung",
                    "Lobpreis Gottes",
                    "Sakramente",
                    "Heilige Taufe",
                    "Heiliges Abendmahl",
                    "Heilige Versiegelung",
                    "Segenshandlungen",
                    "Konfirmation",
                    "Trauung",
                    "Den Glauben leben",
                    "Morgen - Abend",
                    "Gottes Liebe - Nächstenliebe",
                    "Mitarbeit - Gemeinschaft",
                    "Sendung - Nachfolge - Bekenntnis",
                    "Verheißung - Erwartung - Erfüllung",
                    "Sterben - Ewiges Leben"
                )
            )
        }
    }
}

val hymnsRepo = HymnsRepository(App.myRepository.defaultSharedPreferences, App.myRepository.resources)


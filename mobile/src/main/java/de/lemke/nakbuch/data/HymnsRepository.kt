package de.lemke.nakbuch.data

import App
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.Rubric
import de.lemke.nakbuch.domain.model.hymnPlaceholder
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream

val hymnsRepo = HymnsRepository()

class HymnsRepository {
    private var allGesangbuchHymns: ArrayList<Hymn>? = null
    private var allChorbuchHymns: ArrayList<Hymn>? = null
    private var allGesangbuchHymnsSortedAlphabetic: ArrayList<Hymn>? = null
    private var allChorbuchHymnsSortedAlphabetic: ArrayList<Hymn>? = null


    suspend fun getHymnByNumber(
        buchMode: BuchMode,
        number: Int
    ): Hymn = getAllHymns(buchMode)[number - 1]

    suspend fun getAllHymnsSortedAlphabetic(buchMode: BuchMode): ArrayList<Hymn> =
        if (buchMode == BuchMode.Gesangbuch) {
            if (allGesangbuchHymnsSortedAlphabetic == null) {
                allGesangbuchHymnsSortedAlphabetic = ArrayList(getAllHymns(buchMode))
                allGesangbuchHymnsSortedAlphabetic!!.sortWith(compareBy { it.title })
            }
            allGesangbuchHymnsSortedAlphabetic!!
        } else {
            if (allChorbuchHymnsSortedAlphabetic == null) {
                allChorbuchHymnsSortedAlphabetic = ArrayList(getAllHymns(buchMode))
                allChorbuchHymnsSortedAlphabetic!!.sortWith(compareBy { it.title })
            }
            allChorbuchHymnsSortedAlphabetic!!
        }


    suspend fun getAllHymnsSortedRubric(
        buchMode: BuchMode,
        rubricIndex: Int
    ): ArrayList<Hymn> {
        TODO("not implemented")
    }

    suspend fun getAllHymnsSearchList(buchMode: BuchMode, search: String): ArrayList<Hymn> {
        val sp = App.myRepository.getDefaultSharedPreferences()
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
        result.add(hymnPlaceholder) //Placeholder
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
            if (allGesangbuchHymns == null) allGesangbuchHymns = getAllHymnsFromAssets(buchMode)
            allGesangbuchHymns!!
        } else {
            if (allChorbuchHymns == null) allChorbuchHymns = getAllHymnsFromAssets(buchMode)
            allChorbuchHymns!!
        }

    @Suppress("unchecked_Cast") //, "BlockingMethodInNonBlockingContext")
    private suspend fun getAllHymnsFromAssets(
        buchMode: BuchMode
    ): ArrayList<Hymn> {
        val sp = App.myRepository.getDefaultSharedPreferences()
        val assets = App.myRepository.getAssets()
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
            fis =
                if (buchMode == BuchMode.Gesangbuch) assets.open("hymnsGesangbuchNoCopyright.txt") else assets.open(
                    "hymnsChorbuchNoCopyright.txt"
                )
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
                    hm["hymnText"]!!,
                    hm["hymnCopyright"]!!,
                )
            )
        return result
    }
}

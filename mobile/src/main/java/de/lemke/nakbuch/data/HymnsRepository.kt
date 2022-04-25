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


    fun getHymnByNumber(
        buchMode: BuchMode,
        number: Int
    ): Hymn = getAllHymns(buchMode)[number - 1]

    fun getAllHymnsSortedAlphabetic(buchMode: BuchMode) = getAllHymns(buchMode).sortWith(Comparator.comparing { hymn: Hymn -> hymn.title })

    fun getAllHymnsSortedRubric(
        buchMode: BuchMode,
        rubricIndex: Int
    ) {
        TODO("not implemented")
    }
    fun getAllHymnsSearchList(buchMode: BuchMode, search: String): ArrayList<Hymn> {
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

    private fun addToSearchWithKeywords(buchMode: BuchMode, hymnList: ArrayList<Hymn>, searchs: HashSet<String>) {
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

    fun getAllHymns(buchMode: BuchMode) =
        if (buchMode == BuchMode.Gesangbuch) allGesangbuchHymns ?: getAllHymnsFromAssets(buchMode)
        else allChorbuchHymns ?: getAllHymnsFromAssets(buchMode)

    private fun getAllHymnsFromAssets(
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

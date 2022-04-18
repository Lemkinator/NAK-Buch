package de.lemke.nakbuch.data

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.Rubric
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream

val hymnsRepo = HymnsRepository()

class HymnsRepository {
    private val allGesangbuchHymns: ArrayList<Hymn>? = null
    private val allChorbuchHymns: ArrayList<Hymn>? = null

    companion object {
        fun hymnCount(gesangbuchSelected: Boolean) = if (gesangbuchSelected) 438 else 462
    }

    fun getHymnByNumber(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean,
        number: Int
    ): Hymn = getAllHymns(mContext, sp, gesangbuchSelected)[number - 1]

    fun getAllHymnsSortedAlphabetic(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean
    ) =
        getAllHymns(
            mContext,
            sp,
            gesangbuchSelected
        ).sortWith(Comparator.comparing { hymn: Hymn -> hymn.title })

    fun getAllHymnsSortedRubric(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean,
        rubricIndex: Int
    ) {
        TODO("not implemented")
    }

    fun getAllHymns(mContext: Context, sp: SharedPreferences, gesangbuchSelected: Boolean) =
        if (gesangbuchSelected) allGesangbuchHymns ?: getAllHymnsFromAssets(
            mContext,
            sp,
            gesangbuchSelected
        )
        else allChorbuchHymns ?: getAllHymnsFromAssets(
            mContext,
            sp,
            gesangbuchSelected
        )

    private fun getAllHymnsFromAssets(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean
    ): ArrayList<Hymn> {
        val fis: InputStream
        val ois: ObjectInputStream
        var list: java.util.ArrayList<java.util.HashMap<String, String>>? = null
        if (gesangbuchSelected && sp.getString("privateTextGesangbuch", null) != null) {
            return Gson().fromJson(
                sp.getString("privateTextGesangbuch", null),
                object : TypeToken<java.util.ArrayList<java.util.HashMap<String, String>>>() {}.type
            )
        } else if (!gesangbuchSelected && sp.getString("privateTextChorbuch", null) != null) {
            return Gson().fromJson(
                sp.getString("privateTextChorbuch", null),
                object : TypeToken<java.util.ArrayList<java.util.HashMap<String, String>>>() {}.type
            )
        }
        try {
            fis =
                if (gesangbuchSelected) mContext.assets.open("hymnsGesangbuchNoCopyright.txt") else mContext.assets.open(
                    "hymnsChorbuchNoCopyright.txt"
                )
            ois = ObjectInputStream(fis)
            list = ois.readObject() as java.util.ArrayList<java.util.HashMap<String, String>>
            ois.close()
            fis.close()
        } catch (c: IOException) {
            c.printStackTrace()
            Toast.makeText(mContext, c.toString(), Toast.LENGTH_LONG).show()
        } catch (c: ClassNotFoundException) {
            c.printStackTrace()
            Toast.makeText(mContext, c.toString(), Toast.LENGTH_LONG).show()
        }
        val result = ArrayList<Hymn>()
        for (hm: HashMap<String, String> in list!!)
            result.add(
                Hymn(
                    if (gesangbuchSelected) BuchMode.Gesangbuch else BuchMode.Chorbuch,
                    hm["hymnNr"]!!.toInt(),
                    Rubric(mContext, gesangbuchSelected, hm["hymnRubricIndex"]!!.toInt()),
                    hm["hymnNrAndTitle"]!!,
                    hm["hymnTitle"]!!,
                    hm["hymnText"]!!,
                    hm["hymnCopyright"]!!,
                )
            )
        return result
    }
}

package de.lemke.nakbuch.domain.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.domain.utils.AssetsHelper.getHymnArrayList

object HymnPrefsHelper {
    private lateinit var hymnAdditions: ArrayList<HashMap<String, String>>
    private fun getEmptyList(size: Int): ArrayList<HashMap<String, String>> {
        val result = ArrayList<HashMap<String, String>>()
        for (i in 0 until size) {
            val hm = HashMap<String, String>()
            result.add(hm)
        }
        return result
    }

    private fun getHymnAdditionsList(gesangbuchSelected: Boolean, spHymns: SharedPreferences): ArrayList<HashMap<String, String>> {
        return if (gesangbuchSelected) {
            Gson().fromJson(
                spHymns.getString("hymnAdditionsGesangbuch", Gson().toJson(getEmptyList(438))),
                object : TypeToken<ArrayList<HashMap<String, String>>>() {}.type
            )
        } else {
            Gson().fromJson(
                spHymns.getString(
                    "hymnAdditionsChorbuch",
                    Gson().toJson(getEmptyList(462))
                ),
                object :
                    TypeToken<ArrayList<HashMap<String, String>>>() {}.type
            )
        }
    }

    @JvmStatic
    fun writeToList(gesangbuchSelected: Boolean, spHymns: SharedPreferences, hymnNr: Int, key: String, value: String) {
        hymnAdditions = getHymnAdditionsList(gesangbuchSelected, spHymns)
        hymnAdditions[hymnNr - 1][key] = value
        spHymns.edit().putString(
            if (gesangbuchSelected) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
            Gson().toJson(
                hymnAdditions
            )
        ).apply()
    }

    @JvmStatic
    fun writeFavsToList(gesangbuchSelected: Boolean, spHymns: SharedPreferences, selected: HashMap<Int, Boolean>, hymns: ArrayList<HashMap<String, String>>, value: String) {
        hymnAdditions = getHymnAdditionsList(gesangbuchSelected, spHymns)
        for ((key, value1) in selected) {
            if (value1) {
                hymnAdditions[(hymns[key]["hymnNr"])!!.toInt() - 1]["fav"] =
                    value
            }
        }
        spHymns.edit().putString(
            if (gesangbuchSelected) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
            Gson().toJson(
                hymnAdditions
            )
        ).apply()
    }

    @JvmStatic
    fun getFromList(gesangbuchSelected: Boolean, spHymns: SharedPreferences, hymnNr: Int, key: String): String {
        hymnAdditions = getHymnAdditionsList(gesangbuchSelected, spHymns)
        if (!hymnAdditions[hymnNr - 1].containsKey(key)) {
            hymnAdditions[hymnNr - 1][key] = ""
        }
        return hymnAdditions[hymnNr - 1][key].toString()
    }

    @JvmStatic
    fun getFavList(mContext: Context, gesangbuchSelected: Boolean, sp: SharedPreferences, spHymns: SharedPreferences): ArrayList<HashMap<String, String>> {
        val hymns = getHymnArrayList(mContext, sp, gesangbuchSelected)
        val favHymns = ArrayList<HashMap<String, String>>()
        hymnAdditions = getHymnAdditionsList(gesangbuchSelected, spHymns)
        for (i in hymns.indices) {
            if (hymnAdditions[i]["fav"] == "1") {
                favHymns.add(hymns[i])
            }
        }
        return favHymns
    }
}
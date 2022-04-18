package de.lemke.nakbuch.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.data.HymnsRepository.Companion.hymnCount
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData

val hymnDataRepo = HymnDataRepository()

class HymnDataRepository {
    private var gesangbuchHymnsData: ArrayList<HymnData?>? = null
    private var chorbuchHymnsData: ArrayList<HymnData?>? = null

    fun getHymnData(gesangbuchSelected: Boolean, spHymns: SharedPreferences, hymn: Hymn) =
        getAllHymnData(gesangbuchSelected, spHymns)[hymn.number - 1] ?: HymnData()

    fun getAllHymnData(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences
    ): ArrayList<HymnData?> {
        return if (gesangbuchSelected) gesangbuchHymnsData ?: getHymnDataFromPreferences(
            gesangbuchSelected,
            spHymns
        )
        else chorbuchHymnsData ?: getHymnDataFromPreferences(gesangbuchSelected, spHymns)
    }

    fun setHymnData(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences,
        hymn: Hymn,
        hymnData: HymnData
    ) {
        getAllHymnData(gesangbuchSelected, spHymns)[hymn.number - 1] = hymnData
        writeHymnDataToPreferences(gesangbuchSelected, spHymns)
    }

    fun setAllHymnData(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences,
        hymnsData: ArrayList<HymnData?>
    ) {
        if (gesangbuchSelected) gesangbuchHymnsData else chorbuchHymnsData = hymnsData
        writeHymnDataToPreferences(gesangbuchSelected, spHymns)
    }

    private fun getHymnDataFromPreferences(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences
    ): ArrayList<HymnData?> {
        return Gson().fromJson(
            spHymns.getString(
                if (gesangbuchSelected) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
                Gson().toJson(ArrayList<HymnData?>(hymnCount(gesangbuchSelected)))
            ),
            object : TypeToken<ArrayList<HymnData?>>() {}.type
        )
    }

    private fun writeHymnDataToPreferences(
        gesangbuchSelected: Boolean,
        spHymns: SharedPreferences
    ) {
        spHymns.edit().putString(
            if (gesangbuchSelected) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
            Gson().toJson(
                getAllHymnData(gesangbuchSelected, spHymns)
            )
        ).apply()
    }

    fun getFavList(
        mContext: Context,
        gesangbuchSelected: Boolean,
        sp: SharedPreferences,
        spHymns: SharedPreferences
    ): ArrayList<HashMap<String, String>> {
        TODO("not implemented")
        /*val hymns = AssetsHelper.getHymnArrayList(mContext, sp, gesangbuchSelected)
        val favHymns = ArrayList<HashMap<String, String>>()
        HymnPrefsHelper.hymnAdditions = getHymnAdditionsList(gesangbuchSelected, spHymns)
        for (i in hymns.indices) {
            if (HymnPrefsHelper.hymnAdditions[i]["fav"] == "1") {
                favHymns.add(hymns[i])
            }
        }
        return favHymns*/
    }
}
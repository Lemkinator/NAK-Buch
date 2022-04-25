package de.lemke.nakbuch.data

import App
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import de.lemke.nakbuch.domain.utils.Constants

val hymnDataRepo = HymnDataRepository(App.myRepository.getHymnDataSharedPreferences())

class HymnDataRepository (private var spHymns: SharedPreferences) {
    private var gesangbuchHymnsData: ArrayList<HymnData?>? = null
    private var chorbuchHymnsData: ArrayList<HymnData?>? = null
    //private var spHymns: SharedPreferences? = null

    fun getHymnData(buchMode: BuchMode, hymn: Hymn) =
        getAllHymnData(buchMode)[hymn.number - 1] ?: HymnData()

    fun setHymnData(
        buchMode: BuchMode,
        hymn: Hymn,
        hymnData: HymnData
    ) {
        getAllHymnData(buchMode)[hymn.number - 1] = hymnData
        writeHymnDataToPreferences(buchMode)
    }

    fun getAllHymnData(
        buchMode: BuchMode,
    ): ArrayList<HymnData?> {
        return if (buchMode == BuchMode.Gesangbuch) {
            if (gesangbuchHymnsData == null) gesangbuchHymnsData = getHymnDataFromPreferences(buchMode)
            gesangbuchHymnsData!!
        } else {
            if (chorbuchHymnsData == null) chorbuchHymnsData = getHymnDataFromPreferences(buchMode)
            chorbuchHymnsData!!
        }
    }

    fun setAllHymnData(
        buchMode: BuchMode,
        hymnsData: ArrayList<HymnData?>
    ) {
        if (buchMode == BuchMode.Gesangbuch) gesangbuchHymnsData else chorbuchHymnsData = hymnsData
        writeHymnDataToPreferences(buchMode)
    }

    private fun writeHymnDataToPreferences(
        buchMode: BuchMode,
    ) {
        getSpHymns().edit().putString(
            if (buchMode == BuchMode.Gesangbuch) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
            Gson().toJson(
                getAllHymnData(buchMode)
            )
        ).apply()
    }

    private fun getSpHymns(): SharedPreferences {
        if (spHymns == null) spHymns = App.myRepository.getHymnDataSharedPreferences()
        return spHymns!!
    }

    private fun getHymnDataFromPreferences(
        buchMode: BuchMode,
    ): ArrayList<HymnData?>? {
        return Gson().fromJson(
            getSpHymns().getString(
                if (buchMode == BuchMode.Gesangbuch) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
                Gson().toJson(ArrayList<HymnData?>(Constants.hymnCount(buchMode)))
            ),
            object : TypeToken<ArrayList<HymnData?>>() {}.type
        )
    }

    fun getFavList(
        buchMode: BuchMode,
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
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

class HymnDataRepository(private var spHymns: SharedPreferences) {
    private var gesangbuchHymnsData: ArrayList<HymnData?>? = null
    private var chorbuchHymnsData: ArrayList<HymnData?>? = null

    suspend fun getHymnData(hymn: Hymn) =
        getAllHymnData(hymn.buchMode)[hymn.number - 1] ?: HymnData()

    suspend fun setHymnData(
        hymn: Hymn,
        hymnData: HymnData
    ) {
        getAllHymnData(hymn.buchMode)[hymn.number - 1] = hymnData
        writeHymnDataToPreferences(hymn.buchMode)
    }

    suspend fun getAllHymnData(
        buchMode: BuchMode,
    ): ArrayList<HymnData?> =
        if (buchMode == BuchMode.Gesangbuch) {
            if (gesangbuchHymnsData == null) gesangbuchHymnsData = getHymnDataFromPreferences(buchMode)
            gesangbuchHymnsData!!
        } else {
            if (chorbuchHymnsData == null) chorbuchHymnsData = getHymnDataFromPreferences(buchMode)
            chorbuchHymnsData!!
        }

    suspend fun setAllHymnData(
        buchMode: BuchMode,
        hymnsData: ArrayList<HymnData?>
    ) {
        if (buchMode == BuchMode.Gesangbuch) gesangbuchHymnsData else chorbuchHymnsData = hymnsData
        writeHymnDataToPreferences(buchMode)
    }

    private suspend fun writeHymnDataToPreferences(
        buchMode: BuchMode,
    ) {
        spHymns.edit().putString(
            if (buchMode == BuchMode.Gesangbuch) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
            Gson().toJson(
                getAllHymnData(buchMode)
            )
        ).apply()
    }

    private suspend fun getHymnDataFromPreferences(
        buchMode: BuchMode,
    ): ArrayList<HymnData?>? {
        return Gson().fromJson(
            spHymns.getString(
                if (buchMode == BuchMode.Gesangbuch) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
                Gson().toJson(ArrayList<HymnData?>(Constants.hymnCount(buchMode)))
            ), object : TypeToken<ArrayList<HymnData?>>() {}.type
        )
    }
}
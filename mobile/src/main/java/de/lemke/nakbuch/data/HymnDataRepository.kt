package de.lemke.nakbuch.data


import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.lemke.nakbuch.App
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import java.io.File
import java.time.LocalDate
import java.util.*

val hymnDataRepo = HymnDataRepository(App.hymnDataSharedPreferences, App.myCacheDir, App.myFilesDir)

class HymnDataRepository(private val spHymns: SharedPreferences, private val cacheDir: File, private val filesDir: File) {
    private var gesangbuchHymnsData: ArrayList<HymnData>? = null
    private var chorbuchHymnsData: ArrayList<HymnData>? = null

    companion object {
        private const val HISTORYSIZE = 500
    }

    suspend fun getHymnData(hymn: Hymn): HymnData {
        if (hymn.number <= 0 || hymn.number > hymnsRepo.hymnCount(hymn.buchMode)) return HymnData()
        return getAllHymnData(hymn.buchMode)[hymn.number - 1]
    }

    suspend fun getAllHymnData(buchMode: BuchMode): ArrayList<HymnData> =
        if (buchMode == BuchMode.Gesangbuch) {
            if (gesangbuchHymnsData == null) gesangbuchHymnsData = getHymnDataFromPreferences(buchMode)
            gesangbuchHymnsData!!
        } else {
            if (chorbuchHymnsData == null) chorbuchHymnsData = getHymnDataFromPreferences(buchMode)
            chorbuchHymnsData!!
        }

    suspend fun setHymnData(hymn: Hymn, hymnData: HymnData) {
        if (0 <= hymn.number && hymn.number <= hymnsRepo.hymnCount(hymn.buchMode))
            getAllHymnData(hymn.buchMode)[hymn.number - 1] = hymnData
    }

    suspend fun setAllHymnData(buchMode: BuchMode, hymnsData: ArrayList<HymnData>) {
        if (buchMode == BuchMode.Gesangbuch) gesangbuchHymnsData else chorbuchHymnsData = hymnsData
        writeHymnDataToPreferences(buchMode)
    }

    suspend fun writeHymnDataToPreferences(buchMode: BuchMode) {
        spHymns.edit().putString(
            if (buchMode == BuchMode.Gesangbuch) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch",
            Gson().toJson(getAllHymnData(buchMode))
        ).apply()
    }

    private suspend fun getHymnDataFromPreferences(buchMode: BuchMode): ArrayList<HymnData> {
        var hymnData: ArrayList<HymnData>? = Gson().fromJson(
            spHymns.getString(
                if (buchMode == BuchMode.Gesangbuch) "hymnAdditionsGesangbuch" else "hymnAdditionsChorbuch", null
            ), object : TypeToken<ArrayList<HymnData>?>() {}.type
        )
        if (hymnData == null) {
            hymnData = ArrayList()
            for (i in 0 until hymnsRepo.hymnCount(buchMode)) hymnData.add(HymnData())
        }
        return hymnData
    }

    suspend fun resetHistory() = spHymns.edit().putString("historyList", null).apply()
    suspend fun getHistoryList(): ArrayList<Pair<Hymn, LocalDate>> {
        var historyList: ArrayList<Pair<Hymn, LocalDate>>? = Gson().fromJson(
            spHymns.getString("historyList", null),
            object : TypeToken<ArrayList<Pair<Hymn, LocalDate>>?>() {}.type
        )
        if (historyList == null) historyList = ArrayList()
        return historyList
    }

    suspend fun addToHistoryList(hymn: Hymn) {
        val historyList: ArrayList<Pair<Hymn, LocalDate>> = getHistoryList()
        historyList.add(Pair(hymn, LocalDate.now()))
        if (historyList.size > HISTORYSIZE) historyList.removeLast()
        spHymns.edit().putString("historyList", Gson().toJson(historyList)).apply()
    }

    fun getUri(hymn: Hymn): Uri = File("$filesDir/hymnPhotos/${hymn.buchMode}/${hymn.number}/${UUID.randomUUID()}.jpg").toUri()

    fun getTempPhotoUri(): Uri = File(cacheDir, "currentPhotoUncompressed.jpg").toUri()

    suspend fun deletePhoto(uri: Uri) {
        val fdelete = uri.toFile()
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.d("deleted File", fdelete.absolutePath)
            } else {
                Log.e("could not delete File", fdelete.absolutePath)
            }
        }
    }

}

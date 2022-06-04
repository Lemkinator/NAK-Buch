package de.lemke.nakbuch.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.ObjectInputStream
import javax.inject.Inject

class InitDatabaseUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
    private val setRecentColors: SetRecentColorsUseCase,
    private val setHints: SetHintsUseCase,
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(forceInit: Boolean = false): Job = CoroutineScope(Dispatchers.Default).launch {
        for (buchMode in BuchMode.values()) {
            if (hymnsRepository.getAllRubrics(buchMode).size < buchMode.rubricCount)
                hymnsRepository.addRubrics(List(buchMode.rubricCount) { index -> Rubric(RubricId.create(index, buchMode)!!) })
            if (hymnsRepository.getAllHymns(buchMode).size < buchMode.hymnCount || forceInit)
                hymnsRepository.addHymns(getAllHymnsFromAssets(buchMode))
        }
        //create new db prepop
        //setRecentColors(listOf(context.resources.getColor(R.color.primary_color, context.theme)))
        //setHints(context.resources.getStringArray(R.array.hint_values).toSet())
    }

    @Suppress("unchecked_cast")
    fun getAllHymnsFromAssets(buchMode: BuchMode): List<Hymn> {
        val fis: InputStream
        val ois: ObjectInputStream
        var list: java.util.ArrayList<java.util.HashMap<String, String>>? = null
        try {
            fis = context.resources.assets.open(buchMode.assetFileName)
            ois = ObjectInputStream(fis)
            list = ois.readObject() as java.util.ArrayList<java.util.HashMap<String, String>>
            ois.close()
            fis.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list!!.map {
            Hymn(
                HymnId.create(it["hymnNr"]!!.toInt(), buchMode)!!,
                Rubric(RubricId.create(it["hymnRubricIndex"]!!.toInt(), buchMode)!!),
                it["hymnNrAndTitle"]!!,
                it["hymnTitle"]!!,
                it["hymnText"]!!.replace("</p><p>", "\n\n").replace("<br>", ""),
                it["hymnCopyright"]!!.replace("<br>", ""),
                it["hymnText"]!!.contains("urheberrechtlich geschützt...", ignoreCase = true)
            )
        }
    }
}

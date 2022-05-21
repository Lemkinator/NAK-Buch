package de.lemke.nakbuch.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.hymnUseCases.GetHymnCountUseCase
import de.lemke.nakbuch.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.ObjectInputStream
import javax.inject.Inject

class InitDataBaseUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
    private val getHymnCount: GetHymnCountUseCase,
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(forceInit: Boolean = false): Job = CoroutineScope(Dispatchers.Default).launch {
        var buchMode = BuchMode.Gesangbuch
        val rubricCountGesangbuch = hymnsRepository.rubricCount(buchMode)
        if (hymnsRepository.getAllRubrics(buchMode).size < rubricCountGesangbuch)
            hymnsRepository.addRubrics(List(rubricCountGesangbuch) { index -> Rubric(RubricId.create(index, buchMode)!!) })
        if (hymnsRepository.getAllHymns(buchMode).size < getHymnCount(buchMode) || forceInit)
            hymnsRepository.addHymns(getAllHymnsFromAssets(buchMode))

        buchMode = BuchMode.Chorbuch
        val rubricCountChorbuch = hymnsRepository.rubricCount(buchMode)
        if (hymnsRepository.getAllRubrics(buchMode).size < rubricCountChorbuch)
            hymnsRepository.addRubrics(List(rubricCountChorbuch) { index -> Rubric(RubricId.create(index, buchMode)!!) })
        if (hymnsRepository.getAllHymns(buchMode).size < getHymnCount(buchMode) || forceInit)
            hymnsRepository.addHymns(getAllHymnsFromAssets(buchMode))

        buchMode = BuchMode.Jugendliederbuch
        val rubricCountJugendliederbuch = hymnsRepository.rubricCount(buchMode)
        if (hymnsRepository.getAllRubrics(buchMode).size < rubricCountJugendliederbuch)
            hymnsRepository.addRubrics(List(rubricCountJugendliederbuch) { index -> Rubric(RubricId.create(index, buchMode)!!) })
        if (hymnsRepository.getAllHymns(buchMode).size < getHymnCount(buchMode) || forceInit)
        //hymnsRepository.addHymns(getAllHymnsFromAssets(buchMode))
            hymnsRepository.addHymns(
                listOf(
                    Hymn(
                        HymnId.create(1, BuchMode.Jugendliederbuch)!!,
                        Rubric(RubricId.create(0, BuchMode.Jugendliederbuch)!!),
                        "1. Titel",
                        "Titel",
                        "Hier steht der Text",
                        "Text: Leonard Lemke (*2000)"
                    ),
                    Hymn(
                        HymnId.create(2, BuchMode.Jugendliederbuch)!!,
                        Rubric(RubricId.create(1, BuchMode.Jugendliederbuch)!!),
                        "2. Lied",
                        "Lied",
                        "Hier steht der Text vom Lied",
                        "Text: Leonard Lemke (*2000)"
                    ),
                    Hymn(
                        HymnId.create(3, BuchMode.Jugendliederbuch)!!,
                        Rubric(RubricId.create(1, BuchMode.Jugendliederbuch)!!),
                        "3. Song",
                        "Song",
                        "Hier steht der Text vom Song",
                        "Text: Leonard Lemke (*2000)"
                    )
                )
            )
    }

    @Suppress("unchecked_Cast")
    fun getAllHymnsFromAssets(buchMode: BuchMode): List<Hymn> {
        val fis: InputStream
        val ois: ObjectInputStream
        var list: java.util.ArrayList<java.util.HashMap<String, String>>? = null
        try {
            fis = when (buchMode) {
                BuchMode.Gesangbuch -> context.resources.assets.open("hymnsGesangbuchNoCopyright.txt")
                BuchMode.Chorbuch -> context.resources.assets.open("hymnsChorbuchNoCopyright.txt")
                BuchMode.Jugendliederbuch -> context.resources.assets.open("hymnsJugendliederbuchNoCopyright.txt")
            }
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
            )
        }
    }
}

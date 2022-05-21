package de.lemke.nakbuch.data.database

import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.PersonalHymn
import de.lemke.nakbuch.domain.model.Rubric
import java.time.LocalDate

fun rubricFromDb(rubricDb: RubricDb): Rubric =
    //if (rubricDb == null) null // rubricPlaceholder?
    //else
        Rubric(
        rubricId = rubricDb.rubricId,
        name = rubricDb.name,
        isMain = (rubricDb.isMain == 0),
    )

fun rubricToDb(rubric: Rubric): RubricDb =
    RubricDb(
        rubricId = rubric.rubricId,
        name = rubric.name,
        isMain = if (rubric.isMain) 0 else 1,
    )

fun hymnFromDb(hymnDb: HymnDb): Hymn =
    Hymn(
        hymnId = hymnDb.hymnId,
        rubric = Rubric(hymnDb.rubricId),
        numberAndTitle = hymnDb.numberAndTitle,
        title = hymnDb.title,
        text = hymnDb.text,
        copyright = hymnDb.copyright,
    )

fun hymnFromDb(hymnAndRubric: HymnAndRubric?): Hymn =
    if (hymnAndRubric == null) Hymn.hymnPlaceholder //TODO Sense??
    else Hymn(
        hymnId = hymnAndRubric.hymn.hymnId,
        rubric = rubricFromDb(hymnAndRubric.rubric),
        numberAndTitle = hymnAndRubric.hymn.numberAndTitle,
        title = hymnAndRubric.hymn.title,
        text = hymnAndRubric.hymn.text,
        copyright = hymnAndRubric.hymn.copyright,
    )

fun hymnToDb(hymn: Hymn): HymnDb =
    HymnDb(
        hymnId = hymn.hymnId,
        rubricId = hymn.rubric.rubricId,
        numberAndTitle = hymn.numberAndTitle,
        title = hymn.title,
        text = hymn.text,
        copyright = hymn.copyright,
    )

fun historyFromDb(hymnAndHistory: HymnAndHistory): Pair<Hymn, LocalDate> =
    Pair(hymnFromDb(hymnAndHistory.hymn), hymnAndHistory.history.date)

fun historyToDb(hymn: Hymn, date: LocalDate): HistoryDb =
    HistoryDb(
        hymnId = hymn.hymnId,
        date = date
    )

fun personalHymnFromDb(personalHymnDataWithLists: PersonalHymnDataWithLists?): PersonalHymn =
    if (personalHymnDataWithLists == null) PersonalHymn.personalHymnPlaceholder //TODO Sense??
    else PersonalHymn(
        hymn = hymnFromDb(personalHymnDataWithLists.hymn),
        favorite = personalHymnDataWithLists.hymnData.favorite == 1,
        notes = personalHymnDataWithLists.hymnData.notes,
        sungOnList = personalHymnDataWithLists.sungOnList.map { it.date },
        photoList = personalHymnDataWithLists.photoList.map { it.uri },
    )

fun personalHymnToHymnDataDb(personalHymn: PersonalHymn): HymnDataDb =
    HymnDataDb(
        hymnId = personalHymn.hymn.hymnId,
        favorite = if (personalHymn.favorite) 1 else 0,
        notes = personalHymn.notes
    )

fun personalHymnToSungOnDbList(personalHymn: PersonalHymn): List<SungOnDb> =
    personalHymn.sungOnList.map {
        SungOnDb(
            hymnId = personalHymn.hymn.hymnId,
            date = it
        )
    }

fun personalHymnToPhotoDbList(personalHymn: PersonalHymn): List<PhotoDb> =
    personalHymn.photoList.mapIndexed { index, uri ->
        PhotoDb(
            index = index,
            hymnId = personalHymn.hymn.hymnId,
            uri = uri
        )
    }


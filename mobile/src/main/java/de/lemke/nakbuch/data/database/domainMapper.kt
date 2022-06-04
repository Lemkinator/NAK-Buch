package de.lemke.nakbuch.data.database

import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.PersonalHymn
import de.lemke.nakbuch.domain.model.Rubric
import java.time.LocalDateTime

fun rubricFromDb(rubricDb: RubricDb): Rubric =
        Rubric(
        rubricId = rubricDb.rubricId,
        name = rubricDb.name,
        isMain = rubricDb.isMain == 0,
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
        containsCopyright = hymnDb.containsCopyright == 1,
    )

fun hymnFromDb(hymnAndRubric: HymnAndRubric?): Hymn =
    if (hymnAndRubric == null) Hymn.hymnPlaceholder
    else Hymn(
        hymnId = hymnAndRubric.hymn.hymnId,
        rubric = rubricFromDb(hymnAndRubric.rubric),
        numberAndTitle = hymnAndRubric.hymn.numberAndTitle,
        title = hymnAndRubric.hymn.title,
        text = hymnAndRubric.hymn.text,
        copyright = hymnAndRubric.hymn.copyright,
        containsCopyright = hymnAndRubric.hymn.containsCopyright == 1
    )

fun hymnToDb(hymn: Hymn): HymnDb =
    HymnDb(
        hymnId = hymn.hymnId,
        rubricId = hymn.rubric.rubricId,
        numberAndTitle = hymn.numberAndTitle,
        title = hymn.title,
        text = hymn.text,
        copyright = hymn.copyright,
        containsCopyright = if (hymn.containsCopyright) 1 else 0,
    )

fun historyFromDb(hymnAndHistory: HymnAndHistory): Pair<Hymn, LocalDateTime> =
    Pair(hymnFromDb(hymnAndHistory.hymn), hymnAndHistory.history.dateTime)

fun historyToDb(hymn: Hymn, dateTime: LocalDateTime): HistoryDb =
    HistoryDb(hymnId = hymn.hymnId, date = dateTime.toLocalDate(), dateTime = dateTime)

fun personalHymnFromDb(personalHymnDb: PersonalHymnDb?): PersonalHymn =
    if (personalHymnDb == null) PersonalHymn.personalHymnPlaceholder
    else if (personalHymnDb.hymnData == null) PersonalHymn(hymnFromDb(personalHymnDb.hymn))
    else PersonalHymn(
        hymn = hymnFromDb(personalHymnDb.hymn),
        favorite = personalHymnDb.hymnData.favorite == 1,
        notes = personalHymnDb.hymnData.notes,
        sungOnList = personalHymnDb.sungOnList.map { it.date }.reversed(),
        photoList = personalHymnDb.photoList.sortedBy{it.position}.map { it.uri },
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
            position = index,
            hymnId = personalHymn.hymn.hymnId,
            uri = uri
        )
    }

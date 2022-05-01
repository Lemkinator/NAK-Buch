package de.lemke.nakbuch.domain.model

import App
import de.lemke.nakbuch.R

data class Rubric(
    val buchMode: BuchMode,
    val index: Int,
    val name: String,
    val isMain: Boolean
) {
    constructor(buchMode: BuchMode, index: Int) :
            this(
                buchMode,
                index,
                getRubricName(buchMode, index),
                isMainRubric(buchMode, index)
            )

}

private fun isMainRubric(buchMode: BuchMode, index: Int): Boolean {
    val rubricListItem =
        if (buchMode == BuchMode.Gesangbuch) App.myRepository.getResources().getIntArray(R.array.RubricListItemGesangbuch)
        else App.myRepository.getResources().getIntArray(R.array.RubricListItemChorbuch)
    return rubricListItem[index] == 0
}

private fun getRubricName(buchMode: BuchMode, index: Int): String {
    val rubricTitles =
        if (buchMode == BuchMode.Gesangbuch) App.myRepository.getResources().getStringArray(R.array.RubricTitlesGesangbuch)
        else App.myRepository.getResources().getStringArray(R.array.RubricTitlesChorbuch)
    return rubricTitles[index]
}
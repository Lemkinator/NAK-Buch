package de.lemke.nakbuch.domain.model

import de.lemke.nakbuch.App
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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rubric

        if (buchMode != other.buchMode || index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        return index + 32*buchMode.hashCode()
    }
}

private fun isMainRubric(buchMode: BuchMode, index: Int): Boolean {
    val rubricListItem =
        if (buchMode == BuchMode.Gesangbuch) App.myRepository.resources.getIntArray(R.array.RubricListItemGesangbuch)
        else App.myRepository.resources.getIntArray(R.array.RubricListItemChorbuch)
    // check?`if (index < 0 || index >= rubricListItem.size) return true
    return rubricListItem[index] == 0
}

private fun getRubricName(buchMode: BuchMode, index: Int): String {
    val rubricTitles =
        if (buchMode == BuchMode.Gesangbuch) App.myRepository.resources.getStringArray(R.array.RubricTitlesGesangbuch)
        else App.myRepository.resources.getStringArray(R.array.RubricTitlesChorbuch)
    // check? if (index < 0 || index >= rubricTitles.size) return "undefined"
    return rubricTitles[index]
}

val rubricPlaceholder = Rubric(BuchMode.Gesangbuch, -1, "Placeholder", true)

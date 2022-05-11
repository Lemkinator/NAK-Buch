package de.lemke.nakbuch.domain.model

import de.lemke.nakbuch.App
import de.lemke.nakbuch.R

data class Rubric(
    val index: Int,
    val buchMode: BuchMode,
    val name: String,
    val isMain: Boolean,
) {
    constructor(index: Int, buchMode: BuchMode) :
            this(
                index,
                buchMode,
                getRubricName(buchMode, index),
                isMainRubric(buchMode, index),
            )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rubric

        if (buchMode != other.buchMode || index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        return index + 32* buchMode.hashCode()
    }
}

private fun isMainRubric(buchMode: BuchMode, index: Int): Boolean {
    val rubricListItem =
        if (buchMode == BuchMode.Gesangbuch) App.myResources.getIntArray(R.array.RubricListItemGesangbuch)
        else App.myResources.getIntArray(R.array.RubricListItemChorbuch)
    // check?`if (index < 0 || index >= rubricListItem.size) return true
    return rubricListItem[index] == 0
}

private fun getRubricName(buchMode: BuchMode, index: Int): String {
    val rubricTitles =
        if (buchMode == BuchMode.Gesangbuch) App.myResources.getStringArray(R.array.RubricTitlesGesangbuch)
        else App.myResources.getStringArray(R.array.RubricTitlesChorbuch)
    // check? if (index < 0 || index >= rubricTitles.size) return "undefined"
    return rubricTitles[index]
}

val rubricPlaceholder = Rubric(-1, BuchMode.Gesangbuch, "Placeholder", true)

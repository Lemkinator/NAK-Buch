package de.lemke.nakbuch.domain.model

import android.content.Context
import de.lemke.nakbuch.R

data class Rubric(
    val gesangbuchSeleced: Boolean,
    val index: Int,
    val name: String,
    val isMain: Boolean
) {
    constructor(mContext: Context, gesangbuchSeleced: Boolean, index: Int) :
            this(
                gesangbuchSeleced,
                index,
                getRubricName(mContext, gesangbuchSeleced, index),
                isMain(mContext, gesangbuchSeleced, index)
            )

}

private fun isMain(mContext: Context, gesangbuchSeleced: Boolean, index: Int): Boolean {
    val rubricListItem =
        if (gesangbuchSeleced) mContext.resources.getIntArray(R.array.RubricListItemGesangbuch)
        else mContext.resources.getIntArray(R.array.RubricListItemChorbuch)
    return rubricListItem[index] == 0
}

private fun getRubricName(mContext: Context, gesangbuchSeleced: Boolean, index: Int): String {
    val rubricTitles =
        if (gesangbuchSeleced) mContext.resources.getStringArray(R.array.RubricTitlesGesangbuch)
        else mContext.resources.getStringArray(R.array.RubricTitlesChorbuch)
    return rubricTitles[index]
}
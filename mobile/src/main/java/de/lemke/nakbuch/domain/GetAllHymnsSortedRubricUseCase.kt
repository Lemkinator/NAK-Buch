package de.lemke.nakbuch.domain

import android.content.Context
import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnsRepo

class GetAllHymnsSortedRubricUseCase {
    operator fun invoke(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean,
        rubricIndex: Int
    ) = hymnsRepo.getAllHymnsSortedRubric(mContext, sp, gesangbuchSelected, rubricIndex)
}
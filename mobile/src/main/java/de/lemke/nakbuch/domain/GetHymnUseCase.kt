package de.lemke.nakbuch.domain

import android.content.Context
import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnsRepo

class GetHymnUseCase {
    operator fun invoke(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean,
        hymnNr: Int
    ) = hymnsRepo.getHymnByNumber(mContext, sp, gesangbuchSelected, hymnNr)
}
package de.lemke.nakbuch.domain

import android.content.Context
import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnsRepo

class GetAllHymnsSortedAlphabeticalUseCase {
    operator fun invoke(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean,
    ) = hymnsRepo.getAllHymnsSortedAlphabetic(mContext, sp, gesangbuchSelected)
}
package de.lemke.nakbuch.domain

import android.content.Context
import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnsRepo

class GetAllHymnsUseCase {
    operator fun invoke(
        mContext: Context,
        sp: SharedPreferences,
        gesangbuchSelected: Boolean,
    ) = hymnsRepo.getAllHymns(mContext, sp, gesangbuchSelected)
}
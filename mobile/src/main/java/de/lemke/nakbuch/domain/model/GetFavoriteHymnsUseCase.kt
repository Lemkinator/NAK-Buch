package de.lemke.nakbuch.domain.model

import android.content.Context
import android.content.SharedPreferences
import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.data.hymnsRepo

class GetFavoriteHymnsUseCase {
    operator fun invoke(
        mContext: Context,
        sp: SharedPreferences,
        spHymns: SharedPreferences,
        gesangbuchSelected: Boolean,
    ): List<Hymn> {
        return hymnsRepo.getAllHymns(mContext, sp, gesangbuchSelected).filter { hymn ->
            hymnDataRepo.getAllHymnData(
                gesangbuchSelected,
                spHymns
            )[hymn.number - 1]?.favorite ?: false
        }
    }
}
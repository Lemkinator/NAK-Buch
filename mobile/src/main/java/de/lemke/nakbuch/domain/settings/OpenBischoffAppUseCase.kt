package de.lemke.nakbuch.domain.settings

import android.content.Context
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode

class OpenBischoffAppUseCase {
    operator fun invoke(mContext: Context, buchMode: BuchMode) {
        OpenAppUseCase()(
            mContext, mContext.getString(
                if (buchMode == BuchMode.Gesangbuch) R.string.bischoffGesangbuchPackageName else R.string.bischoffChorbuchPackageName
            ),
            true
        )
    }
}
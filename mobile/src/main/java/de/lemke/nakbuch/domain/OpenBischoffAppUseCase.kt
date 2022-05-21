package de.lemke.nakbuch.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import javax.inject.Inject

class OpenBischoffAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(buchMode: BuchMode) {
        OpenAppUseCase(context)(
            context.getString(if (buchMode == BuchMode.Gesangbuch) R.string.bischoffGesangbuchPackageName else R.string.bischoffChorbuchPackageName),
            true
        )
    }
}
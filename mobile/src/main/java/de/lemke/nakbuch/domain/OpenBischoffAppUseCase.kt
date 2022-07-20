package de.lemke.nakbuch.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.model.BuchMode
import javax.inject.Inject

class OpenBischoffAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openApp: OpenAppUseCase,
) {
    operator fun invoke(buchMode: BuchMode, tryLocalFirst: Boolean) = when (buchMode) {
        BuchMode.Gesangbuch -> openApp(context.getString(R.string.bischoffGesangbuchPackageName), tryLocalFirst)
        BuchMode.Chorbuch -> openApp(context.getString(R.string.bischoffChorbuchPackageName), tryLocalFirst)
        else -> {} //No official app, do nothing
    }
}
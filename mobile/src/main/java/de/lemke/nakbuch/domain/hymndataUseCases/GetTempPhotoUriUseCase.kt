package de.lemke.nakbuch.domain.hymndataUseCases

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class GetTempPhotoUriUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(useFileProvider: Boolean): Uri =
        if (useFileProvider)
            FileProvider.getUriForFile(context, "de.lemke.nakbuch.fileprovider", File(context.cacheDir, "currentPhotoUncompressed.jpg"))
        else File(context.cacheDir, "currentPhotoUncompressed.jpg").toUri()
}
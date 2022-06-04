package de.lemke.nakbuch.domain.hymndataUseCases

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.domain.GetUserSettingsUseCase
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CompressJPGUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUserSettings: GetUserSettingsUseCase,
) {
    suspend operator fun invoke(origin: Uri, destination: Uri) = withContext(Dispatchers.Default) {
        Log.d("Compressor", "Compressing file: " + origin.toFile().absolutePath)
        val resolution = getUserSettings().photoResolution.value
        val quality = getUserSettings().photoQuality.value
        Compressor.compress(context, origin.toFile()) {
            resolution(resolution, resolution)
            quality(quality)
            size(2_097_152) // 2 MB
            format(Bitmap.CompressFormat.JPEG)
            destination(destination.toFile())
        }
    }
}
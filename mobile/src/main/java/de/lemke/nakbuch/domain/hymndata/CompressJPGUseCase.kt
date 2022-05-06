package de.lemke.nakbuch.domain.hymndata

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import de.lemke.nakbuch.R
import de.lemke.nakbuch.domain.settings.GetStringSettingUseCase
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompressJPGUseCase {
    suspend operator fun invoke(mContext: Context, origin: Uri , destination: Uri) {
        Log.d("Compressor", "Compressing file: " + origin.toFile().absolutePath)
        val resolution = when (GetStringSettingUseCase()("imgResolution", mContext.getString(R.string.medium))) {
            mContext.getString(R.string.veryLow) -> 512
            mContext.getString(R.string.low) -> 1024
            mContext.getString(R.string.medium) -> 2048
            mContext.getString(R.string.high) -> 4096
            mContext.getString(R.string.veryHigh) -> 8192
            else -> 2048
        }
        val quality = when (GetStringSettingUseCase()("imgQuality", mContext.getString(R.string.medium))) {
            mContext.getString(R.string.veryLow) -> 15
            mContext.getString(R.string.low) -> 25
            mContext.getString(R.string.medium) -> 50
            mContext.getString(R.string.high) -> 75
            mContext.getString(R.string.veryHigh) -> 100
            else -> 50
        }
        withContext(Dispatchers.IO) {
            Compressor.compress(mContext, origin.toFile()) {
                resolution(resolution, resolution)
                quality(quality)
                size(2_097_152) // 2 MB
                format(Bitmap.CompressFormat.JPEG)
                destination(destination.toFile()) //File(currentFolder.absolutePath, file.name))
            }
        }
    }
}
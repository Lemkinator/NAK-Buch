package de.lemke.nakbuch.domain.hymndata

import android.content.Context
import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPhotoUseCase {
    suspend operator fun invoke(mContext: Context, hymn: Hymn, hymnData: HymnData, index: Int) =
        CoroutineScope(Dispatchers.IO).launch {
            val uri = hymnDataRepo.getUri(hymn)
            CompressJPGUseCase()(mContext, GetTempPhotoUriUseCase()(), uri)
            hymnData.photoList.add(index, uri.toString())
            SetHymnDataUseCase()(hymn, hymnData)
        }
}
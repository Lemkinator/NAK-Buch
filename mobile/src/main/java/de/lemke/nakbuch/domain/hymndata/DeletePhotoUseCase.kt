package de.lemke.nakbuch.domain.hymndata

import android.net.Uri
import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.Hymn
import de.lemke.nakbuch.domain.model.HymnData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeletePhotoUseCase {
    suspend operator fun invoke(hymn: Hymn, hymnData: HymnData, index: Int) {
        withContext(Dispatchers.IO) {
            hymnDataRepo.deletePhoto(Uri.parse(hymnData.photoList.removeAt(index)))
            SetHymnDataUseCase()(hymn, hymnData)
        }
    }

}

package de.lemke.nakbuch.domain.hymndataUseCases

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.domain.model.PersonalHymn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

class AddPhotoUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val compressJPG: CompressJPGUseCase,
    private val getTempPhotoUri: GetTempPhotoUriUseCase,
    private val setHymnData: SetPersonalHymnUseCase,
) {
    suspend operator fun invoke(personalHymn: PersonalHymn, index: Int) =
        CoroutineScope(Dispatchers.Default).launch {
            val destinationUri =
                File("${context.filesDir}/hymnPhotos/${personalHymn.hymn.hymnId.toInt()}/${UUID.randomUUID()}.jpg").toUri()
            compressJPG(getTempPhotoUri(false), destinationUri)
            val newPhotoList = personalHymn.photoList.toMutableList()
            newPhotoList.add(index, destinationUri)
            personalHymn.photoList = newPhotoList
            setHymnData(personalHymn)
        }
}
package de.lemke.nakbuch.domain.hymndataUseCases

import android.util.Log
import de.lemke.nakbuch.domain.model.PersonalHymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeletePhotoUseCase @Inject constructor(
    private val setHymnData: SetPersonalHymnUseCase,
) {
    suspend operator fun invoke(personalHymn: PersonalHymn, index: Int) = withContext(Dispatchers.Default) {
        val newPhotoList = personalHymn.photoList.toMutableList()
        val fdelete = File(newPhotoList.removeAt(index).path!!)
        if (fdelete.exists()) {
            if (fdelete.delete()) Log.d("deleted File", fdelete.absolutePath)
            else Log.e("could not delete File", fdelete.absolutePath)
        }
        personalHymn.photoList = newPhotoList
        setHymnData(personalHymn)
    }
}

package de.lemke.nakbuch.domain.hymndata

import android.util.Log
import de.lemke.nakbuch.data.hymnDataRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.domain.model.Hymn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetFavoritesFromListUseCase {
    suspend operator fun invoke(buchMode: BuchMode, hymnList: ArrayList<Hymn>, selected: HashMap<Int, Boolean>, favorite: Boolean) {
        Log.d("test", "size: " + selected.size + " : " + selected.toString())
        withContext(Dispatchers.IO) {
            selected.forEach {
                if (it.value) {
                    val hymn = hymnList[it.key]
                    hymnDataRepo.setHymnData(hymn, GetHymnDataUseCase()(hymn).copy(favorite = favorite))
                }
            }
            hymnDataRepo.writeHymnDataToPreferences(buchMode)
        }
    }
}

package de.lemke.nakbuch.data.database

import android.net.Uri
import androidx.room.TypeConverter
import de.lemke.nakbuch.domain.model.HymnId
import de.lemke.nakbuch.domain.model.RubricId
import java.time.LocalDate

/** Type converters to map between SQLite types and entity types. */
object Converters {
    /** Returns the int representation of the [hymnId]. */
    @TypeConverter
    fun hymnIdtoTb(hymnId: HymnId): Int {
        return hymnId.toInt()
    }

    /** Returns the [HymnId] represented by the [hymntIdInt]. */
    @TypeConverter
    fun hymnIdFromDb(hymntIdInt: Int): HymnId? {
        return HymnId.create(hymntIdInt)
    }
    /** Returns the int representation of the [rubricId]. */
    @TypeConverter
    fun rubricIdtoTb(rubricId: RubricId): Int {
        return rubricId.toInt()
    }

    /** Returns the [RubricId] represented by the [rubricIdInt]. */
    @TypeConverter
    fun rubricIdFromDb(rubricIdInt: Int): RubricId? {
        return RubricId.create(rubricIdInt)
    }

    /** Returns the string representation of the [uri]. */
    @TypeConverter
    fun uriToDb(uri: Uri): String {
        return uri.toString()
    }

    /** Returns the [Uri] represented by the [uriString]. */
    @TypeConverter
    fun uriFromDb(uriString: String): Uri {
        return Uri.parse(uriString)
    }

    /** Returns the string representation of the [localDate]. */
    @TypeConverter
    fun localDateToDb(localDate: LocalDate): String {
        return localDate.toString()
    }

    /** Returns the [LocalDate] represented by the [localDateString]. */
    @TypeConverter
    fun localDateFromDb(localDateString: String): LocalDate {
        return LocalDate.parse(localDateString)
    }
}

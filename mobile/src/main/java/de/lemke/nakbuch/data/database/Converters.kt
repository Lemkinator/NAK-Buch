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
    fun hymnIdtoTb(hymnId: HymnId): Int = hymnId.toInt()

    /** Returns the [HymnId] represented by the [hymntIdInt]. */
    @TypeConverter
    fun hymnIdFromDb(hymntIdInt: Int): HymnId? = HymnId.create(hymntIdInt)

    /** Returns the int representation of the [rubricId]. */
    @TypeConverter
    fun rubricIdtoTb(rubricId: RubricId): Int = rubricId.toInt()

    /** Returns the [RubricId] represented by the [rubricIdInt]. */
    @TypeConverter
    fun rubricIdFromDb(rubricIdInt: Int): RubricId? = RubricId.create(rubricIdInt)

    /** Returns the string representation of the [uri]. */
    @TypeConverter
    fun uriToDb(uri: Uri): String = uri.toString()

    /** Returns the [Uri] represented by the [uriString]. */
    @TypeConverter
    fun uriFromDb(uriString: String): Uri = Uri.parse(uriString)

    /** Returns the string representation of the [localDate]. */
    @TypeConverter
    fun localDateToDb(localDate: LocalDate): String = localDate.toString()

    /** Returns the [LocalDate] represented by the [localDateString]. */
    @TypeConverter
    fun localDateFromDb(localDateString: String): LocalDate = LocalDate.parse(localDateString)
}

package de.lemke.nakbuch.data.database

import androidx.room.TypeConverter
import java.time.ZonedDateTime

/** Type converters to map between SQLite types and entity types. */
object Converters {
    /** Returns the string representation of the [timestamp]. */
    @TypeConverter
    fun timestampToDb(timestamp: ZonedDateTime?): String? {
        return timestamp?.toString()
    }

    /** Returns the [ZonedDateTime] represented by the [timestamp]. */
    @TypeConverter
    fun timestampFromDb(timestamp: String?): ZonedDateTime? {
        return timestamp?.let {
            ZonedDateTime.parse(it)
        }
    }
}

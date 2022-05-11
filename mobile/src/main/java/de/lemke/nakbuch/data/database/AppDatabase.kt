package de.lemke.nakbuch.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    version = 1,
    entities = [
        HymnDb::class,
        HymnDataDb::class,
        RubricDb::class,
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hymnDao(): HymnDao
    abstract fun hymnDataDao(): HymnDataDao
    abstract fun rubricDao(): RubricDao
}

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
        SungOnDb::class,
        PhotoDb::class,
        HistoryDb::class,
    ],
    exportSchema = true,
    /*autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ],*/

)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hymnDao(): HymnDao
    abstract fun hymnDataDao(): HymnDataDao
    abstract fun rubricDao(): RubricDao
    abstract fun sungOnDao(): SungOnDao
    abstract fun photoDao(): PhotoDao
    abstract fun historyDao(): HistoryDao
}

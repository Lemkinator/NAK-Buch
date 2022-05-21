package de.lemke.nakbuch

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.lemke.nakbuch.data.database.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object
 PersistenceModule : Application() {

    private val Context.userSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "userSettings")

    @Provides
    @Singleton
    fun provideUserSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userSettingsStore

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "app")
        //.createFromAsset("databases/app.db")
        .apply {
            if (BuildConfig.DEBUG) fallbackToDestructiveMigration()
        }
        .build()

    @Provides
    fun provideRubricDao(
        database: AppDatabase,
    ): RubricDao = database.rubricDao()

    @Provides
    fun provideHymnDao(
        database: AppDatabase,
    ): HymnDao = database.hymnDao()

    @Provides
    fun provideHymnDataDao(
        database: AppDatabase,
    ): HymnDataDao = database.hymnDataDao()

    @Provides
    fun provideHistoryDao(
        database: AppDatabase,
    ): HistoryDao = database.historyDao()

    @Provides
    fun provideSungOnDao(
        database: AppDatabase,
    ):  SungOnDao = database.sungOnDao()

    @Provides
    fun providePhotoDao(
        database: AppDatabase,
    ):  PhotoDao = database.photoDao()
}



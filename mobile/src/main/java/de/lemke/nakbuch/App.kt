package de.lemke.nakbuch

import android.app.Application
import android.content.ContentResolver
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import de.lemke.nakbuch.data.database.AppDatabase
import java.io.File

/**
 * Main entry point into the application process.
 * Registered in the AndroidManifest.xml file.
 */
class App : Application() {

    private val userSettingsStore: DataStore<Preferences> by preferencesDataStore(name = "userSettings")

    override fun onCreate() {
        super.onCreate()
        //userSettingsRepo = UserSettingsRepository(userSettingsStore)
        defaultSharedPreferences = getSharedPreferences(getString(R.string.preferenceFileDefault), MODE_PRIVATE)
        hymnDataSharedPreferences = getSharedPreferences(getString(R.string.preferenceFileHymns), MODE_PRIVATE)
        myResources = resources
        myCacheDir = cacheDir
        myFilesDir = filesDir
        myAudioManager = getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        myContentResolver = contentResolver

        database = Room
            .databaseBuilder(this, AppDatabase::class.java, "app")
            .apply {
                if (BuildConfig.DEBUG) fallbackToDestructiveMigration()
            }
            .build()

        /*
         In a real app we should never use runBlocking {}. Especially not on app start up.
         However, we would need to refactor the ProductsRepository to use Flow. Therefore, we accept this hack for now. After all, it is
         just a demo app.
         The real solution would be to launch a coroutine in the app scope:
         private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
         scope.launch {  }
         */
        /*
        runBlocking {
            InitializeShoppingCartIdUseCase(userSettingsRepo)()
        }
        */
    }

    companion object {
        /** Singleton [SharedPreferences] instance. */
        lateinit var defaultSharedPreferences: SharedPreferences
        /** Singleton [SharedPreferences] instance. */
        lateinit var hymnDataSharedPreferences: SharedPreferences
        /** Singleton [Resources] instance. */
        lateinit var myResources: Resources
        /** Singleton [File] instance. */
        lateinit var myCacheDir: File
        /** Singleton [File] instance. */
        lateinit var myFilesDir: File
        /** Singleton [AudioManager] instance. */
        lateinit var myAudioManager: AudioManager
        /** Singleton [ContentResolver] instance. */
        lateinit var myContentResolver: ContentResolver
        /** Singleton [AppDatabase] instance. */
        lateinit var database: AppDatabase
    }
}

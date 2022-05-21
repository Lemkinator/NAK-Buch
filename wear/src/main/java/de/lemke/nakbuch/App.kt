package de.lemke.nakbuch

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources

/**
 * Main entry point into the application process.
 * Registered in the AndroidManifest.xml file.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        defaultSharedPreferences = getSharedPreferences(getString(R.string.preferenceFileDefault), MODE_PRIVATE)
        myResources = resources
        /*
        CoroutineScope(Dispatchers.Main).launch {
            GetAllHymnsUseCase()(BuchMode.Gesangbuch)
            GetAllHymnsUseCase()(BuchMode.Chorbuch)
        }
        */
    }

    companion object {
        /** Singleton [SharedPreferences] instance. */
        lateinit var defaultSharedPreferences: SharedPreferences
        /** Singleton [Resources] instance. */
        lateinit var myResources: Resources
    }
}

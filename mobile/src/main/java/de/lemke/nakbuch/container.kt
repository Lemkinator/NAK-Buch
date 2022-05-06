package de.lemke.nakbuch

import android.app.Application
import android.content.ContentResolver
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import java.io.File

data class MyRepository(
    val defaultSharedPreferences: SharedPreferences,
    val hymnDataSharedPreferences: SharedPreferences,
    val resources: Resources,
    val cacheDir: File,
    val filesDir: File,
    val audioManager: AudioManager,
    val contentResolver: ContentResolver
) {
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        myRepository = MyRepository(
            getSharedPreferences(getString(R.string.preferenceFileDefault), MODE_PRIVATE),
            getSharedPreferences(getString(R.string.preferenceFileHymns), MODE_PRIVATE),
            resources,
            cacheDir,
            filesDir,
            getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager,
            contentResolver
        )
    }

    companion object {
        lateinit var myRepository: MyRepository
    }
}

// can be used globally
// App.myRepository.getSomeData()

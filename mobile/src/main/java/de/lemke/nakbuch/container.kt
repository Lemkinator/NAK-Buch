import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.content.res.Resources
import de.lemke.nakbuch.R

class MyRepository(
    private val sp: SharedPreferences,
    private val spHymns: SharedPreferences,
    private val resources: Resources
) {
    fun getResources() = resources
    fun getAssets(): AssetManager = resources.assets
    fun getDefaultSharedPreferences() = sp
    fun getHymnDataSharedPreferences() = spHymns
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        myRepository = MyRepository(
            getSharedPreferences(getString(R.string.preferenceFileDefault), Context.MODE_PRIVATE),
            getSharedPreferences(getString(R.string.preferenceFileHymns), Context.MODE_PRIVATE),
            resources
        )
    }

    companion object {
        lateinit var myRepository: MyRepository
    }
}

// can be used globally
// App.myRepository.getSomeData()

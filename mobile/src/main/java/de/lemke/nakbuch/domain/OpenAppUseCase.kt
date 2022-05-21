package de.lemke.nakbuch.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.R
import javax.inject.Inject

class OpenAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(packageName: String, tryLocalFirst: Boolean) {
        if (tryLocalFirst) openAppWithPackageName(packageName)
        else openAppWithPackageNameOnStore(packageName)
    }


    private fun openAppWithPackageName(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            openAppWithPackageNameOnStore(packageName)
        }
    }

    private fun openAppWithPackageNameOnStore(packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(context.getString(R.string.playStoreAppLink) + packageName)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
            intent.data =
                Uri.parse(context.getString(R.string.playStoreLink) + packageName)
            context.startActivity(intent)
        }
    }

    /* Since Android 11 (API level 30), most user-installed apps are not visible by default.
    In your manifest, you must statically declare which apps you are going to get info about, as in the following:
    <manifest>
        <queries>
            <!-- Explicit apps you know in advance about: -->
            <package android:name="com.example.this.app"/>
            <package android:name="com.example.this.other.app"/>
        </queries>
        ...
    </manifest> */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
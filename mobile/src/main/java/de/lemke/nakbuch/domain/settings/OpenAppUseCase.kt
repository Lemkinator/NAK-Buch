package de.lemke.nakbuch.domain.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import de.lemke.nakbuch.R

class OpenAppUseCase {
    operator fun invoke(mContext: Context, packageName: String, tryLocalFirst: Boolean) {
        if (tryLocalFirst) openAppWithPackageName(mContext, packageName)
        else openAppWithPackageNameOnStore(mContext, packageName)
    }


    private fun openAppWithPackageName(mContext: Context, packageName: String) {
        val intent = mContext.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            mContext.startActivity(intent)
        } else {
            openAppWithPackageNameOnStore(mContext, packageName)
        }
    }

    private fun openAppWithPackageNameOnStore(mContext: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(mContext.getString(R.string.playStoreAppLink) + packageName)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            mContext.startActivity(intent)
        } catch (anfe: ActivityNotFoundException) {
            intent.data =
                Uri.parse(mContext.getString(R.string.playStoreLink) + packageName)
            mContext.startActivity(intent)
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
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}